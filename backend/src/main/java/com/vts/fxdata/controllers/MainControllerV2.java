package com.vts.fxdata.controllers;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.niamedtech.expo.exposerversdk.PushClient;
import com.niamedtech.expo.exposerversdk.PushClientException;
import com.vts.fxdata.entities.ChartState;
import com.vts.fxdata.entities.Client;
import com.vts.fxdata.models.*;
import com.vts.fxdata.models.Record;
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
import java.util.List;
import java.util.TimeZone;

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
    JsonMappingException d;
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
                    Timeframe.valueOf(request.getTimeframe()),
                    Action.valueOf(request.getAction()),
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

        var confirmation = pending.get();
        var unconfirmedRecordIds = confirmation.getRecordIds();
        if (unconfirmedRecordIds.size() == 0) {
            return new ResponseEntity<>("Record ID not found", HttpStatus.NOT_FOUND);
        }

        // mark all unconfirmed records confirmed
        com.vts.fxdata.entities.Record record = null;
        for (Long recordId : unconfirmedRecordIds) {
            var optRecord = this.recordService.getRecordById(recordId);
            if (optRecord.isEmpty()) continue;

            record = optRecord.get();
            record.setConfirmation(true);
            record.setTime(LocalDateTime.now(ZoneOffset.UTC));
            record.setConfirmationDelay(LocalDateTime.now(ZoneOffset.UTC));
            if (request.getPrice()>0) {
                record.setPrice(request.getPrice());
            }
            this.recordService.saveAndFlush(record);
        }

        // send out notifications
        String message = String.format("%s %s  (%s)", record.getAction(), record.getPair(), record.getPrice());
        pushNotifications(message, record.getTimeframe() + " " + record.getState() + "              " + record.getConfirmationDelay());

        this.confirmationService.deleteConfirmation(request.getId());
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @GetMapping("/pending/{pair}")
    public List<com.vts.fxdata.entities.Confirmation> getPendingConfirmations(@PathVariable String pair) {
        return this.confirmationService.getPendingConfirmations(pair);
    }

    @PostMapping("/state")
    public ResponseEntity<String> setChartState(@RequestBody State request, TimeZone timezone) throws PushClientException, InterruptedException {
        try {
            var state = new ChartState(request.getPair(),
                    Timeframe.valueOf(request.getTimeframe()),
                    StateEnum.valueOf(request.getState()));

            if (this.stateService.setState(state)) {
                // new state, send notification
                String message = String.format("%s on %s %s", state.getState(), state.getPair(), state.getTimeframe());
                pushNotifications(message, "");
            }
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @GetMapping("/states/")
    public StatesView getStates(@RequestParam(value = "state", required = false) StateEnum state, TimeZone timezone) {
        return stateService.getLastStates(state);
    }

    @PostMapping("/addclient")
    @Deprecated
    public void addClient(@RequestBody ExpoTokenRequest request) {
        if (!PushClient.isExponentPushToken(request.getToken()))
            return;

        try {
            this.clientService.addClient(new Client(request.getToken()));
        } catch(DataIntegrityViolationException e) {
            System.out.println(e);
        }
    }

    private void requestConfirmation(com.vts.fxdata.entities.Record rec) {
        var pending = getPendingConfirmations(rec.getPair()).stream().filter(c -> c.getTimeframe()==rec.getTimeframe()).findFirst();
        if (pending.isPresent()) {
            var pendingConfirmation = pending.get();
            pendingConfirmation.setRecordId(rec.getId());
            this.confirmationService.saveConfirmation(pendingConfirmation);
        } else {
            this.confirmationService.saveConfirmation(
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
}
