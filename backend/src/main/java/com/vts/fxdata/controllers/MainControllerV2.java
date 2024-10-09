package com.vts.fxdata.controllers;

import com.niamedtech.expo.exposerversdk.PushClient;
import com.niamedtech.expo.exposerversdk.PushClientException;
import com.vts.fxdata.entities.ChartState;
import com.vts.fxdata.entities.Client;
import com.vts.fxdata.models.*;
import com.vts.fxdata.models.dto.*;
import com.vts.fxdata.models.dto.Record;
import com.vts.fxdata.notifications.NotificationServer;
import com.vts.fxdata.repositories.ClientService;
import com.vts.fxdata.repositories.ConfirmationService;
import com.vts.fxdata.repositories.RecordService;
import com.vts.fxdata.repositories.StateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

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

    public MainControllerV2(RecordService recordService, ConfirmationService confirmationService, ClientService clientService, StateService stateService, HttpServletRequest request) {
        this.recordService = recordService;
        this.confirmationService = confirmationService;
        this.clientService = clientService;
        this.stateService = stateService;
        this.request = request;
    }

    @PostMapping("/addrecord")
    public ResponseEntity<String> addRecord(@RequestBody Record request) throws PushClientException, InterruptedException {
        try {
            var rec = new com.vts.fxdata.entities.Record(request.getPair(),
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
        com.vts.fxdata.entities.Record confirmedRecord = null;

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
            confirmedRecord = this.recordService.getRecordById(recordId).get();
            notes = buildNotes(confirmedRecord, request.getLevels(), iterator);
            confirmedRecord.setConfirmation(true);
            confirmedRecord.setTime(LocalDateTime.now(ZoneOffset.UTC));
            confirmedRecord.setPrice(request.getPrice());
            confirmedRecord.setStartPrice(request.getLevels()[0]);
            confirmedRecord.setTargetPrice(request.getLevels()[1]);
            confirmedRecord.setNotes(notes);
            this.recordService.save(confirmedRecord);

            // remove record IDs
            confirmation.getRecordIds().clear();
            this.confirmationService.save(confirmation);

            // update the corresponding state
            updateState(confirmedRecord);

        } catch(Exception e) {
            errorMessage = e.getMessage()+"\r\n"+notes;
        }

        if (errorMessage!=null) {
            return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // send out notifications
        String message = String.format("%s %s  (%s)", confirmedRecord.getAction(),
                confirmedRecord.getPair(), confirmedRecord.getPrice());
        pushNotifications(message, confirmedRecord.getTimeframe() + " " + confirmedRecord.getState() +
                " ".repeat(24-confirmedRecord.getConfirmationDelay().length()) + confirmedRecord.getConfirmationDelay());

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

        for(ChartState chState : this.stateService.findAll().stream()
                .filter(s -> s.getPair().equals(request.getPair())).toList())
        {
            chState.setPrice(request.getPrice());
            chState.setUpdated(LocalDateTime.now(ZoneOffset.UTC));
            chState.setPoint(request.getPoint());
            this.stateService.save(chState);
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

    private void pushNotifications(String msgLine1, String msgLine2) throws PushClientException {
        // TODO add a retry mechanism in case of a failure
        for (Client client:this.clientService.getClients()) {
            var token = client.getToken();
            NotificationServer.send(token, "FxData", msgLine1, msgLine2);
        }
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

    private void updateState(com.vts.fxdata.entities.Record confirmedRecord) throws PushClientException {
        var states = this.stateService.getPairStates(confirmedRecord.getPair());
        final var timeframe = confirmedRecord.getTimeframe();
        List<ChartState> tfState = states.stream().filter(st -> st.getTimeframe()==timeframe).collect(Collectors.toList());
        if (tfState.size() > 0) {
            var state = tfState.get(0);
            var existingAction = state.getAction();
            if (existingAction!=null) {
                if (existingAction.getAction() != confirmedRecord.getAction()) {
                    // TODO send notification
                    // for pairs in Range this would mean to close existing trades and reverse
                    // for pairs in a trend we should ignore an action in the opposite direction to the trend
                    String message = String.format("%s %s %s", confirmedRecord.getPair(), confirmedRecord.getTimeframe(), confirmedRecord.getAction());
                    pushNotifications(message,  String.format("is replacing %s on %s at %s", existingAction.getAction(), confirmedRecord.getTimeframe(), confirmedRecord.getTime()));
                }
            }
            state.setAction(confirmedRecord);
            this.stateService.save(state);
        } else {
            new ResponseEntity<>("Failed to link record and state.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
