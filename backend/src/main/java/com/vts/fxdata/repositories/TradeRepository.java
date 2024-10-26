package com.vts.fxdata.repositories;

import com.vts.fxdata.entities.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {
    @Query(value = "SELECT x.* FROM trades x JOIN Record r ON x.record_id = r.id WHERE r.id = :recordId", nativeQuery = true)
    Optional<Trade> findByRecordId(@Param("recordId") Long recordId);

    @Query(value = "SELECT * FROM trades WHERE closed_time is null ORDER BY time DESC LIMIT 200", nativeQuery = true)
    Optional<List<Trade>> getTrades();
}
