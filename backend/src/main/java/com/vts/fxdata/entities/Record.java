package com.vts.fxdata.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vts.fxdata.configuration.WebConfig;
import com.vts.fxdata.models.ActionEnum;
import com.vts.fxdata.models.StateEnum;
import com.vts.fxdata.models.TimeframeEnum;
import com.vts.fxdata.utils.TimeUtils;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.*;

@Entity
@Table(name = "stars",
        uniqueConstraints={
        @UniqueConstraint(columnNames = {"time", "pair", "timeframe"})
})
public class Record {
    private static final int NOTES_LENGTH = 4096;
    @Id
    @SequenceGenerator(
            name = "star_sequence",
            sequenceName = "star_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "star_sequence"
    )
    private Long Id;

    /**
     * The name of the pair where a star candle was found.
     */
    private String pair;

    /**
     * The timeframe where a star candle was found.
     */
    private TimeframeEnum timeframe;

    /**
     * The time when the star candle was found.
     */
    @Column(columnDefinition= "TIMESTAMP WITH TIME ZONE DEFAULT now()")
    @DateTimeFormat(iso= DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(pattern = WebConfig.DATE_TIME_PATTERN)
    private LocalDateTime time;

    /**
     * The potential action based on the found star candle
     */
    private ActionEnum action;

    /**
     * The state of the chart at the time the action was confirmed. (e.g. Range, Bullish, Bearish)
     */
    private StateEnum state;

    /**
     * The bid price at the time when the star candle was found for unconfirmed records or
     * the big price at the time when the action was confirmed.
     */
    private Double price;

    /**
     * The bid price at the time when the action was completed.
     */
    private Double exitPrice;

    /**
     * The target price for the potential move.
     */
    private Double targetPrice;

    /**
     * The price where the actual price reversal happened and wasn't obvious until the confirmation was found.
     * AKA the best possible entry price for the reversal action.
     * This price is used as a starting point to calculate the price movement progress to the target price.
     */
    private Double startPrice;

    /**
     * True when the reversal was confirmed on the lower timeframe, false otherwise.
     */
    private boolean confirmation;
    /**
     * Human-readable time that it took to confirm the star candle.
     */

    private String confirmationDelay;
    /**
     * Technical notes for debugging
     */
    @Column(length = NOTES_LENGTH)
    private String notes;

    /**
     * The time when the action started.
     */
    @DateTimeFormat(iso= DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(pattern = WebConfig.DATE_TIME_PATTERN)
    private LocalDateTime startTime;

    /**
     * The time when the action ended.
     */
    @DateTimeFormat(iso= DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(pattern = WebConfig.DATE_TIME_PATTERN)
    private LocalDateTime endTime;

    /**
     * The profit this action brought in.
     */
    private Integer profit;

    /**
     * The maximum loss the account balance had to endure during this action.
     */
    private Integer maxDrawdown;

    /**
     * The current progress of the action showing the percentage of way the current price has moved from startPrice to targetPrice.
     */
    private Integer progress;

    /**
     * The maximum negative progress achieved until completion.
     */
    private Integer minProgress;

    /**
     * The maximum progress achieved until completion.
     */
    private Integer maxProgress;

    /**
     * The targeted pip value for this action.
     */
    private Integer targetPips;

    public Record(Long id, String pair, TimeframeEnum timeframe, ActionEnum action, StateEnum state, Double price, boolean confirmation) {
        this();
        this.Id = id;
        this.pair = pair;
        this.timeframe = timeframe;
        this.action = action;
        this.state = state;
        this.price = price;
        this.confirmation = confirmation;
    }

    public Record(String pair, TimeframeEnum timeframe, ActionEnum action, StateEnum state, Double price, boolean confirmation) {
        this();
        this.pair = pair;
        this.timeframe = timeframe;
        this.action = action;
        this.state = state;
        this.price = price;
        this.confirmation = confirmation;
    }

    public Record() {
        this.setTime(TimeUtils.removeSeconds(LocalDateTime.now(ZoneOffset.UTC)));
        this.profit = 0;
        this.maxDrawdown = 0;
        this.progress = 0;
        this.maxProgress = 0;
        this.minProgress = 0;
        this.targetPips = 0;
    }

    public Long getId() {
        return Id;
    }

    public String getPair() {
        return pair;
    }

    public void setPair(String pair) {
        this.pair = pair;
    }

    public TimeframeEnum getTimeframe() {
        return timeframe;
    }

    public void setTimeframe(TimeframeEnum timeframe) {
        this.timeframe = timeframe;
    }

    public ActionEnum getAction() {
        return action;
    }

    public void setAction(ActionEnum actionEnum) {
        this.action = actionEnum;
    }

    public StateEnum getState() {
        return state;
    }

    public void setState(StateEnum state) {
        this.state = state;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = TimeUtils.removeSeconds(time);
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getExitPrice() {
        return exitPrice ==null ? 0 : exitPrice;
    }

    public void setExitPrice(Double exitPrice) {
        this.exitPrice = exitPrice;
    }

    public Double getStartPrice() {
        return this.startPrice;
    }

    public void setStartPrice(Double StartPrice) {
        this.startPrice = StartPrice;
    }
    public Double getTargetPrice() {
        return this.targetPrice;
    }

    public void setTargetPrice(Double targetPrice) {
        this.targetPrice = targetPrice;
    }

    public boolean isConfirmation() {
        return confirmation;
    }

    public void setConfirmation(boolean confirmation) {
        this.confirmation = confirmation;
        if (confirmation) {
            setStartTime(TimeUtils.removeSeconds(LocalDateTime.now(ZoneOffset.UTC)));
            this.confirmationDelay = TimeUtils.formatDuration(this.time, this.startTime, false);
        }
    }

    public String getConfirmationDelay() {
        return this.confirmationDelay = this.confirmationDelay==null ? "" : this.confirmationDelay;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        if (notes.length()>NOTES_LENGTH) {
            this.notes = notes.substring(0, NOTES_LENGTH-1);
        } else {
            this.notes = notes;
        }
    }

    public LocalDateTime getStartTime() {
        return startTime==null ? LocalDateTime.now(ZoneOffset.UTC): startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Integer getProfit() {
        return profit==null ? 0 : profit;
    }

    public void setProfit(Integer profit) {
        this.profit = profit;
    }

    public Integer getMaxDrawdown() {
        return maxDrawdown==null ? 0 : maxDrawdown;
    }

    public void setMaxDrawdown(Integer maxDrawdown) {
        this.maxDrawdown = maxDrawdown;
    }

    public Integer getProgress() {
        return progress==null ? 0 : progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public Integer getMinProgress() {
        return minProgress==null ? 0 : minProgress;
    }

    public void setMinProgress(Integer minProgress) {
        this.minProgress = minProgress;
    }

    public Integer getMaxProgress() {
        return maxProgress==null ? 0 : maxProgress;
    }

    public void setMaxProgress(Integer maxProgress) {
        this.maxProgress = maxProgress;
    }

    public Integer getTargetPips() {
        return targetPips==null ? 0 : targetPips;
    }

    public void setTargetPips(Integer targetPips) {
        this.targetPips = targetPips;
    }
//
//    public TfState getTfState() {
//        return tfState;
//    }
//
//    public void setTfState(TfState tfState) {
//        this.tfState = tfState;
//    }
}

