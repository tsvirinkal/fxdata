package com.vts.fxdata.repositories;

import com.vts.fxdata.entities.Record;
import com.vts.fxdata.models.DayRecords;
import com.vts.fxdata.repositories.RecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
//import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalField;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

@Service
public class RecordService {

    private final RecordRepository recordRepository;

    @Autowired
    public RecordService(RecordRepository recordRepository) {
        this.recordRepository = recordRepository;
    }

    public void addRecord(Record record)
    {
        this.recordRepository.save(record);
    }

    public List<DayRecords> getLastRecords(int tzOffset) {
        return convertToDayRecordsList(this.recordRepository.getRecordsByDate(), tzOffset);
    }

    public List<DayRecords> getLastRecords(String pair, int tzOffset) {
        return convertToDayRecordsList(this.recordRepository.getRecordsByDateAndPair(pair), tzOffset);
    }

    public List<DayRecords> getConfirmedRecords(int tzOffset) {
        return convertToDayRecordsList(this.recordRepository.getConfirmedRecordsByDate(), tzOffset);
    }

    public List<DayRecords> getConfirmedRecords(String pair, int tzOffset) {
        return convertToDayRecordsList(this.recordRepository.getConfirmedRecordsByDateAndPair(pair), tzOffset);
    }

    @Transactional
    public void deleteRecord(long id) {
        this.recordRepository.deleteById(id);

    }

    public void saveAndFlush(Record record) {
        this.recordRepository.saveAndFlush(record);
    }

    public Optional<Record> getRecordById(long id) {
        return this.recordRepository.findById(id);
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
                dayRecords.setDate(localTime.toLocalDate());//LocalDate.ofInstant(r.getTime().toInstant(ZoneOffset.UTC),zoneId));
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
