package com.vts.fxdata.controllers;

import com.niamedtech.expo.exposerversdk.PushClient;
import com.niamedtech.expo.exposerversdk.PushClientException;
import com.vts.fxdata.entities.TfState;
import com.vts.fxdata.entities.Client;
import com.vts.fxdata.entities.Record;
import com.vts.fxdata.entities.Trade;
import com.vts.fxdata.models.*;
import com.vts.fxdata.models.dto.*;
import com.vts.fxdata.notifications.NotificationServer;
import com.vts.fxdata.repositories.*;
import com.vts.fxdata.utils.FxUtils;
import com.vts.fxdata.utils.TimeUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@RestController
@RequestMapping("api/v2/fxdata")
@CrossOrigin(origins = "http://localhost:8080")
public class MainControllerV2 {
    private static final Logger log = LoggerFactory.getLogger(MainControllerV2.class);

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
        var dto = ToStringBuilder.reflectionToString(request, ToStringStyle.JSON_STYLE);
        log.info(String.format("/addrecord body=%s",dto));

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
        if (pending==null) {
            return new ResponseEntity<>("Confirmation not found", HttpStatus.NOT_FOUND);
        }
        var dto = ToStringBuilder.reflectionToString(request, ToStringStyle.JSON_STYLE);
        log.info(String.format("/confirmed body=%s",dto));

        // mark the newest record as confirmed and delete the rest
        com.vts.fxdata.entities.Record rec = null;
        var unconfirmedRecordIds = pending.getRecordIds();
        Collections.reverse(unconfirmedRecordIds);
        var iterator = unconfirmedRecordIds.iterator();
        String errorMessage = null;
        String notes = null;
        log.info("/confirmed, id="+request.getId()+", unconfirmedRecordIds size="+unconfirmedRecordIds.size());
        try {
            if (!iterator.hasNext()) {
                log.info("/confirmed, id="+request.getId()+", No records found");
                return new ResponseEntity<>("No records found", HttpStatus.NOT_FOUND);
            }

            while (rec==null && iterator.hasNext()) {
                var recordId = iterator.next();
                log.info("/confirmed, id="+request.getId()+", recordId="+recordId);
                rec = this.recordService.getRecordById(recordId);
                if (rec==null) {
                    log.info("/confirmed, id=" + request.getId() + ", missing record skipped");
                }
            }
            if (rec==null) {
                log.info("/confirmed, id="+request.getId()+", all records are missing, signal ignored");
                pending.getRecordIds().clear();
                log.info("/confirmed, id="+request.getId()+", cleared confirmation record");
                this.confirmationService.deleteConfirmation(request.getId());
                return new ResponseEntity<>(String.format("Confirmation for a missing record ignored. id=",request.getId()), HttpStatus.NOT_FOUND);
            }
            log.info("/confirmed, id="+request.getId()+", record retrieved");

            // check if confirmation does not clash with a trend in this timeframe
            var state = this.stateService.getState(rec.getPair(), rec.getTimeframe().ordinal());
            // TODO check the strength of the trend. Must be older than 150 candles to trust it
            // TODO and ignore opposite signals
            if (state.getState() == StateEnum.Bullish && rec.getAction() == ActionEnum.Sell ||
                state.getState() == StateEnum.Bearish && rec.getAction() == ActionEnum.Buy) {
                // ignore sells in a bullish trend
                // ignore buys in a bearish trend
                log.info("/confirmed, id="+request.getId()+", ignoring sells or buys, deleting unconfirmed records");
                // delete all records that signal to act against the trend
                this.recordService.deleteAll(unconfirmedRecordIds.iterator());//deleteRecord(recordId);
            } else {
                log.info("/confirmed, id="+request.getId()+", calculate targets and pips");
                var startPrice = request.getLevels()[0];
                var targetPrice = request.getLevels()[1];

                var targetPips = FxUtils.getPips(targetPrice, request.getPrice(), request.getPoint());
                var missedPips = FxUtils.getPips(request.getPrice(), startPrice, request.getPoint());
                if (rec.getAction() == ActionEnum.Sell) {
                    targetPips = FxUtils.getPips(request.getPrice(), targetPrice, request.getPoint());
                    missedPips = FxUtils.getPips(startPrice, request.getPrice(), request.getPoint());
                }
                log.info("/confirmed, id="+request.getId()+", targetPips="+targetPips);
                // ignore small waves and missed opportunities (missed pips must be 33% or less)
                if (targetPips<40 || missedPips>targetPips/2) {
                    log.info(String.format("Ignoring action %s on %s due to low risk/reward ratio", rec.getAction().toString(), rec.getPair()));
                    this.recordService.deleteAll(unconfirmedRecordIds.iterator());
                    return new ResponseEntity<>(null, HttpStatus.OK);
                }

                final var pair = rec.getPair();
                final var price = rec.getPrice();
                var openTrades = this.tradeService.getTrades().stream().filter(t -> t.getAction().getPair().equals(pair)).toList();
                var nearbyTradesCount = openTrades.stream().filter(t -> Math.abs(FxUtils.getPips(t.getAction().getPrice(), price, request.getPoint()))<50).count();
                log.info("/confirmed, id="+request.getId()+", nearbyTradesCount="+nearbyTradesCount);
                // don't open new trades if there's an existing one within 50 pips
                if (nearbyTradesCount>0) {
                    log.info(String.format("Ignoring action %s on %s due to an existing trade nearby.", rec.getAction().toString(), rec.getPair()));
                    this.recordService.deleteAll(unconfirmedRecordIds.iterator());
                    return new ResponseEntity<>(null, HttpStatus.OK);
                }

                rec.setTargetPips(targetPips);
                rec.setConfirmation(true);
                rec.setPrice(request.getPrice());
                rec.setStartPrice(startPrice);
                rec.setTargetPrice(targetPrice);
                rec.setTime(LocalDateTime.now(ZoneOffset.UTC));
                rec.setState(state.getState());     // update the state to the current one
                notes = buildNotes(rec, request.getLevels(), iterator);
                rec.setNotes(notes);
                log.info("/confirmed, id="+request.getId()+", built notes="+notes);
                var progress = FxUtils.getProgress(request.getPrice(), rec.getStartPrice(), request.getPoint(), targetPips);
                if (rec.getAction() == ActionEnum.Sell) {
                    progress = FxUtils.getProgress(rec.getStartPrice(), request.getPrice(), request.getPoint(), targetPips);
                }
                rec.setProgress(progress);
                rec.setMinProgress(progress);
                rec.setMaxProgress(progress);
                this.recordService.save(rec);
                log.info("/confirmed, id="+request.getId()+", saved new record");
                // update the corresponding state
                updateStateWithNewAction(state, rec, request.getPoint());
                log.info("/confirmed, id="+request.getId()+", update state with wew action");
            }
            // remove record IDs
            pending.getRecordIds().clear();
            log.info("/confirmed, id="+request.getId()+", cleared confirmation record");
            this.confirmationService.save(pending);



        } catch(Exception e) {
            var sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            errorMessage = sw+"\r\n"+notes;
            log.error("Confirmed handler failed: ",e);
            if (rec!=null) {
                rec.setNotes(notes+"\r\n"+sw);
                this.recordService.save(rec);
            }
            pushNotifications(e.getMessage(), notes);
        }

        if (errorMessage!=null) {
            return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // send out notifications
        int count = rec.getConfirmationDelay().isEmpty() ? 0: rec.getConfirmationDelay().length();
        String message = String.format("%s %s  (%s)", rec.getAction(),
                rec.getPair(), rec.getPrice());
        pushNotifications(message, rec.getTimeframe() + " " + rec.getState() +
                " ".repeat(24-count) + rec.getConfirmationDelay());

        this.confirmationService.deleteConfirmation(request.getId());
        log.info(String.format("Added a new action: %s on %s.", rec.getAction().toString(), rec.getPair()));
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @GetMapping("/pending/{pair}")
    public List<com.vts.fxdata.entities.Confirmation> getPendingConfirmations(@PathVariable String pair) {
        return this.confirmationService.getPendingConfirmations(pair);
    }

    @PostMapping("/state")
    public ResponseEntity<String> setChartState(@RequestBody State state) {
        try {
            this.stateService.setState(TfState.newInstance(state));
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

        // manage open trades
        final var pair = request.getPair();
        var openTrades = this.tradeService.getTrades().stream().filter(t -> t.getAction().getPair().equals(pair)).toList();
        handleDoublingDown(openTrades, request.getPrice(), request.getPoint());
        handleWrongSideOfTrend(openTrades, request.getPrice(), request.getPoint());

        for(TfState state : this.stateService.getStates(request.getPair())) {
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

    @GetMapping("/trades/all")
    public List<com.vts.fxdata.models.dto.Trade> getAllTrades(@RequestParam(name="tzo", required = false) Integer tzOffset) {
        var trades = this.tradeService.getTrades();
        if (trades==null) trades = new ArrayList<>();
        return TradesView.getTrades(false, trades, tzOffset==null ? 0: tzOffset);
    }

    @GetMapping("/trades/active")
    public List<com.vts.fxdata.models.dto.Trade> getActiveTrades() {
        var trades = this.tradeService.getTrades();
        if (trades==null) trades = new ArrayList<>();
        return TradesView.getTrades(true, trades, 0);
    }

    @PostMapping("/trade/close")
    public ResponseEntity<String> closeTrade(@RequestBody TradeAck tradeAck) {
        if (tradeAck==null || tradeAck.getId()<=0) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
        var dto = ToStringBuilder.reflectionToString(tradeAck, ToStringStyle.JSON_STYLE);
        log.info(String.format("/trade/close, body=%s",dto));
        try {
            var trade = this.tradeService.findById(tradeAck.getId());
            if (trade==null) {
                return new ResponseEntity<>("Invalid id", HttpStatus.BAD_REQUEST);
            }
            if (trade.getOpenedTime()==null || trade.getClosedTime()!=null) {
                return new ResponseEntity<>("Failed to close an invalid trade.", HttpStatus.BAD_REQUEST);
            }
            var action = trade.getAction();
            if (action.getTimeframe()!=TimeframeEnum.H1) {
                return new ResponseEntity<>("Invalid timeframe in trade.", HttpStatus.BAD_REQUEST);
            }
            var state = this.stateService.getState(action.getPair(), TimeframeEnum.H1.ordinal());
            if (state.getActions().remove(trade.getAction())) {
                this.stateService.save(state);
            } else {
                throw new RuntimeException(String.format("Trade to close references an action that is missing in the state. Trade ID=%d",tradeAck.getId()));
            }
            action.setEndTime(LocalDateTime.now(ZoneOffset.UTC));
            action.setExitPrice(state.getPrice());
            trade.setCommand(TradeEnum.Close);
            this.tradeService.save(trade);
            log.info(String.format("Closing trade with id=%d pair=%s, action=%s",tradeAck.getId(),trade.getAction().getPair(),trade.getAction().getAction()));
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>("closed", HttpStatus.OK);
    }

    @PostMapping("/trade/opened")
    public ResponseEntity<String> acknowledgeTradeOpened(@RequestBody TradeAck tradeAck) {
        if (tradeAck==null || tradeAck.getId()<=0 || tradeAck.getPrice()==null || tradeAck.getPrice()<0) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
        var dto = ToStringBuilder.reflectionToString(tradeAck, ToStringStyle.JSON_STYLE);
        log.info(String.format("/trade/opened, body=%s",dto));
        try {
            var trade = this.tradeService.findById(tradeAck.getId());
            if (trade==null) {
                return new ResponseEntity<>("Invalid id", HttpStatus.BAD_REQUEST);
            }
            if (trade.getOpenedTime()!=null) {
                return new ResponseEntity<>("Failed to open an invalid trade.", HttpStatus.BAD_REQUEST);
            }
            if (trade.getAction().getTimeframe()!=TimeframeEnum.H1) {
                return new ResponseEntity<>("Invalid timeframe in trade.", HttpStatus.BAD_REQUEST);
            }
            trade.setOpenedTime(LocalDateTime.now(ZoneOffset.UTC));
            trade.getAction().setPrice(tradeAck.getPrice());
            trade.setCommand(TradeEnum.Wait);
            this.tradeService.save(trade);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @PostMapping("/trade/closed")
    public ResponseEntity<String> acknowledgeTradeClosed(@RequestBody TradeAck tradeAck) {
        if (tradeAck==null || tradeAck.getId()<=0 || tradeAck.getPrice()==null || tradeAck.getPrice()<0) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
        var dto = ToStringBuilder.reflectionToString(tradeAck, ToStringStyle.JSON_STYLE);
        log.info(String.format("/trade/close, body=%s",dto));
        try {
            var trade = this.tradeService.findById(tradeAck.getId());
            if (trade==null) {
                return new ResponseEntity<>("Invalid id", HttpStatus.BAD_REQUEST);
            }
            if (trade.getOpenedTime()==null || trade.getClosedTime()!=null) {
                return new ResponseEntity<>("Failed to close an invalid trade.", HttpStatus.BAD_REQUEST);
            }
            if (trade.getAction().getTimeframe()!=TimeframeEnum.H1) {
                return new ResponseEntity<>("Invalid timeframe in trade.", HttpStatus.BAD_REQUEST);
            }
            var closedTime = LocalDateTime.now(ZoneOffset.UTC);
            trade.setClosedTime(closedTime);
            trade.getAction().setEndTime(closedTime);
            trade.getAction().setExitPrice(tradeAck.getPrice());
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
            var rec = this.recordService.getRecordById(otherId);
            if (rec==null) continue;
            notesBuilder.append("Deleted record ID: ")
                    .append(otherId)
                    .append(" created on ")
                    .append(rec.getTime().minusMinutes(240))
                    .append("\n");
            this.recordService.deleteRecord(otherId);
        }
        notesBuilder.append("Levels: ").append(Arrays.toString(levels));
        return notesBuilder.toString();
    }

    private void updateStateWithNewAction(TfState state, com.vts.fxdata.entities.Record newAction, double point) throws PushClientException {
        boolean notificationSent = false;
        log.info("/confirmed/updateStateWithNewAction, recordId="+newAction.getId()+", state has actions: "+state.getActions().size());
        for(var stateAction : state.getActions()) {
            log.info("    /confirmed/updateStateWithNewAction, actionId="+newAction.getId());
            if (stateAction.getAction() == newAction.getAction()) {
                log.info("    /confirmed/updateStateWithNewAction, actionId="+newAction.getId()+", another action in the same direction");
                // we've got another action in the same direction
                // will use the new action, the previous ones will have no end time
                if (!notificationSent) {
                    var message = String.format("Another %s %s %s", newAction.getPair(), newAction.getTimeframe(), newAction.getAction());
                    notificationSent = pushNotifications(message, String.format("previous was (%s) at %s", stateAction.getPrice(), TimeUtils.formatTime(stateAction.getTime())));
                }
            } else {
                log.info("    /confirmed/updateStateWithNewAction, actionId="+newAction.getId()+", opposite direction, closing existing action");
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
                // close existing trade
                var trade = this.tradeService.findByRecordId(stateAction.getId());
                if (trade != null) { // trades for actions from H4 won't be found
                    log.info("    /confirmed/updateStateWithNewAction, actionId="+newAction.getId()+", opposite direction, found Trade for existing action, issuing Close command");
                    trade.setCommand(TradeEnum.Close);
                    this.tradeService.save(trade);
                }
            }
        }
        log.info("    /confirmed/updateStateWithNewAction, actionId="+newAction.getId()+", clearing existing state actions, adding the new one");
        // set new action
        state.getActions().clear();
        state.addAction(newAction);
        state.setPoint(point);
        this.stateService.save(state);
        log.info("    /confirmed/updateStateWithNewAction, actionId="+newAction.getId()+", saved state");
        if (newAction.getTimeframe()==TimeframeEnum.H1) {
            this.tradeService.save(new Trade(newAction, TradeEnum.Open));
            log.info("    /confirmed/updateStateWithNewAction, actionId="+newAction.getId()+", issued a new Open trade command");
        }
    }

    private void updateStateMetrics(TfState state, Heartbeat data) {
        var currPrice = data.getPrice();
        var point = data.getPoint();
        state.setPrice(currPrice);
        state.setPoint(point); // TODO remove, added temporarily to fix old records
        state.setUpdated(TimeUtils.removeSeconds(LocalDateTime.now(ZoneOffset.UTC)));

        for (var action : state.getActions()) {
           updateActionMetrics(action, currPrice, point);
        }
        this.stateService.save(state);
    }

    private void updateActionMetrics(Record action, double currPrice, double point) {
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
                targetPips = FxUtils.getPips(action.getTargetPrice(),action.getPrice(),point);
                progress = FxUtils.getProgress(currPrice, action.getStartPrice(), point, targetPips);
                break;
            case Sell:
                profit = FxUtils.getPips(actionPrice, currPrice, point);
                maxDrawdown = Math.max(-profit, maxDrawdown);
                targetPips = FxUtils.getPips(action.getPrice(),action.getTargetPrice(), point);
                progress = FxUtils.getProgress(action.getStartPrice(), currPrice, point, targetPips);
                break;
        }
        if (progress<minProgress) {
            minProgress=progress;
        }
        if (progress>maxProgress) {
            maxProgress=progress;
        }
        action.setProgress(progress);
        action.setTargetPips(targetPips); // TODO remove, added temporarily to fix old records
        action.setProfit(profit);
        action.setMaxDrawdown(maxDrawdown);
        action.setMinProgress(minProgress);
        action.setMaxProgress(maxProgress);
    }

    private void handleDoublingDown(List<Trade> openTrades, double price, double point) {
        var lt = openTrades.stream().filter(t -> t.getAction() != null)
                .max(Comparator.comparingInt(t -> t.getAction().getProfit()));
        if (lt.isPresent() && lt.get().getAction().getProfit() < -50) {
            // double down
            var rec = lt.get().getAction();
            var newRec = new Record(rec.getPair(), rec.getTimeframe(), rec.getAction(), rec.getState(), price, true);
            newRec.setStartPrice(rec.getStartPrice());
            newRec.setStartTime(LocalDateTime.now(ZoneOffset.UTC));
            newRec.setTargetPrice(rec.getTargetPrice());
            updateActionMetrics(newRec, price, point);

            this.recordService.saveAndFlush(newRec);
            this.tradeService.save(new Trade(newRec, TradeEnum.Open));
            var state = this.stateService.getState(rec.getPair(), rec.getTimeframe().ordinal());
            if ( state!=null ) {
                state.getActions().add(newRec);
                this.stateService.saveAndFlush(state);
            }
            log.info(String.format("Added a new action: %s on %s to double down.", rec.getAction().toString(), rec.getPair()));
        }
    }

    private void handleWrongSideOfTrend(List<Trade> openTrades, double price, double point) {
        // TODO var bearishTrades = openTrades.stream().filter(t -> t.getAction() != null && t.getAction().getAction()==ActionEnum.Sell);
    }
}
