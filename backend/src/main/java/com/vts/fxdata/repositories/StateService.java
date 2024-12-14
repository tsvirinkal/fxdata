package com.vts.fxdata.repositories;

import com.vts.fxdata.entities.TfState;
import com.vts.fxdata.models.DayStates;
import com.vts.fxdata.models.StateEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

@Service
public class StateService {

    private final StateRepository stateRepository;

    @Autowired
    public StateService(StateRepository stateRepository) {
        this.stateRepository = stateRepository;
    }

    public boolean setState(TfState record)
    {
        var state = this.stateRepository.getState(record.getPair(), record.getTimeframe().ordinal());
        if (state==null) {
            this.stateRepository.save(record);
            return true;
        } else
        if (!(state.getPair().equals(record.getPair()) && state.getState()==record.getState())) {
            state.setState(record.getState());
            state.setTime(record.getTime());
            this.stateRepository.save(state);
            return true;
        }
        // not modified
        return false;
    }

    public TfState getState(String pair, int timeframe) {
        if (pair==null)
            throw new IllegalArgumentException("Pair is null");
        return this.stateRepository.getState(pair, timeframe);
    }

    public TfState getActiveState(String pair) {
        if (pair==null)
            throw new IllegalArgumentException("Pair is null");
        var rec = this.stateRepository.getStates(pair).stream().filter(st -> st.isActive()).findFirst();
        return rec.isPresent() ? rec.get() : null;
    }

    public List<TfState> getStates(String pair) {
        if (pair==null)
            throw new IllegalArgumentException("Pair is null");
        return this.stateRepository.getStates(pair);
    }

    public List<TfState> getLastStates(StateEnum state) {
        return state==null ? this.stateRepository.getStates() : this.stateRepository.getStates(state.ordinal());
    }

    @Transactional
    public void deleteRecord(long id) {
        this.stateRepository.deleteById(id);
    }

    public void save(TfState record) {
        this.stateRepository.save(record);
    }

    public void saveAndFlush(TfState record) {
        this.stateRepository.saveAndFlush(record);
    }

    public List<TfState> findAll() {
        return this.stateRepository.findAll();
    }

    public TfState getRecordById(long id) {
        var rec = this.stateRepository.findById(id);
        return rec.isPresent() ? rec.get() : null;
    }

    private static List<DayStates> convertToDayStatesList(List<TfState> records, TimeZone timezone) {
        var listDayStates = new ArrayList<DayStates>();

        DayStates DayStates = null;
        for (TfState r: records) {
            if (r.getTime()==null) continue;

            var localTime = r.getTime().toInstant(ZoneOffset.UTC).atZone(timezone.toZoneId());
            if (DayStates!=null &&
                    localTime.getDayOfMonth() != DayStates.getDate().getDayOfMonth())
            {
                listDayStates.add(DayStates);
                DayStates = null;
            }

            if (DayStates==null) {
                DayStates = new DayStates();
                DayStates.setDate(LocalDate.ofInstant(r.getTime().toInstant(ZoneOffset.UTC),timezone.toZoneId()));
            }

            r.setTime(localTime.toLocalDateTime());
            DayStates.getRecords().add(r);
        }
        if (DayStates!=null && DayStates.getDate()!=null) {
            listDayStates.add(DayStates);
        }
        return listDayStates;
    }
}
