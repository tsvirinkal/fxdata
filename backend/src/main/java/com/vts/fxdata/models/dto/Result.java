package com.vts.fxdata.models.dto;

public class Result {
    private String pair;
    private String timeframe;
    private String action;
    private String state;
    private Double entryPrice;
    private Double exitPrice;
    private Integer targetPips;
    private Integer profit;
    private Integer maxDrawdown;
    private Integer minProgress;
    private Integer maxProgress;
    private String startTime;
    private String endTime;
    private String duration;

    public Result(String pair, String timeframe, String action, String state, Double entryPrice, Double exitPrice,
                  Integer targetPips, Integer profit, Integer maxDrawdown, Integer minProgress, Integer maxProgress,
                  String startTime, String endTime, String duration) {
        this.pair = pair;
        this.timeframe = timeframe;
        this.action = action;
        this.state = state;
        this.entryPrice = entryPrice;
        this.exitPrice = exitPrice;
        this.targetPips = targetPips;
        this.profit = profit;
        this.maxDrawdown = maxDrawdown;
        this.minProgress = minProgress;
        this.maxProgress = maxProgress;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
    }

    public String getPair() { return pair; }

    public void setPair(String pair) { this.pair = pair; }

    public String getTimeframe() { return timeframe; }

    public void setTimeframe(String timeframe) { this.timeframe = timeframe; }

    public String getAction() { return action; }

    public void setAction(String action) { this.action = action; }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Double getEntryPrice() {
        return entryPrice;
    }

    public void setEntryPrice(Double entryPrice) {
        this.entryPrice = entryPrice;
    }

    public Double getExitPrice() {
        return exitPrice;
    }

    public void setExitPrice(Double exitPrice) {
        this.exitPrice = exitPrice;
    }

    public Integer getTargetPips() {
        return targetPips;
    }

    public void setTargetPips(Integer targetPips) {
        this.targetPips = targetPips;
    }

    public Integer getProfit() {
        return profit;
    }

    public void setProfit(Integer profit) {
        this.profit = profit;
    }

    public Integer getMaxDrawdown() {
        return maxDrawdown;
    }

    public void setMaxDrawdown(Integer maxDrawdown) {
        this.maxDrawdown = maxDrawdown;
    }

    public Integer getMinProgress() {
        return minProgress;
    }

    public void setMinProgress(Integer minProgress) {
        this.minProgress = minProgress;
    }

    public Integer getMaxProgress() {
        return maxProgress;
    }

    public void setMaxProgress(Integer maxProgress) {
        this.maxProgress = maxProgress;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
