package com.vts.fxdata.controllers;

import com.niamedtech.expo.exposerversdk.PushClient;
import com.niamedtech.expo.exposerversdk.PushClientException;
import com.vts.fxdata.entities.ChartState;
import com.vts.fxdata.entities.Client;
import com.vts.fxdata.entities.Record;
import com.vts.fxdata.entities.Trade;
import com.vts.fxdata.models.*;
import com.vts.fxdata.models.dto.*;
import com.vts.fxdata.notifications.NotificationServer;
import com.vts.fxdata.repositories.*;
import com.vts.fxdata.utils.FxUtils;
import com.vts.fxdata.utils.TimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;

@RestController
@RequestMapping("api/v2/fxdata")
@CrossOrigin(origins = "http://localhost:8080")
public class MainControllerV2 {

    @Autowired
    private final RecordService recordService;
    @Autowired
    private final StateService stateService;
    @Autowired
    private final ConfirmationService confirmationService;
    @Autowired
    private final TradeService tradeService;
    @Autowired
    private final ClientService clientService;

    @Autowired
    private final HttpServletRequest request;

    public MainControllerV2(RecordService recordService,
                            ConfirmationService confirmationService,
                            TradeService tradeService,
                            ClientService clientService,
                            StateService stateService,
                            HttpServletRequest request) {
        this.recordService = recordService;
        this.confirmationService = confirmationService;
        this.tradeService = tradeService;
        this.clientService = clientService;
        this.stateService = stateService;
        this.request = request;
    }

    @PostMapping("/addrecord")
    public ResponseEntity<String> addRecord(@RequestBody com.vts.fxdata.models.dto.Record request) throws PushClientException, InterruptedException {
        try {
            var rec = new Record(request.getPair(),
                    TimeframeEnum.valueOf(request.getTimeframe()),
                    ActionEnum.valueOf(request.getAction()),
                    StateEnum.valueOf(request.getState()),
                    request.getPrice(),
                    false);
            this.recordService.addRecord(rec);
            requestConfirmation(rec);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(null, HttpStatus.CREATED);
    }

    @GetMapping("/")
    public List<DayRecords> getConfirmedRecords(@RequestParam(name="tzo", required = false) Integer tzOffset) {
        return recordService.getConfirmedRecords(tzOffset==null ? 0:tzOffset.intValue());
    }

    @GetMapping("/{pair}")
    public List<DayRecords> getConfirmedRecords(
            @PathVariable String pair, @RequestParam(name="tzo", required = false) Integer tzOffset) {

        return this.recordService.getConfirmedRecords(pair, tzOffset==null ? 0:tzOffset.intValue());
    }

    @PostMapping("/delete/{recordId}")
    public ResponseEntity<String> deleteRecord(@PathVariable Long recordId) {
        try {
            this.recordService.deleteRecord(recordId);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @PostMapping("/confirmed")
    public ResponseEntity<String> confirmationFound(@RequestBody Confirmation request) throws PushClientException, InterruptedException {

        var pending = this.confirmationService.findById(request.getId());
        if (!pending.isPresent()) {
            return new ResponseEntity<>("Confirmation not found", HttpStatus.NOT_FOUND);
        }

        // mark the newest record as confirmed and delete the rest
        var confirmation = pending.get();
        com.vts.fxdata.entities.Record rec = null;

        var unconfirmedRecordIds = confirmation.getRecordIds();
        Collections.reverse(unconfirmedRecordIds);
        var iterator = unconfirmedRecordIds.iterator();
        String errorMessage = null;
        String notes = null;

        try {
            if (!iterator.hasNext()) {
                return new ResponseEntity<>("No records found", HttpStatus.NOT_FOUND);
            }
            var recordId = iterator.next();
            rec = this.recordService.getRecordById(recordId).get();
            // check if confirmation does not clash with a trend in this timeframe
            var state = this.stateService.getState(rec.getPair(), rec.getTimeframe().ordinal());
            // TODO check the strength of the trend. Must be older than 150 candles to trust it
            // TODO and ignore opposite signals
            if (state.getState() == StateEnum.Bullish && rec.getAction() == ActionEnum.Sell ||
                state.getState() == StateEnum.Bearish && rec.getAction() == ActionEnum.Buy) {
                // ignore sells in a bullish trend
                // ignore buys in a bearish trend
                // delete all records that signal to act against the trend
                this.recordService.deleteRecord(recordId);
                while(iterator.hasNext()) {
                    var otherId = iterator.next();
                    this.recordService.deleteRecord(otherId);
                }
            } else {
                notes = buildNotes(rec, request.getLevels(), iterator);
                rec.setConfirmation(true);
                rec.setPrice(request.getPrice());
                rec.setStartPrice(request.getLevels()[0]);
                rec.setTargetPrice(request.getLevels()[1]);
                rec.setTime(LocalDateTime.now(ZoneOffset.UTC));
                rec.setNotes(notes);

                var targetPips = FxUtils.getPips(rec.getTargetPrice(), request.getPrice(), request.getPoint());
                if (rec.getAction() == ActionEnum.Sell) {
                    targetPips = FxUtils.getPips(request.getPrice(), rec.getTargetPrice(), request.getPoint());
                }
                rec.setTargetPips(targetPips);

                var progress = FxUtils.getProgress(request.getPrice(), rec.getStartPrice(), request.getPoint(), targetPips);
                if (rec.getAction() == ActionEnum.Sell) {
                    progress = FxUtils.getProgress(rec.getStartPrice(), request.getPrice(), request.getPoint(), targetPips);
                }
                rec.setProgress(progress);
                rec.setMinProgress(progress);
                rec.setMaxProgress(progress);
                this.recordService.save(rec);

                // update the corresponding state
                updateStateWithNewAction(rec, request.getPoint());
            }
            // remove record IDs
            confirmation.getRecordIds().clear();
            this.confirmationService.save(confirmation);



        } catch(Exception e) {
            errorMessage = e.getMessage()+"\r\n"+notes;
        }

        if (errorMessage!=null) {
            return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // send out notifications
        int count = rec.getConfirmationDelay()==null ? 0: rec.getConfirmationDelay().length();
        String message = String.format("%s %s  (%s)", rec.getAction(),
                rec.getPair(), rec.getPrice());
        pushNotifications(message, rec.getTimeframe() + " " + rec.getState() +
                " ".repeat(24-count) + rec.getConfirmationDelay());

        this.confirmationService.deleteConfirmation(request.getId());
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @GetMapping("/pending/{pair}")
    public List<com.vts.fxdata.entities.Confirmation> getPendingConfirmations(@PathVariable String pair) {
        return this.confirmationService.getPendingConfirmations(pair);
    }

    @PostMapping("/state")
    public ResponseEntity<String> setChartState(@RequestBody State state) {
        try {
            this.stateService.setState(ChartState.newInstance(state));
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @GetMapping("/states")
    public List<Pair> getStates(@RequestParam(value = "state", required = false) StateEnum state,
                                @RequestParam(name="tzo", required = false) Integer tzOffset) {
        var states = this.stateService.getLastStates(state);
        return StatesView.getPairs(states, tzOffset==null ? 0:tzOffset.intValue());
    }

    @PostMapping("/addclient")
    @Deprecated
    public void addClient(@RequestBody ExpoToken request) {
        if (!PushClient.isExponentPushToken(request.getToken()))
            return;

        try {
            this.clientService.addClient(new Client(request.getToken()));
        } catch(DataIntegrityViolationException e) {
            System.out.println(e);
        }
    }

    @PostMapping("/heartbeat")
    public ResponseEntity<String> addClient(@RequestBody Heartbeat request) {
        if (request==null) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }

        for(ChartState state : this.stateService.getStates(request.getPair())) {
            updateStateMetrics(state, request);
        }
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @GetMapping("/results")
    public List<Result> getResults(@RequestParam(name="tzo", required = false) Integer tzOffset) {
        var results = new ArrayList<Result>();
        for(var r : this.recordService.getResultRecords()) {
            var startTime = r.getStartTime().minusMinutes(tzOffset==null ? 0:tzOffset.intValue());
            var endTime = r.getEndTime().minusMinutes(tzOffset==null ? 0:tzOffset.intValue());
            if (r.getAction()==null) continue; // TODO remove
            results.add(new Result(
                    r.getPair(),
                    r.getTimeframe().toString(),
                    r.getAction().toString(),
                    r.getState().toString(),
                    r.getPrice(),
                    r.getExitPrice(),
                    r.getTargetPips(),
                    r.getProfit(),
                    r.getMaxDrawdown(),
                    r.getMinProgress(),
                    r.getMaxProgress(),
                    TimeUtils.formatTime(startTime),
                    TimeUtils.formatTime(endTime),
                    TimeUtils.formatDuration(r.getStartTime(), r.getEndTime(), false)));
        }
        return results;
    }

    @GetMapping("/trades")
    public List<com.vts.fxdata.models.dto.Trade> getTrades(@RequestParam(name="tzo", required = false) Integer tzOffset,
                                                           @RequestParam(name="act", required = false) boolean active) {
        return TradesView.getTrades(active, this.tradeService.getTrades(), tzOffset==null ? 0: tzOffset);
    }

    @PostMapping("/trade/opened/{id}")
    public ResponseEntity<String> acknowledgeTradeOpened(@PathVariable Long id) {
        try {
            var trade = this.tradeService.findById(id);
            if (trade==null) {
                return new ResponseEntity<>("Wrong id", HttpStatus.BAD_REQUEST);
            }
            if (trade.getOpenedTime()!=null) {
                return new ResponseEntity<>("Failed to open an invalid trade.", HttpStatus.BAD_REQUEST);
            }
            if (trade.getAction().getTimeframe()!=TimeframeEnum.H1) {
                return new ResponseEntity<>("Invalid timeframe in trade.", HttpStatus.BAD_REQUEST);
            }
            trade.setOpenedTime(LocalDateTime.now(ZoneOffset.UTC));
            trade.setCommand(TradeEnum.Wait);
            this.tradeService.save(trade);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @PostMapping("/trade/closed/{id}")
    public ResponseEntity<String> acknowledgeTradeClosed(@PathVariable Long id) {
        try {
            var trade = this.tradeService.findById(id);
            if (trade==null) {
                return new ResponseEntity<>("Wrong id", HttpStatus.BAD_REQUEST);
            }
            if (trade.getOpenedTime()==null || trade.getClosedTime()!=null) {
                return new ResponseEntity<>("Failed to close an invalid trade.", HttpStatus.BAD_REQUEST);
            }
            if (trade.getAction().getTimeframe()!=TimeframeEnum.H1) {
                return new ResponseEntity<>("Invalid timeframe in trade.", HttpStatus.BAD_REQUEST);
            }
            trade.setClosedTime(LocalDateTime.now(ZoneOffset.UTC));
            this.tradeService.save(trade);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    private void requestConfirmation(com.vts.fxdata.entities.Record rec) {
        var pending = getPendingConfirmations(rec.getPair()).stream().filter(c -> c.getTimeframe()==rec.getTimeframe()).findFirst();
        if (pending.isPresent()) {
            var pendingConfirmation = pending.get();
            pendingConfirmation.setRecordId(rec.getId());
            this.confirmationService.save(pendingConfirmation);
        } else {
            this.confirmationService.save(
                    new com.vts.fxdata.entities.Confirmation(rec.getPair(),rec.getTimeframe(),rec.getAction(),
                            rec.getTime(),rec.getId()));
            this.confirmationService.deleteOppositePending(rec);
        }
    }

    private boolean pushNotifications(String msgLine1, String msgLine2) throws PushClientException {
        // TODO add a retry mechanism in case of a failure
        for (Client client:this.clientService.getClients()) {
            var token = client.getToken();
            NotificationServer.send(token, "FxData", msgLine1, msgLine2);
        }
        return true;
    }

    private String buildNotes(com.vts.fxdata.entities.Record confirmedRecord, double levels[], Iterator<Long> iterator) {
        var notesBuilder = new StringBuilder();

        notesBuilder.append("Confirmed record ID: ")
                .append(confirmedRecord.getId())
                .append(" created on ")
                .append(confirmedRecord.getTime().minusMinutes(240))
                .append("\n");

        if (iterator.hasNext()) {
            notesBuilder.append("Other records:\n");
        }

        while(iterator.hasNext()) {
            var otherId = iterator.next();
            var r = this.recordService.getRecordById(otherId).get();
            notesBuilder.append("Deleted record ID: ")
                    .append(otherId)
                    .append(" created on ")
                    .append(r.getTime().minusMinutes(240))
                    .append("\n");
            this.recordService.deleteRecord(otherId);
        }
        notesBuilder.append("Levels: ").append(Arrays.toString(levels));
        return notesBuilder.toString();
    }

    private void updateStateWithNewAction(com.vts.fxdata.entities.Record newAction, double point) throws PushClientException {
        var state = this.stateService.getState(newAction.getPair(), newAction.getTimeframe().ordinal());
        if (state==null) {
            state = new ChartState(newAction.getPair(),newAction.getTimeframe(), newAction.getState());
            state.setTime(newAction.getTime());
            state.setPrice(newAction.getPrice());
        }
        boolean notificationSent = false;

        for(var stateAction : state.getActions()) {
            if (stateAction.getAction() == newAction.getAction()) {
                // we've got another action in the same direction
                // will use the new action, the previous ones will have no end time
                if (!notificationSent) {
                    var message = String.format("Another %s %s %s", newAction.getPair(), newAction.getTimeframe(), newAction.getAction());
                    notificationSent = pushNotifications(message, String.format("previous was (%s) at %s", stateAction.getPrice(), TimeUtils.formatTime(stateAction.getTime())));
                }
            } else {
                stateAction.setEndTime(TimeUtils.removeSeconds(LocalDateTime.now(ZoneOffset.UTC)));
                stateAction.setExitPrice(newAction.getPrice());  // price of the new action is the price we complete the previous action
                                                                // TODO this works for range, what about trend?
                double highPrice, lowPrice;
                if (stateAction.getAction() == ActionEnum.Buy) {
                    lowPrice = stateAction.getPrice();
                    highPrice = newAction.getPrice();   // hopefully higher than the price before
                } else {
                    highPrice = stateAction.getPrice();
                    lowPrice = newAction.getPrice();    // hopefully lower than the price before
                }
                stateAction.setProfit(FxUtils.getPips(highPrice, lowPrice, state.getPoint()));
                // for pairs in Range this would mean to close existing trades and reverse
                // for pairs in a trend we should ignore an action in the opposite direction to the trend
                if (!notificationSent) {
                    var message = String.format("%s %s %s", newAction.getPair(), newAction.getTimeframe(), newAction.getAction());
                    notificationSent = pushNotifications(message, String.format("is replacing %s on %s at %s",
                            stateAction.getAction(), stateAction.getTimeframe(), TimeUtils.formatTime(stateAction.getTime())));
                }
                var trade = this.tradeService.findByRecordId(stateAction.getId());
                if (trade!=null) {
                    if (newAction.getTimeframe()==TimeframeEnum.H1) {
                        // TODO remove and throw if NULL. Handle NULLs only for transition.
                        trade.setCommand(TradeEnum.Close);
                        this.tradeService.save(trade);
                    }
                }
            }
        }
        state.addAction(newAction);
        state.setPoint(point);
        this.stateService.save(state);
        if (newAction.getTimeframe()==TimeframeEnum.H1) {
            this.tradeService.save(new Trade(newAction, TradeEnum.Open));
        }
    }

    private void updateStateMetrics(ChartState state, Heartbeat data) {
        var currPrice = data.getPrice();
        var point = data.getPoint();
        state.setPrice(currPrice);
        state.setPoint(point); // TODO remove, added temporarily to fix old records
        state.setUpdated(TimeUtils.removeSeconds(LocalDateTime.now(ZoneOffset.UTC)));

        for (var action : state.getActions()) {
            int profit = 0;
            var progress = 0;
            var targetPips = action.getTargetPips();;
            var maxDrawdown = action.getMaxDrawdown();
            var minProgress = action.getMinProgress();
            var maxProgress = action.getMaxProgress();
            var actionPrice = action.getPrice();

            switch (action.getAction()) {
                case Buy:
                    profit = FxUtils.getPips(currPrice, actionPrice, point);
                    maxDrawdown = Math.max(-profit, maxDrawdown);
                    progress = FxUtils.getProgress(currPrice, action.getStartPrice(), point, action.getTargetPips());
                    targetPips = FxUtils.getPips(action.getTargetPrice(),action.getStartPrice(),point); // TODO remove
                    break;
                case Sell:
                    profit = FxUtils.getPips(actionPrice, currPrice, point);
                    maxDrawdown = Math.max(-profit, maxDrawdown);
                    progress = FxUtils.getProgress(action.getStartPrice(), currPrice, point, action.getTargetPips());
                    targetPips = FxUtils.getPips(action.getStartPrice(),action.getTargetPrice(),point); // TODO remove
                    break;
            }
            if (progress<minProgress) {
                action.setMinProgress(progress);
            }
            if (progress>maxProgress) {
                action.setMaxProgress(progress);
            }
            action.setProgress(progress);
            action.setTargetPips(targetPips); // TODO remove, added temporarily to fix old records
            action.setProfit(profit);
            action.setMaxDrawdown(maxDrawdown);
        }
        this.stateService.save(state);
    }
}
