package com.vts.fxdata.models.dto;

public class Result {
    private String pair;
    private String timeframe;
    private String action;
    private Integer targetPips;
    private Integer profit;
    private Integer maxDrawdown;
    private Integer minProgress;
    private Integer maxProgress;
    private String startTime;
    private String endTime;

    public Result(String pair, String timeframe, String action, Integer targetPips, Integer profit, Integer maxDrawdown, Integer minProgress, Integer maxProgress, String startTime, String endTime) {
        this.pair = pair;
        this.timeframe = timeframe;
        this.action = action;
        this.targetPips = targetPips;
        this.profit = profit;
        this.maxDrawdown = maxDrawdown;
        this.minProgress = minProgress;
        this.maxProgress = maxProgress;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getPair() { return pair; }

    public void setPair(String pair) { this.pair = pair; }

    public String getTimeframe() { return timeframe; }

    public void setTimeframe(String timeframe) { this.timeframe = timeframe; }

    public String getAction() { return action; }

    public void setAction(String action) { this.action = action; }

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
}
