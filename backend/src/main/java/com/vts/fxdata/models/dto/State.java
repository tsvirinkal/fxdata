package com.vts.fxdata.models.dto;

public class State {
    private String pair;
    private String state;
    private String timeframe;
    private String time;
    private Action action;

    public State(String pair, String state, String timeframe, String time) {
        this.pair = pair;
        this.state = state;
        this.timeframe = timeframe;
        this.time = time;
    }

    public String getPair() {
        return pair;
    }

    public void setPair(String pair) {
        this.pair = pair;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTimeframe() {
        return timeframe;
    }

    public void setTimeframe(String timeframe) {
        this.timeframe = timeframe;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }
}