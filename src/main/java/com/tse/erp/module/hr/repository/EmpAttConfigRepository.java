package com.tse.erp.module.hr.repository;

import com.tse.erp.module.hr.entity.EmpAttConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmpAttConfigRepository
        extends JpaRepository<EmpAttConfig, Long> {

    List<EmpAttConfig> findAllByOrderByIdDesc();

    List<EmpAttConfig> findByBuIdOrderByIdDesc(Long buId);

    List<EmpAttConfig> findByEmployeeId(Long employeeId);
}