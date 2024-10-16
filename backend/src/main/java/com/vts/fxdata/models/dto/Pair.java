package com.vts.fxdata.models.dto;

public class Pair {
    private String name;
    private double price;
    private double point;
    private State[] states;
    private String updated;

    public Pair(String name, double price, double point, String updated, State[] states) {
        this.name = name;
        this.price = price;
        this.point = point;
        this.states = states;
        this.updated = updated;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public State[] getStates() {
        return states;
    }

    public void setStates(State[] states) {
        this.states = states;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }
}
