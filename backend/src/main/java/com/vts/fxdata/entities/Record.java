package com.vts.fxdata.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vts.fxdata.models.Action;
import com.vts.fxdata.models.State;
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
    private State state;

    private Double price;

    private boolean confirmation;

    @Column(columnDefinition= "TIMESTAMP WITH TIME ZONE DEFAULT now()")
    @DateTimeFormat(iso= DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(pattern = "HH:mm dd.MM.yyyy")
    private LocalDateTime time;

    public Record(Long id, String pair, Timeframe timeframe, Action action, State state, Double price, boolean confirmation) {
        this();
        this.Id = id;
        this.pair = pair;
        this.timeframe = timeframe;
        this.action = action;
        this.state = state;
        this.price = price;
        this.confirmation = confirmation;
    }

    public Record(String pair, Timeframe timeframe, Action action, State state, Double price, boolean confirmation) {
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

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {

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
    }
}

