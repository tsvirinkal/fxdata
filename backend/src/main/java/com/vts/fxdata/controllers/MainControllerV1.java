package com.vts.fxdata.controllers;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.vts.fxdata.entities.ChartState;
import com.vts.fxdata.entities.Client;
import com.vts.fxdata.models.*;
import com.vts.fxdata.models.Record;
import com.vts.fxdata.notifications.NotificationServer;
import com.vts.fxdata.repositories.ClientService;
import com.vts.fxdata.repositories.ConfirmationService;
import com.vts.fxdata.repositories.RecordService;
import com.niamedtech.expo.exposerversdk.PushClient;
import com.niamedtech.expo.exposerversdk.PushClientException;
import com.vts.fxdata.repositories.StateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("api/v1/fxdata")
@CrossOrigin(origins = "http://localhost:8080")
public class MainControllerV1 {

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

    public MainControllerV1(RecordService recordService, ConfirmationService confirmationService, ClientService clientService, StateService stateService, HttpServletRequest request) {
        this.recordService = recordService;
        this.confirmationService = confirmationService;
        this.clientService = clientService;
        this.stateService = stateService;
        this.request = request;
    }

    @PostMapping("/addrecord")
    public void addRecord(HttpServletRequest httpRequest, @RequestBody Record request, TimeZone timezone) throws PushClientException, InterruptedException {
        var rec = new com.vts.fxdata.entities.Record(request.getPair(),
                Timeframe.valueOf(request.getTimeframe()),
                Action.valueOf(request.getAction()),
                StateEnum.valueOf(request.getState()),
                request.getPrice(),
                false);
        this.recordService.addRecord(rec);

        var data = new HashMap<String, Object>();
        data.put("pair", rec.getPair());
        data.put("action", rec.getAction());
        data.put("timeframe", rec.getTimeframe());
        data.put("state", rec.getState());
        data.put("price", rec.getPrice());
        data.put("id", rec.getId());

        // send out notifications
        String message = String.format("%s %s  (%s)",rec.getAction(),rec.getPair(),rec.getPrice());
        pushNotifications(message,rec.getTimeframe()+" "+rec.getState(), data);
    }

    @GetMapping("/")
    public List<DayRecords> getLastRecords(@RequestParam(name="tzo", required = false) Integer tzOffset) {
        return recordService.getLastRecords(tzOffset==null ? 0:tzOffset.intValue());
    }

    @GetMapping("/{pair}")
    public List<DayRecords> getLastRecords(
            @PathVariable String pair, @RequestParam(name="tzo", required = false) Integer tzOffset) {

        return this.recordService.getLastRecords(pair, tzOffset==null ? 0:tzOffset.intValue());
    }

    @PostMapping("/delete/{recordId}")
    public void deleteRecord(@PathVariable Long recordId) {
        this.recordService.deleteRecord(recordId);
    }

    @PostMapping("/confirm/{recordId}")
    public void requestConfirmation(@PathVariable Long recordId) {
        var record = this.recordService.getRecordById(recordId);
        if (record.isPresent()) {
            var rec = record.get();
            var pending = getPendingConfirmations(rec.getPair()).stream().filter(c -> c.getTimeframe()==rec.getTimeframe()).findFirst();
            if (pending.isPresent()) {
                var pendingConfirmation = pending.get();
                pendingConfirmation.getRecordIds().add(recordId);
                this.confirmationService.requestConfirmation(pendingConfirmation);
            } else {
                this.confirmationService.requestConfirmation(
                        new com.vts.fxdata.entities.Confirmation(rec.getPair(),rec.getTimeframe(),rec.getAction(),
                                rec.getTime(),rec.getId()));
            }
        }
    }

    @PostMapping("/confirmed")
    public void confirmationFound(@RequestBody Confirmation request) throws PushClientException, InterruptedException {

        var pending = this.confirmationService.findById(request.getId());
        if (pending.isPresent()) {
            var confirmation = pending.get();
            confirmation.getRecordIds().forEach( recordId ->
            {
                // mark record confirmed
                var record = this.recordService.getRecordById(recordId);
                if (record.isPresent()) {
                    var rec = record.get();
                    rec.setConfirmation(true);
                    this.recordService.saveAndFlush(rec);
                }
            });

            var data = new HashMap<String, Object>();
            data.put("id", request.getRecordId());
            // send out one notification per confirmation
            pushNotifications("Confirmation found for", confirmation.getAction() + " " + confirmation.getPair() + "," + confirmation.getTimeframe(), data);

            this.confirmationService.deleteConfirmation(request.getId());
        }
    }

    @GetMapping("/pending/{pair}")
    public List<com.vts.fxdata.entities.Confirmation> getPendingConfirmations(@PathVariable String pair) {
        return this.confirmationService.getPendingConfirmations(pair);
    }

    @PostMapping("/state")
    public void setChartState(@RequestBody State request, TimeZone timezone) throws PushClientException, InterruptedException {
        var state = new ChartState(request.getPair(),
                Timeframe.valueOf(request.getTimeframe()),
                StateEnum.valueOf(request.getState()));

        if (this.stateService.setState(state)) {
            // new state, send notification

            var data = new HashMap<String, Object>();
            data.put("pair", state.getPair());
            data.put("timeframe", state.getTimeframe());
            data.put("state", state.getState());

            // send out notifications
            String message = String.format("%s on %s %s", state.getState(), state.getPair(), state.getTimeframe());
            pushNotifications(message, "", data);
        }
    }

    @GetMapping("/states/")
    public StatesView getStates(@RequestParam(value = "state", required = false) StateEnum state, TimeZone timezone) {
        return stateService.getLastStates(state);
    }

    @PostMapping("/addclient")
    public void addClient(@RequestBody ExpoTokenRequest request) {
        if (!PushClient.isExponentPushToken(request.getToken()))
            return;

        try {
            this.clientService.addClient(new Client(request.getToken()));
        } catch(Exception e) {
            System.out.println(e);
        }
    }

    private void pushNotifications(String msgLine1, String msgLine2, Map<String, Object> data) throws PushClientException, InterruptedException {
        // TODO add a retry mechanism in case of a failure
        for (Client client:this.clientService.getClients()) {
            var token = client.getToken();
            NotificationServer.send(token, "FxData", msgLine1, msgLine2, data);
        }
    }
}
