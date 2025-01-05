package com.vts.fxdata.models.dto;

public class TradeAck {
    private Long id;
    private String pair;
    private String error;
    private Double price;

    public TradeAck() {}

    public TradeAck(Long id, Double price, String pair, String error) {
        this.id = id;
        this.price = price;
        this.pair = pair;
        this.error = error;
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

    public String getPair() {
        return pair;
    }

    public void setPair(String pair) {
        this.pair = pair;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
