package com.vts.fxdata.models.dto;

public class Heartbeat {
    private String pair;
    private double price;
    private double point;
    private int activeTF;
    private double[] levels;

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

    public int getActiveTF() {
        return activeTF;
    }

    public void setActiveTF(int activeTF) {
        this.activeTF = activeTF;
    }

    public double[] getLevels() {
        return levels;
    }

    public void setLevels(double[] levels) {
        this.levels = levels;
    }
}
