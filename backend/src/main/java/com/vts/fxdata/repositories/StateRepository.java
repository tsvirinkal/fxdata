package com.vts.fxdata.repositories;

import com.vts.fxdata.entities.ChartState;
import com.vts.fxdata.entities.Record;
import com.vts.fxdata.models.State;
import com.vts.fxdata.models.Timeframe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StateRepository extends JpaRepository<ChartState, Long> {
    @Query(value = "select * from states order by time desc limit 200", nativeQuery = true)
    List<ChartState> getStates();

    @Query(value = "select * from states where pair=:#{#pair} order by time desc limit 200", nativeQuery = true)
    List<ChartState> getStates(String pair);

    @Query(value = "select * from states where state=:#{#state} order by time desc limit 200", nativeQuery = true)
    List<ChartState> getStates(int state);

    @Query(value = "select * from states where pair=:#{#pair} and timeframe=:#{#timeframe} order by time desc limit 1", nativeQuery = true)
    ChartState getState(String pair, int timeframe);

    List<ChartState> findByPair(String pair);
}
