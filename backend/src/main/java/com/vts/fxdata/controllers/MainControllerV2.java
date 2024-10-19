package com.vts.fxdata.controllers;

import com.niamedtech.expo.exposerversdk.PushClient;
import com.niamedtech.expo.exposerversdk.PushClientException;
import com.vts.fxdata.entities.ChartState;
import com.vts.fxdata.entities.Client;
import com.vts.fxdata.entities.Record;
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
    private final ClientService clientService;

    @Autowired
    private final HttpServletRequest request;

    public MainControllerV2(RecordService recordService,
                            ConfirmationService confirmationService,
                            ClientService clientService,
                            StateService stateService,
                            HttpServletRequest request) {
        this.recordService = recordService;
        this.confirmationService = confirmationService;
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
            notes = buildNotes(rec, request.getLevels(), iterator);
            rec.setConfirmation(true);
            rec.setPrice(request.getPrice());
            rec.setStartPrice(request.getLevels()[0]);
            rec.setTargetPrice(request.getLevels()[1]);
            rec.setNotes(notes);

            var targetPips = FxUtils.getPips(rec.getTargetPrice(),rec.getStartPrice(),request.getPoint());
            if (rec.getAction()==ActionEnum.Sell) {
                targetPips = FxUtils.getPips(rec.getStartPrice(),rec.getTargetPrice(),request.getPoint());
            }
            rec.setTargetPips(targetPips);

            var progress = FxUtils.getProgress(request.getPrice(), rec.getStartPrice(), request.getPoint(), targetPips);
            if (rec.getAction()==ActionEnum.Sell) {
                progress = FxUtils.getProgress(rec.getStartPrice(), request.getPrice(), request.getPoint(), targetPips);
            }
            rec.setProgress(progress);
            rec.setMinProgress(progress);
            rec.setMaxProgress(progress);
            this.recordService.save(rec);

            // remove record IDs
            confirmation.getRecordIds().clear();
            this.confirmationService.save(confirmation);

            // update the corresponding state
            updateStateWithNewAction(rec, request.getPoint());

        } catch(Exception e) {
            errorMessage = e.getMessage()+"\r\n"+notes;
        }

        if (errorMessage!=null) {
            return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // send out notifications
        String message = String.format("%s %s  (%s)", rec.getAction(),
                rec.getPair(), rec.getPrice());
        pushNotifications(message, rec.getTimeframe() + " " + rec.getState() +
                " ".repeat(24-rec.getConfirmationDelay().length()) + rec.getConfirmationDelay());

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
    public List<Pair> getStates(@RequestParam(value = "state", required = false) StateEnum state, TimeZone timezone) {
        return this.stateService.getLastStates(state);
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
    public List<Result> getResults(TimeZone timezone) {
        var results = new ArrayList<Result>();
        for(var r : this.recordService.getResultRecords()) {
            results.add(new Result(
                    r.getPair(),
                    r.getTimeframe().toString(),
                    r.getAction().toString(),
                    r.getTargetPips(),
                    r.getProfit(),
                    r.getMaxDrawdown(),
                    r.getMinProgress(),
                    r.getMaxProgress(),
                    r.getStartTime().toString(),
                    r.getEndTime().toString()));
        }
        return results;
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
                    notificationSent = pushNotifications(message, String.format("previous was (%s) at %s", stateAction.getPrice(), stateAction.getTime()));
                }
            } else {
                stateAction.setEndTime(TimeUtils.removeSeconds(LocalDateTime.now(ZoneOffset.UTC)));
                stateAction.setProfit(FxUtils.getPips(stateAction.getPrice(), newAction.getPrice(), state.getPoint()));
                // for pairs in Range this would mean to close existing trades and reverse
                // for pairs in a trend we should ignore an action in the opposite direction to the trend
                if (!notificationSent) {
                    var message = String.format("%s %s %s", newAction.getPair(), newAction.getTimeframe(), newAction.getAction());
                    notificationSent = pushNotifications(message, String.format("is replacing %s on %s at %s",
                            stateAction.getAction(), stateAction.getTimeframe(), stateAction.getTime()));
                }
            }
        }
        state.addAction(newAction);
        state.setPoint(point);
        this.stateService.save(state);
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
