package com.vts.fxdata.repositories;

import com.vts.fxdata.controllers.MainControllerV2;
import com.vts.fxdata.entities.Record;
import com.vts.fxdata.models.dto.DayRecords;
import com.vts.fxdata.models.dto.Result;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Service
public class RecordService {
    private static final Logger log = LoggerFactory.getLogger(RecordService.class);
    private final RecordRepository recordRepository;

    @Autowired
    public RecordService(RecordRepository recordRepository) {
        this.recordRepository = recordRepository;
    }

    public void addRecord(Record record)
    {
        try {
            this.recordRepository.save(record);
        } catch(Exception e) {
            log.error(String.format("Failed to persist record for %s,%s id=%d",
                    record.getPair(),record.getTimeframe().toString(),record.getId()), e);
        }
    }

    public List<DayRecords> getLastRecords(int tzOffset) {
        return convertToDayRecordsList(this.recordRepository.getRecords(), tzOffset);
    }

    public List<DayRecords> getLastRecords(String pair, int tzOffset) {
        return convertToDayRecordsList(this.recordRepository.getRecordsByDateAndPair(pair), tzOffset);
    }

    public List<DayRecords> getConfirmedRecords(int tzOffset) {
        return convertToDayRecordsList(this.recordRepository.getConfirmedRecords(), tzOffset);
    }

    public List<DayRecords> getConfirmedRecords(String pair, int tzOffset) {
        return convertToDayRecordsList(this.recordRepository.getConfirmedRecordsByPair(pair), tzOffset);
    }

    public List<Record> getResultRecords() {
        return this.recordRepository.getResultRecords();
    }

    @Transactional
    public void deleteRecord(long id) {
        this.recordRepository.deleteById(id);
    }

    @Transactional
    public void deleteAll(Iterator<Long> recordIds) {
        recordIds.forEachRemaining(id -> this.recordRepository.deleteById(id));
    }

    public void save(Record record) {
        this.recordRepository.save(record);
    }

    public void saveAndFlush(Record record) {
        this.recordRepository.saveAndFlush(record);
    }

    public Record getRecordById(long id) {
        var rec = this.recordRepository.findById(id);
        return rec.isPresent() ? rec.get() : null;
    }

    private static List<DayRecords> convertToDayRecordsList(List<Record> records, int tzOffset) {
        var listDayRecords = new ArrayList<DayRecords>();

        DayRecords dayRecords = null;
        for (Record r: records) {
            if (r.getTime()==null) continue;

            var localTime = r.getTime().minusMinutes(tzOffset);
            if (dayRecords!=null &&
                    localTime.getDayOfMonth() != dayRecords.getDate().getDayOfMonth())
            {
                listDayRecords.add(dayRecords);
                dayRecords = null;
            }

            if (dayRecords==null) {
                dayRecords = new DayRecords();
                dayRecords.setDate(localTime.toLocalDate());
            }

            r.setTime(localTime);
            dayRecords.getRecords().add(r);
        }
        if (dayRecords!=null && dayRecords.getDate()!=null) {
            listDayRecords.add(dayRecords);
        }
        return listDayRecords;
    }
}
