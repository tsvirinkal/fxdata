package com.vts.fxdata.models;

public class StateRequest {
    private String pair;
    private String timeframe;
    private String state;

    public String getPair() { return pair; }
    public void setPair(String pair) { this.pair = pair; }

    public String getTimeframe() { return timeframe; }
    public void setTimeframe(String timeframe) { this.timeframe = timeframe; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

}
