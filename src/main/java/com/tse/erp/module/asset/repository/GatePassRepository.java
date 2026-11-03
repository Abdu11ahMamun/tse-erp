package com.tse.erp.module.asset.repository;

import com.tse.erp.module.asset.entity.GatePass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GatePassRepository extends JpaRepository<GatePass, Long> {

    List<GatePass> findAllByOrderByIdDesc();

    List<GatePass> findByBuIdOrderByIdDesc(Long buId);
}