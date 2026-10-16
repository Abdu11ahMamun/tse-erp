package com.tse.erp.module.hr.repository;

import com.tse.erp.module.hr.entity.LeaveAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveAllocationRepository
        extends JpaRepository<LeaveAllocation, Long> {

    List<LeaveAllocation> findAllByOrderByIdDesc();

    List<LeaveAllocation> findByBuIdOrderByIdDesc(Long buId);

    List<LeaveAllocation> findByLeaveYearOrderByIdDesc(Integer leaveYear);

    List<LeaveAllocation> findByBuIdAndLeaveYearOrderByIdDesc(
            Long buId, Integer leaveYear);

    List<LeaveAllocation>
    findByBuIdAndEmpTypeIdAndLeaveCategoryIdAndLeaveYear(
            Long buId, Long empTypeId, Long leaveCategoryId,
            Integer leaveYear);
}
