package com.vts.fxdata.models.dto;

public class TradeAck {
    private Long id;
    private Double price;

    public TradeAck() {}

    public TradeAck(Long id, Double price) {
        this.id = id;
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}
