package com.vts.fxdata.models.dto;

public class Record {
    private String pair;
    private String timeframe;
    private String action;
    private String state;
    private Double price;

    public String getPair() { return pair; }

    public void setPair(String pair) { this.pair = pair; }
    public String getTimeframe() { return timeframe; }

    public void setTimeframe(String timeframe) { this.timeframe = timeframe; }
    public String getAction() { return action; }

    public void setAction(String action) { this.action = action; }
    public String getState() { return state; }

    public void setState(String state) { this.state = state; }
    public Double getPrice() { return price; }

    public void setPrice(Double price) { this.price = price; }
}
