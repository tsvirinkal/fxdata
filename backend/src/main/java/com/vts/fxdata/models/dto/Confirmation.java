package com.vts.fxdata.models.dto;

import com.vts.fxdata.models.ActionEnum;

public class Confirmation {
    private String pair;
    private String timeframe;
    private ActionEnum action;
    private String time;
    private double price;
    long id;
    long recordId;

    public String getPair() {
        return pair;
    }

    public void setPair(String pair) {
        this.pair = pair;
    }

    public String getTimeframe() {
        return timeframe;
    }

    public void setTimeframe(String timeframe) {
        this.timeframe = timeframe;
    }

    public ActionEnum getAction() { return action; }

    public void setAction(ActionEnum action) { this.action = action; }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public long getRecordId() { return recordId; }

    public void setRecordId(long recordId) { this.recordId = recordId; }

    public long getId() { return id; }

    public void setId(long id) { this.id = id; }

    public double getPrice() { return price; }

    public void setPrice(double price) { this.price = price; }
}
