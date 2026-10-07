package com.tse.erp.module.asset.repository;

import com.tse.erp.module.asset.entity.Custodian;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustodianRepository
        extends JpaRepository<Custodian, Long> {

    List<Custodian> findAllByOrderByIdDesc();

    List<Custodian> findByBuIdOrderByIdDesc(Long buId);

    List<Custodian> findByBuIdAndCustodianNameIgnoreCase(
            Long buId, String custodianName);
}
