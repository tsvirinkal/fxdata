package com.vts.fxdata.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vts.fxdata.models.ActionEnum;
import com.vts.fxdata.models.StateEnum;
import com.vts.fxdata.models.TimeframeEnum;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.*;
import java.time.temporal.ChronoUnit;

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
    @JsonFormat(pattern = "HH:mm dd.MM.yyyy")
    private LocalDateTime time;

    /**
     * The potential action based on the found star candle
     */
    private ActionEnum action;

    /**
     * The state of the chart at the time the star candle was found. (e.g. Range, Bullish, Bearish)
     */
    private StateEnum state;

    /**
     * The bid price at the time when the star candle was found.
     */
    private Double price;

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
        this.setTime(LocalDateTime.now(ZoneOffset.UTC));
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
        // remove seconds and milliseconds
        time = time.truncatedTo(ChronoUnit.SECONDS);
        this.time = time;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
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
            this.confirmationDelay = formatDuration(this.time, LocalDateTime.now(ZoneOffset.UTC));
        }
    }

    public String getConfirmationDelay() {
        return this.confirmationDelay;
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

    private static String formatDuration(LocalDateTime start, LocalDateTime end) {
        Duration duration = Duration.between(start, end);
        long totalSeconds = duration.getSeconds();

        // Extract the hours, minutes, and seconds
        long days = totalSeconds / 86400;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        String ret = "";
        if (days>0) {
            ret += String.format("%02dd ", days);
        }
        if (hours>0) {
            ret += String.format("%02dh ", hours);
        }
        ret += String.format("%02dm", minutes);
        return ret;
    }
}

