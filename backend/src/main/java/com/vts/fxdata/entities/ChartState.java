package com.vts.fxdata.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vts.fxdata.models.StateEnum;
import com.vts.fxdata.models.Timeframe;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "states",
        uniqueConstraints={
        @UniqueConstraint(columnNames = {"time", "pair", "timeframe"})
})
public class ChartState {
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
    private StateEnum state;

    @Column(columnDefinition= "TIMESTAMP WITH TIME ZONE DEFAULT now()")
    @DateTimeFormat(iso= DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(pattern = "HH:mm dd.MM.yyyy")
    private LocalDateTime time;

    public ChartState(Long id, String pair, Timeframe timeframe, StateEnum state) {
        this();
        this.Id = id;
        this.pair = pair;
        this.timeframe = timeframe;
        this.state = state;
    }

    public ChartState(String pair, Timeframe timeframe, StateEnum state) {
        this();
        this.pair = pair;
        this.timeframe = timeframe;
        this.state = state;
    }

    public ChartState() {
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

        this.time = time;
    }
}

