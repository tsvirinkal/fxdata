package com.vts.fxdata.repositories;

import com.vts.fxdata.entities.Trade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TradeService {

    private final TradeRepository tradeRepository;

    @Autowired
    public TradeService(TradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }

    public List<Trade> getTrades() {
        var recs = this.tradeRepository.getTrades();
        return recs.isPresent() ? recs.get() : null;
    }

    public Trade findById(Long id) {
        var trade = this.tradeRepository.findById(id);
        return trade.isPresent() ? trade.get() : null;
    }

    public Trade findByRecordId(Long recordId) {
        var trade = this.tradeRepository.findByRecordId(recordId);
        return trade.isPresent() ? trade.get() : null;
    }

    public void save(Trade trade) {
        this.tradeRepository.saveAndFlush(trade);
    }
}
