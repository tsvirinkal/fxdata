package com.vts.fxdata.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vts.fxdata.models.dto.State;
import com.vts.fxdata.models.StateEnum;
import com.vts.fxdata.models.TimeframeEnum;
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
    private TimeframeEnum timeframe;
    private StateEnum state;

    private Double price;
    private Double point;

    @OneToOne
    @JoinColumn(name = "recordId", referencedColumnName = "id")
    private Record action;

    @Column(columnDefinition= "TIMESTAMP WITH TIME ZONE")
    @DateTimeFormat(iso= DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(pattern = "HH:mm dd.MM.yyyy")
    private LocalDateTime updated;

    @Column(columnDefinition= "TIMESTAMP WITH TIME ZONE DEFAULT now()")
    @DateTimeFormat(iso= DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(pattern = "HH:mm dd.MM.yyyy")
    private LocalDateTime time;

    public ChartState(Long id, String pair, TimeframeEnum timeframe, StateEnum state) {
        this();
        this.Id = id;
        this.pair = pair;
        this.timeframe = timeframe;
        this.state = state;
    }

    public ChartState(String pair, TimeframeEnum timeframe, StateEnum state) {
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
        this.price = 0.0;
    }

    public static ChartState newInstance(State state) {
        return new ChartState(state.getPair(),
                TimeframeEnum.valueOf(state.getTimeframe()),
                StateEnum.valueOf(state.getState()));
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

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Double getPoint() {
        return point;
    }

    public void setPoint(Double point) {
        this.point = point;
    }

    public LocalDateTime getUpdated() {
        return updated;
    }

    public void setUpdated(LocalDateTime updated) {
        this.updated = updated;
    }

    public Record getAction() {
        return this.action;
    }

    public void setAction(Record action) {
        this.action = action;
    }


}

