package com.vts.fxdata.repositories;

import com.vts.fxdata.entities.ChartState;
import com.vts.fxdata.models.DayStates;
import com.vts.fxdata.models.StateEnum;
import com.vts.fxdata.models.dto.Pair;
import com.vts.fxdata.models.dto.StatesView;
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

    public boolean setState(ChartState record)
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
//
//    public List<DayStates> getLastStates(TimeZone timezone) {
//        return convertToDayStatesList(this.stateRepository.getStates(), timezone);
//    }
//
//    public List<DayStates> getLastStates(String pair, TimeZone timezone) {
//        return convertToDayStatesList(this.stateRepository.getStates(pair), timezone);
//    }

    public List<Pair> getLastStates(StateEnum state) {
        List<ChartState> ret;
        if (state==null)
            ret = this.stateRepository.getStates();
        else
            ret = this.stateRepository.getStates(state.ordinal());
        return StatesView.getPairs(ret);
    }

    @Transactional
    public void deleteRecord(long id) {
        this.stateRepository.deleteById(id);
    }

    public void saveAndFlush(ChartState record) {
        this.stateRepository.saveAndFlush(record);
    }

    public Optional<ChartState> getRecordById(long id) {
        return this.stateRepository.findById(id);
    }

    private static List<DayStates> convertToDayStatesList(List<ChartState> records, TimeZone timezone) {
        var listDayStates = new ArrayList<DayStates>();

        DayStates DayStates = null;
        for (ChartState r: records) {
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
