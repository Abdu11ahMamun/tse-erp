package com.tse.erp.module.hr.repository;

import com.tse.erp.module.hr.entity.ManualAttnUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ManualAttnUpdateRepository
        extends JpaRepository<ManualAttnUpdate, Long> {

    List<ManualAttnUpdate> findAllByOrderByIdDesc();

    List<ManualAttnUpdate> findByBuIdOrderByIdDesc(Long buId);

    List<ManualAttnUpdate> findByAttenDateOrderByIdDesc(LocalDate attenDate);

    List<ManualAttnUpdate> findByBuIdAndAttenDateOrderByIdDesc(
            Long buId, LocalDate attenDate);

    List<ManualAttnUpdate> findByBuIdAndDeptIdAndDesigIdAndShiftIdAndAttenDate(
            Long buId, Long deptId, Long desigId,
            Long shiftId, LocalDate attenDate);
}