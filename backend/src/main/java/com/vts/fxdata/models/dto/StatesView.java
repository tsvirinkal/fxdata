package com.vts.fxdata.models.dto;

import com.vts.fxdata.entities.ChartState;
import com.vts.fxdata.models.Timeframe;

import java.util.*;

public class StatesView {

    private final static Timeframe[] timeframes = new Timeframe[] { Timeframe.H1, Timeframe.H4, Timeframe.D1 };

    public static List<Pair> getPairs(List<ChartState> chartStates) {
        var pairStates = new TreeMap<String, Map<Timeframe, ChartState>>();
        chartStates.forEach(state -> {
            var map = pairStates.get(state.getPair());
            if (map==null) {
                map = new HashMap<>();
            }
            map.put(state.getTimeframe(), state);
            pairStates.put(state.getPair(), map);
        });

        var pairs = new ArrayList<Pair>();
        for (String pair:pairStates.keySet()) {
            var chStates = pairStates.get(pair);
            var states = new ArrayList<State>();
            for (var tf : timeframes){
                ChartState chState = chStates.get(tf);
                State state;
                if ( chState==null ) {
                    state = new State(pair, "---", tf.toString(), "");
                }
                else {
                    state = new State(pair,
                                    chState.getState().toString(),
                                    tf.toString(),
                                    chState.getTime().toString());
                }
                states.add(state);
            }
            pairs.add(new Pair(pair,states.toArray(new State[0])));
        }
        return pairs;
    }
}
