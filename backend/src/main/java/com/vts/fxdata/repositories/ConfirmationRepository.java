package com.vts.fxdata.repositories;

import com.vts.fxdata.entities.Confirmation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConfirmationRepository extends JpaRepository<Confirmation, Long> {
    @Query(value = "select * from confirmations where pair=:#{#pair} order by time desc limit 10", nativeQuery = true)
    List<Confirmation> getPendingConfirmations(@Param("pair") String pair);
}
