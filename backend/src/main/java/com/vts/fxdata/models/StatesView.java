package com.vts.fxdata.models;

import com.vts.fxdata.entities.TfState;
import com.vts.fxdata.models.dto.Action;
import com.vts.fxdata.models.dto.Pair;
import com.vts.fxdata.models.dto.State;
import com.vts.fxdata.utils.TimeUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

public class StatesView {

    private final static TimeframeEnum[] timeframes = new TimeframeEnum[] { TimeframeEnum.H1, TimeframeEnum.H4, TimeframeEnum.D1 };

    public static List<Pair> getPairs(List<TfState> tfStates, int tzOffset) {
        var pairStates = new TreeMap<String, Map<TimeframeEnum, TfState>>();
        tfStates.forEach(state -> {
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
            double price = 0, point = 0;
            String updated = null;
            for (var tf : timeframes){
                TfState tfState = chStates.get(tf);

                if ( tfState==null ) {
                    state = new State(pair, "Pending", tf.toString(), "", null,0, Arrays.asList(), false);
                }
                else {
                    try {
                        price = tfState.getPrice();
                        point = tfState.getPoint();
                        updated = TimeUtils.formatDuration(tfState.getUpdated(), LocalDateTime.now(ZoneOffset.UTC), true) + " ago";
                    } catch(NullPointerException e) {
                    }

                    var actionList = tfState.getActions();
                    Action actionView = null;
                    var progress = 0;
                    if (!actionList.isEmpty()) {
                        // display the last action
                        var action = actionList.get(actionList.size()-1); // TODO the last action is not a good idea, maybe keep the ID of the first action?
                        progress = action.getProgress();
                        actionView = new Action(action.getAction(), action.getTargetPips(), TimeUtils.formatTime(action.getTime().minusMinutes(tzOffset)),
                                                action.getPrice(), action.getStartPrice(), action.getTargetPrice());
                    }
                    List<Long> ids = null;
                    if (tfState.getActions()!=null) {
                        ids = tfState.getActions().stream()
                                .map(com.vts.fxdata.entities.Record::getId)
                                .collect(Collectors.toList());
                    }
                    state = new State(pair, tfState.getState().toString(), tf.toString(),
                            TimeUtils.formatTime(tfState.getTime().minusMinutes(tzOffset)), actionView, progress, ids, tfState.isActive());
                }
                states.add(state);
            }
            pairs.add(new Pair(pair, price, point, updated, states.toArray(new State[0])));
        }
        return pairs;
    }
}
