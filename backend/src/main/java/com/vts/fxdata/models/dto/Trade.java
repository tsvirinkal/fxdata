package com.vts.fxdata.models.dto;

public class Trade {
    private Long id;
    private String pair;
    private String command;
    private String action;
    private Double price;
    private Integer profit;
    private String openedTime;
    private String error;

    public Trade(Long id, String pair, String command, String action, Double price, Integer profit, String openedTime, String error) {
        this.id = id;
        this.pair = pair;
        this.command = command;
        this.action = action;
        this.price = price;
        this.profit = profit;
        this.openedTime = openedTime;
        this.error = error;
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

    public String getOpenedTime() {
        return openedTime;
    }

    public void setOpenedTime(String openedTime) {
        this.openedTime = openedTime;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getProfit() {
        return profit;
    }

    public void setProfit(Integer profit) {
        this.profit = profit;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
