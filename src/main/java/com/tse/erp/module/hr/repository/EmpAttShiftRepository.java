package com.tse.erp.module.hr.repository;

import com.tse.erp.module.hr.entity.EmpAttShift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmpAttShiftRepository
        extends JpaRepository<EmpAttShift, Long> {

    List<EmpAttShift> findByAttConfigIdOrderByIdAsc(Long attConfigId);

    boolean existsByShiftIdAndStatus(Long shiftId, Integer status);
}