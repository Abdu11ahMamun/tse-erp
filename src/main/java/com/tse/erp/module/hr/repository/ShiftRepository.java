package com.tse.erp.module.hr.repository;

import com.tse.erp.module.hr.entity.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long> {

    List<Shift> findAllByOrderByIdDesc();

    List<Shift> findByBuIdOrderByIdDesc(Long buId);

    List<Shift> findByBuIdAndShiftTitleIgnoreCase(
            Long buId, String shiftTitle);
}
