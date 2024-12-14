package com.vts.fxdata.models.dto;

import java.util.List;

public class State {
    private String pair;
    private String state;
    private String timeframe;
    private String time;
    private Action action;
    private Integer progress;
    private List<Long> actions;

    private Boolean isActive;

    public State() {
    }

    public State(String pair, String state, String timeframe, String time, Action action, Integer progress, List<Long> ids, boolean isActive) {
        this.pair = pair;
        this.state = state;
        this.timeframe = timeframe;
        this.time = time;
        this.action = action;
        this.progress = progress;
        this.actions = ids;
        this.isActive = isActive;
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

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public List<Long> getActions() {
        return actions;
    }

    public void setActions(List<Long> actions) {
        this.actions = actions;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }
}
