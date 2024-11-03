package com.vts.fxdata.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vts.fxdata.configuration.WebConfig;
import com.vts.fxdata.entities.TfState;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DayStates {

    private long id;

    @JsonFormat(pattern = WebConfig.DATE_PATTERN)
    private LocalDate date;
    private List<TfState> states;

    public DayStates() {
        states = new ArrayList<TfState>();
    }

    public DayStates(LocalDate date, List<TfState> states) {
        this.date = date;
        this.states = states;
    }

    public LocalDate getDate() {
        return date;
    }
    public long getId() {
        return id;
    }
    public void setDate(LocalDate date) {
        this.date = date;
        this.id = date.toEpochDay();
    }

    public List<TfState> getRecords() {
        return states;
    }
    public void setRecords(List<TfState> states) {
        this.states = states;
    }

}
