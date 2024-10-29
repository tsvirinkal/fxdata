package com.vts.fxdata.models;

import com.vts.fxdata.entities.Trade;
import com.vts.fxdata.utils.TimeUtils;
import java.util.*;

public class TradesView {

    private final static TimeframeEnum[] timeframes = new TimeframeEnum[] { TimeframeEnum.H1, TimeframeEnum.H4, TimeframeEnum.D1 };

    public static List<com.vts.fxdata.models.dto.Trade> getTrades(boolean active, List<Trade> trades, int tzOffset) {
        var tradeView = new ArrayList<com.vts.fxdata.models.dto.Trade>();
        for (Trade t : trades) {
            if (active) {
                if (t.getCommand()==TradeEnum.Open && t.getOpenedTime()!=null) continue;
                if (t.getCommand()==TradeEnum.Close && t.getClosedTime()!=null) continue;
                if (t.getCommand()==TradeEnum.Wait) continue;
            }
            var createdTime = t.getCreatedTime() == null ? "" : TimeUtils.formatTime(t.getCreatedTime().minusMinutes(tzOffset));
            var openedTime = t.getOpenedTime() == null ? "" : TimeUtils.formatTime(t.getOpenedTime().minusMinutes(tzOffset));
            var closedTime = t.getClosedTime() == null ? "" : TimeUtils.formatTime(t.getClosedTime().minusMinutes(tzOffset));
            tradeView.add(new com.vts.fxdata.models.dto.Trade(
                    t.getId(),
                    t.getAction().getPair(),
                    t.getCommand().toString(),
                    t.getAction().getAction().toString(),
                    createdTime,
                    openedTime,
                    closedTime));
        }
        return tradeView;
    }
}
