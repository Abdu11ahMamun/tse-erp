package com.tse.erp.module.hr.repository;

import com.tse.erp.module.hr.entity.EmpAttGps;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmpAttGpsRepository
        extends JpaRepository<EmpAttGps, Long> {

    List<EmpAttGps> findByAttConfigIdOrderByIdAsc(Long attConfigId);

    boolean existsByLocationIdAndStatus(Long locationId, Integer status);
}