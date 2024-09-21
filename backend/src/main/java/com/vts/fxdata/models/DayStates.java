package com.vts.fxdata.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vts.fxdata.entities.ChartState;
import com.vts.fxdata.entities.Record;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DayStates {

    private long id;

    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDate date;
    private List<ChartState> states;

    public DayStates() {
        states = new ArrayList<ChartState>();
    }

    public DayStates(LocalDate date, List<ChartState> states) {
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

    public List<ChartState> getRecords() {
        return states;
    }
    public void setRecords(List<ChartState> states) {
        this.states = states;
    }

}
