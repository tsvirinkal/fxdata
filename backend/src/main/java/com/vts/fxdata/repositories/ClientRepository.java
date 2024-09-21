package com.vts.fxdata.repositories;

import com.vts.fxdata.entities.Client;
import com.vts.fxdata.entities.Confirmation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
   // List<Client> findAll();
}
