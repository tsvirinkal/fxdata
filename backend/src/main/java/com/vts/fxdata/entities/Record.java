package com.vts.fxdata.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vts.fxdata.models.ActionEnum;
import com.vts.fxdata.models.StateEnum;
import com.vts.fxdata.models.Timeframe;
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
    private String pair;
    private Timeframe timeframe;
    private ActionEnum actionEnum;
    private StateEnum state;

    private Double price;

    private boolean confirmation;
    private String confirmationDelay;

    private String notes;

    @Column(columnDefinition= "TIMESTAMP WITH TIME ZONE DEFAULT now()")
    @DateTimeFormat(iso= DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(pattern = "HH:mm dd.MM.yyyy")
    private LocalDateTime time;

    public Record(Long id, String pair, Timeframe timeframe, ActionEnum actionEnum, StateEnum state, Double price, boolean confirmation) {
        this();
        this.Id = id;
        this.pair = pair;
        this.timeframe = timeframe;
        this.actionEnum = actionEnum;
        this.state = state;
        this.price = price;
        this.confirmation = confirmation;
    }

    public Record(String pair, Timeframe timeframe, ActionEnum actionEnum, StateEnum state, Double price, boolean confirmation) {
        this();
        this.pair = pair;
        this.timeframe = timeframe;
        this.actionEnum = actionEnum;
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

    public Timeframe getTimeframe() {
        return timeframe;
    }

    public void setTimeframe(Timeframe timeframe) {
        this.timeframe = timeframe;
    }

    public ActionEnum getAction() {
        return actionEnum;
    }

    public void setAction(ActionEnum actionEnum) {
        this.actionEnum = actionEnum;
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
        this.notes = notes;
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

