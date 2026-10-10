package com.tse.erp.module.hr.repository;

import com.tse.erp.module.hr.entity.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveTypeRepository
        extends JpaRepository<LeaveType, Long> {

    List<LeaveType> findAllByOrderByIdDesc();

    List<LeaveType> findByBgIdOrderByIdDesc(Long bgId);

    List<LeaveType> findByBgIdAndTypeNameIgnoreCase(
            Long bgId, String typeName);

    List<LeaveType> findByBgIdAndShortNameIgnoreCase(
            Long bgId, String shortName);
}
