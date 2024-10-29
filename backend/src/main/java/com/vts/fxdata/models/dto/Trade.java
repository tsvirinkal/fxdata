package com.vts.fxdata.models.dto;

public class Trade {
    private Long id;
    private String pair;
    private String command;
    private String action;
    private String createdTime;
    private String openedTime;
    private String closedTime;

    public Trade(Long id, String pair, String command, String action, String createdTime, String openedTime, String closedTime) {
        this.id = id;
        this.pair = pair;
        this.command = command;
        this.action = action;
        this.createdTime = createdTime;
        this.openedTime = openedTime;
        this.closedTime = closedTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPair() {
        return pair;
    }

    public void setPair(String pair) {
        this.pair = pair;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getOpenedTime() {
        return openedTime;
    }

    public void setOpenedTime(String openedTime) {
        this.openedTime = openedTime;
    }

    public String getClosedTime() {
        return closedTime;
    }

    public void setClosedTime(String closedTime) {
        this.closedTime = closedTime;
    }
}
