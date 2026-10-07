package com.tse.erp.module.asset.repository;

import com.tse.erp.module.asset.entity.CustodianDtl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustodianDtlRepository
        extends JpaRepository<CustodianDtl, Long> {

    List<CustodianDtl> findByCustodianIdOrderByIdAsc(Long custodianId);
}
