package com.tse.erp.module.asset.repository;

import com.tse.erp.module.asset.entity.GatePassDtl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GatePassDtlRepository
        extends JpaRepository<GatePassDtl, Long> {

    List<GatePassDtl> findByGatePassIdOrderByIdAsc(Long gatePassId);
}