package com.vts.fxdata.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vts.fxdata.configuration.WebConfig;
import com.vts.fxdata.models.ActionEnum;
import com.vts.fxdata.models.TimeframeEnum;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "confirmations",
        uniqueConstraints={
        @UniqueConstraint(columnNames = {"time", "pair", "timeframe"})
})
public class Confirmation {
    @Id
    @SequenceGenerator(
            name = "confirmation_sequence",
            sequenceName = "confirmation_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "confirmation_sequence"
    )
    private Long Id;
    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "record_ids")
    private List<Long> recordIds;
    private String pair;
    @Enumerated(EnumType.STRING)
    private TimeframeEnum timeframe;

    @Enumerated(EnumType.STRING)
    private ActionEnum action;
    @Column(columnDefinition= "TIMESTAMP WITH TIME ZONE DEFAULT now()")
    @DateTimeFormat(iso= DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(pattern = WebConfig.DATE_TIME_PATTERN)
    private LocalDateTime time;

    public Confirmation(String pair, TimeframeEnum timeframe, ActionEnum action, LocalDateTime time, long recordId) {
        this.pair = pair;
        this.timeframe = timeframe;
        this.time = time;
        this.action = action;
        this.recordIds = new ArrayList<>() { { add(recordId); }};
    }

    public Confirmation() {
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

    public ActionEnum getAction() { return action; }

    public void setAction(ActionEnum action) { this.action = action; }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public List<Long> getRecordIds() { return recordIds; }

    public void setRecordId(Long recordId) { recordIds.add(recordId); }
}

