package com.vts.fxdata.controllers;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.niamedtech.expo.exposerversdk.PushClient;
import com.niamedtech.expo.exposerversdk.PushClientException;
import com.vts.fxdata.entities.ChartState;
import com.vts.fxdata.entities.Client;
import com.vts.fxdata.entities.Confirmation;
import com.vts.fxdata.entities.Record;
import com.vts.fxdata.models.*;
import com.vts.fxdata.notifications.NotificationServer;
import com.vts.fxdata.repositories.ClientService;
import com.vts.fxdata.repositories.ConfirmationService;
import com.vts.fxdata.repositories.RecordService;
import com.vts.fxdata.repositories.StateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public void addRecord(@RequestBody RecordRequest request) throws PushClientException, InterruptedException {
        var rec = new Record(request.getPair(),
                Timeframe.valueOf(request.getTimeframe()),
                Action.valueOf(request.getAction()),
                State.valueOf(request.getState()),
                request.getPrice(),
                false);
        this.recordService.addRecord(rec);

        requestConfirmation(rec);
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
    public void deleteRecord(@PathVariable Long recordId) {
        this.recordService.deleteRecord(recordId);
    }

    @PostMapping("/confirmed")
    public void confirmationFound(@RequestBody ConfirmationRequest request) throws PushClientException, InterruptedException {

        var pending = this.confirmationService.findById(request.getId());
        if (pending.isPresent()) {
            var confirmation = pending.get();
            var rec = confirmation.getRecordIds().stream().findFirst();
            if (rec.isPresent())
            {
                // mark record confirmed
                var record = this.recordService.getRecordById(rec.get().longValue());
                if (record.isPresent()) {
                    var rec2 = record.get();
                    rec2.setConfirmation(true);
                    rec2.setTime(LocalDateTime.now());
                    if (request.getPrice()>0) rec2.setPrice(request.getPrice());
                    this.recordService.saveAndFlush(rec2);

                    var data = new HashMap<String, Object>();
                    data.put("pair", rec2.getPair());
                    data.put("action", rec2.getAction());
                    data.put("timeframe", rec2.getTimeframe());
                    data.put("state", rec2.getState());
                    data.put("price", request.getPrice());
                    data.put("id", rec2.getId());

                    // send out notifications
                    String message = String.format("%s %s  (%s)",rec2.getAction(),rec2.getPair(),rec2.getPrice());
                    pushNotifications(message,rec2.getTimeframe()+" "+rec2.getState(), data);
                }
            }

            var data = new HashMap<String, Object>();
            data.put("id", request.getRecordId());
            // send out one notification per confirmation
           // pushNotifications("Confirmation found for", confirmation.getAction() + " " + confirmation.getPair() + "," + confirmation.getTimeframe(), data);

            this.confirmationService.deleteConfirmation(request.getId());
        }
    }

    @GetMapping("/pending/{pair}")
    public List<Confirmation> getPendingConfirmations(@PathVariable String pair) {
        return this.confirmationService.getPendingConfirmations(pair);
    }

    @PostMapping("/state")
    public void setChartState(@RequestBody StateRequest request, TimeZone timezone) throws PushClientException, InterruptedException {
        var state = new ChartState(request.getPair(),
                Timeframe.valueOf(request.getTimeframe()),
                State.valueOf(request.getState()));

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
    public StatesView getStates(@RequestParam(value = "state", required = false)  State state, TimeZone timezone) {
        return stateService.getLastStates(state);
    }

    @PostMapping("/addclient")
    public void addClient(@RequestBody ExpoTokenRequest request) {
        if (!PushClient.isExponentPushToken(request.getToken()))
            return;

        try {
            this.clientService.addClient(new Client(request.getToken()));
        } catch(DataIntegrityViolationException e) {
            System.out.println(e);
        }
    }

    private void requestConfirmation(Record rec) {
        var pending = getPendingConfirmations(rec.getPair()).stream().filter(c -> c.getTimeframe()==rec.getTimeframe()).findFirst();
        if (!pending.isPresent()) {
            this.confirmationService.requestConfirmation(
                    new Confirmation(rec.getPair(),rec.getTimeframe(),rec.getAction(),
                            rec.getTime(),rec.getId()));
            this.confirmationService.deleteOppositePending(rec);
        }
    }

    private void pushNotifications(String msgLine1, String msgLine2, Map<String, Object> data) throws PushClientException, InterruptedException {
        // TODO add a retry mechanism in case of a failure
        for (Client client:this.clientService.getClients()) {
            var token = client.getToken();
            NotificationServer.send(token, "Forex Retriever", msgLine1, msgLine2, data);
        }
    }
}
