package com.vts.fxdata.models.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vts.fxdata.entities.Record;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DayRecords {

    private long id;

    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDate date;
    private List<Record> records;

    public DayRecords() {
        records = new ArrayList<Record>();
    }

    public DayRecords(LocalDate date, List<Record> records) {
        this.date = date;
        this.records = records;
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

    public List<Record> getRecords() {
        return records;
    }
    public void setRecords(List<Record> records) {
        this.records = records;
    }

}
