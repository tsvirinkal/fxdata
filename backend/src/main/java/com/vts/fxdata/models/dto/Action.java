package com.vts.fxdata.models.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vts.fxdata.configuration.WebConfig;
import com.vts.fxdata.models.ActionEnum;

import java.time.LocalDateTime;

public class Action {
    private ActionEnum action;
    private int targetPips;

    @JsonFormat(pattern = WebConfig.DATE_TIME_PATTERN)
    private LocalDateTime time;

    private double entryPrice;
    private double startPrice;

    private  double targetPrice;

    public Action(ActionEnum action, int targetPips, LocalDateTime time, double entryPrice, double startPrice, double targetPrice) {
        this.action = action;
        this.targetPips = targetPips;
        this.time = time;
        this.entryPrice = entryPrice;
        this.startPrice = startPrice;
        this.targetPrice = targetPrice;

    }

    public ActionEnum getAction() {
        return action;
    }

    public void setAction(ActionEnum action) {
        this.action = action;
    }

    public int getTargetPips() {
        return targetPips;
    }

    public void setTargetPips(int target) {
        this.targetPips = target;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public double getEntryPrice() {
        return entryPrice;
    }

    public void setEntryPrice(double entryPrice) {
        this.entryPrice = entryPrice;
    }

    public double getStartPrice() {
        return startPrice;
    }

    public void setStartPrice(double startPrice) {
        this.startPrice = startPrice;
    }

    public double getTargetPrice() {
        return targetPrice;
    }

    public void setTargetPrice(double targetPrice) {
        this.targetPrice = targetPrice;
    }
}
