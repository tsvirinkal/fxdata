package com.vts.fxdata.models.dto;

import com.vts.fxdata.entities.ChartState;
import com.vts.fxdata.entities.Record;
import com.vts.fxdata.models.ActionEnum;
import com.vts.fxdata.models.TimeframeEnum;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

public class StatesView {

    private final static TimeframeEnum[] timeframes = new TimeframeEnum[] { TimeframeEnum.H1, TimeframeEnum.H4, TimeframeEnum.D1 };

    public static List<Pair> getPairs(List<ChartState> chartStates) {
        var pairStates = new TreeMap<String, Map<TimeframeEnum, ChartState>>();
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
            State state;
            double price =0, point=0;
            LocalDateTime updated = LocalDateTime.now(ZoneOffset.UTC);
            for (var tf : timeframes){
                ChartState chState = chStates.get(tf);

                if ( chState==null ) {
                    state = new State(pair, "Pending", tf.toString(), "", null,0);
                }
                else {
                    try {
                        updated = chState.getUpdated();
                        price = chState.getPrice();
                        point = chState.getPoint();
                    } catch(NullPointerException e) {
                    }

                    var progress = 0;
                    var action = chState.getAction();
                    Action actionView = null;
                    if (action!=null) {
                        var difference = action.getTargetPrice() - action.getStartPrice();
                        if (action.getAction()==ActionEnum.Sell) {
                            difference = action.getStartPrice() - action.getTargetPrice();
                        }
                        var targetPips = (int) (difference/chState.getPoint()/10);
                        difference = action.getPrice() - action.getStartPrice();
                        if (action.getAction()==ActionEnum.Sell) {
                            difference = action.getStartPrice() - action.getPrice();
                        }
                        progress = (int) (difference/chState.getPoint()/10 * 100.0 / targetPips);
                        actionView = new Action(action.getAction(), targetPips, action.getTime(),
                                                action.getPrice(), action.getStartPrice(), action.getTargetPrice());
                    }
                    state = new State(pair, chState.getState().toString(), tf.toString(),
                                        chState.getTime().toString(), actionView, progress);
                }
                states.add(state);
            }
            pairs.add(new Pair(pair, price, point, updated, states.toArray(new State[0])));
        }
        return pairs;
    }
}
