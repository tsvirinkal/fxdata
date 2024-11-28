package com.vts.fxdata.models.dto;

public class Confirmation {
    private long id;
    private double price;
    private double[] levels;
    private double point;

    public long getId() { return id; }

    public void setId(long id) { this.id = id; }

    public double getPrice() { return price; }

    public void setPrice(double price) { this.price = price; }

    public double[] getLevels() {
        return levels;
    }

    public void setLevels(double[] levels) {
        this.levels = levels;
    }
}
