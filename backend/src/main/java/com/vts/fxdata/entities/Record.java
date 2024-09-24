package com.vts.fxdata.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vts.fxdata.models.Action;
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
    private Action action;
    private StateEnum state;

    private Double price;

    private boolean confirmation;
    private String confirmationDelay;

    @Column(columnDefinition= "TIMESTAMP WITH TIME ZONE DEFAULT now()")
    @DateTimeFormat(iso= DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(pattern = "HH:mm dd.MM.yyyy")
    private LocalDateTime time;

    public Record(Long id, String pair, Timeframe timeframe, Action action, StateEnum state, Double price, boolean confirmation) {
        this();
        this.Id = id;
        this.pair = pair;
        this.timeframe = timeframe;
        this.action = action;
        this.state = state;
        this.price = price;
        this.confirmation = confirmation;
    }

    public Record(String pair, Timeframe timeframe, Action action, StateEnum state, Double price, boolean confirmation) {
        this();
        this.pair = pair;
        this.timeframe = timeframe;
        this.action = action;
        this.state = state;
        this.price = price;
        this.confirmation = confirmation;
    }

    public Record() {
        var nowUtc = LocalDateTime.now(ZoneOffset.UTC);
        // remove seconds and milliseconds
        nowUtc = nowUtc.truncatedTo(ChronoUnit.SECONDS);
        this.setTime(nowUtc);
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

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
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
        if (this.time==null) {
            this.time = time;
        } else {
            setConfirmationDelay(formatDuration(this.time, time));
        }
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
    }

    public String getConfirmationDelay() {
        return this.confirmationDelay;
    }

    public void setConfirmationDelay(String delay) {
        this.confirmationDelay = delay;
    }
    private static String formatDuration(LocalDateTime start, LocalDateTime end) {
        // Calculate the duration between the two DateTime objects
        Duration duration = Duration.between(start, end);

        // Get the total seconds of the duration
        long totalSeconds = duration.getSeconds();

        // Extract the hours, minutes, and seconds
        long days = totalSeconds / 86400;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        String ret = "";
        if (days>0) {
            ret += String.format("%02dd", days);
        }
        if (hours>0) {
            ret += String.format(" %02dh", hours);
        }
        if (minutes>0) {
            ret += String.format(" %02dm", minutes);
        }
        if (seconds>0) {
            ret += String.format(" %02ds", seconds);
        }
        return ret;
    }

}

