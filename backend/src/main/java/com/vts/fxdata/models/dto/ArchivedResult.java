package com.vts.fxdata.models.dto;

public class ArchivedResult {
    private String timePeriod;
    private String profit;
    private String filter;

    public ArchivedResult(String timePeriod, String profit, String filter) {
        this.timePeriod = timePeriod;
        this.profit = profit;
        this.filter = filter;
    }

    public String getTimePeriod() {
        return timePeriod;
    }

    public void setTimePeriod(String timePeriod) {
        this.timePeriod = timePeriod;
    }

    public String getProfit() {
        return profit;
    }

    public void setProfit(String profit) {
        this.profit = profit;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }
}
