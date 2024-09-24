package com.vts.fxdata.models;

import com.vts.fxdata.entities.ChartState;

import java.util.*;

public class StatesView {
    private ArrayList<String[]> states;

    public StatesView() {
    }

    public StatesView(List<ChartState> chartStates) {
        var sorted = new TreeMap<String, Map<Timeframe, StateEnum>>();
        chartStates.forEach(state -> {
            var map = sorted.get(state.getPair());
            if (map==null) {
                map = new HashMap<>();
            }
            map.put(state.getTimeframe(), state.getState());
            sorted.put(state.getPair(), map);
        });

        states = new ArrayList<>();
        for (String pair:sorted.keySet()) {
            String h1s="---", h4s="---", d1s="---";
            var h1o = sorted.get(pair).get(Timeframe.H1);
            var h4o = sorted.get(pair).get(Timeframe.H4);
            var d1o = sorted.get(pair).get(Timeframe.D1);
            if ( h1o!=null ) h1s=h1o.toString();
            if ( h4o!=null ) h4s=h4o.toString();
            if ( d1o!=null ) d1s=d1o.toString();
            states.add(new String[] {pair,h1s ,h4s ,d1s });
        }
    }

    public ArrayList<String[]> getStates() {
        return states;
    }
}
