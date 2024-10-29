package com.vts.fxdata.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vts.fxdata.configuration.WebConfig;
import com.vts.fxdata.models.TradeEnum;
import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Entity
@Table(name = "trades")
public class Trade {
    @Id
    @SequenceGenerator(
            name = "trade_sequence",
            sequenceName = "trade_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "trade_sequence"
    )
    private Long Id;

    @DateTimeFormat(iso= DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(pattern = WebConfig.DATE_TIME_PATTERN)
    private LocalDateTime createdTime;

    @DateTimeFormat(iso= DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(pattern = WebConfig.DATE_TIME_PATTERN)
    private LocalDateTime openedTime;

    @DateTimeFormat(iso= DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(pattern = WebConfig.DATE_TIME_PATTERN)
    private LocalDateTime closedTime;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "recordId", referencedColumnName = "id")
    private Record action;

    @Enumerated(EnumType.STRING)
    private TradeEnum command;

    public Trade(Record action, TradeEnum command) {
        this.action = action;
        this.command = command;
        this.createdTime = action.getTime();
    }

    public Trade() {
    }

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public Record getAction() {
        return action;
    }

    public void setAction(Record action) {
        this.action = action;
    }

    public LocalDateTime getOpenedTime() {
        return openedTime;
    }

    public void setOpenedTime(LocalDateTime openedTime) {
        this.openedTime = openedTime;
    }

    public LocalDateTime getClosedTime() {
        return closedTime;
    }

    public void setClosedTime(LocalDateTime closedTime) {
        this.closedTime = closedTime;
    }

    public TradeEnum getCommand() {
        return command;
    }

    public void setCommand(TradeEnum command) {
        this.command = command;
    }
}

