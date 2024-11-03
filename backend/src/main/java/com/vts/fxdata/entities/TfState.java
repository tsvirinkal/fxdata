package com.vts.fxdata.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vts.fxdata.configuration.WebConfig;
import com.vts.fxdata.models.dto.State;
import com.vts.fxdata.models.StateEnum;
import com.vts.fxdata.models.TimeframeEnum;
import com.vts.fxdata.utils.TimeUtils;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "states",
        uniqueConstraints={
        @UniqueConstraint(columnNames = {"time", "pair", "timeframe"})
})
public class TfState {
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

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "stateId", referencedColumnName = "id")
    @OrderBy("time ASC")
    private List<Record> actions;

    @Column(columnDefinition= "TIMESTAMP WITH TIME ZONE")
    @DateTimeFormat(iso= DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(pattern = WebConfig.DATE_TIME_PATTERN)
    private LocalDateTime updated;

    @Column(columnDefinition= "TIMESTAMP WITH TIME ZONE DEFAULT now()")
    @DateTimeFormat(iso= DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(pattern = WebConfig.DATE_TIME_PATTERN)
    private LocalDateTime time;

    public TfState(Long id, String pair, TimeframeEnum timeframe, StateEnum state) {
        this();
        this.Id = id;
        this.pair = pair;
        this.timeframe = timeframe;
        this.state = state;
    }

    public TfState(String pair, TimeframeEnum timeframe, StateEnum state) {
        this();
        this.pair = pair;
        this.timeframe = timeframe;
        this.state = state;
    }

    public TfState() {
        this.setTime(TimeUtils.removeSeconds(LocalDateTime.now(ZoneOffset.UTC)));
        this.price = 0.0;
        this.actions = new ArrayList<>();
    }

    public static TfState newInstance(State state) {
        return new TfState(state.getPair(),
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

    public List<Record> getActions() {
        return this.actions;
    }

    public void setActions(List<Record> states) {
        this.actions = states;
    }

    public void addAction(Record action) {
        this.actions.add(action);
    }
}

