package com.vts.fxdata.repositories;

import com.vts.fxdata.entities.Record;
import com.vts.fxdata.models.dto.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecordRepository extends JpaRepository<Record, Long> {
    @Query(value = "select * from stars order by time desc limit 200", nativeQuery = true)
    List<Record> getRecords();

    @Query(value = "select * from stars where pair=:#{#pair} order by time desc limit 200", nativeQuery = true)
    List<Record> getRecordsByDateAndPair(String pair);

    @Query(value = "select * from stars where confirmation=true order by time desc limit 200", nativeQuery = true)
    List<Record> getConfirmedRecords();

    @Query(value = "select * from stars where pair=:#{#pair} and confirmation=true order by time desc limit 200", nativeQuery = true)
    List<Record> getConfirmedRecordsByPair(String pair);

    @Query(value = "select * from stars where confirmation=true and end_time is not null order by end_time desc, pair asc", nativeQuery = true)
    List<Record> getResultRecords();

    List<Record> findByPair(String pair);
}
