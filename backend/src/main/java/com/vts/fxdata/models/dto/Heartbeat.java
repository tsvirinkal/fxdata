package com.vts.fxdata.models.dto;

import java.time.LocalDateTime;

public class Heartbeat {
    private String pair;
    private double price;
    private double point;

    public String getPair() {
        return pair;
    }

    public void setPair(String pair) {
        this.pair = pair;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getPoint() {
        return point;
    }

    public void setPoint(double point) {
        this.point = point;
    }
}
