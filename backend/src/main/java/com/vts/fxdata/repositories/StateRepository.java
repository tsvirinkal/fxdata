package com.vts.fxdata.repositories;

import com.vts.fxdata.entities.TfState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StateRepository extends JpaRepository<TfState, Long> {
    @Query(value = "select * from states order by time desc limit 200", nativeQuery = true)
    List<TfState> getStates();

    @Query(value = "select * from states where pair=:#{#pair} order by timeframe", nativeQuery = true)
    List<TfState> getStates(String pair);

    @Query(value = "select * from states where state=:#{#state} order by time desc limit 200", nativeQuery = true)
    List<TfState> getStates(int state);

    @Query(value = "select * from states where pair=:#{#pair} and timeframe=:#{#timeframe} order by time desc limit 1", nativeQuery = true)
    TfState getState(String pair, int timeframe);

    List<TfState> findByPair(String pair);
}
