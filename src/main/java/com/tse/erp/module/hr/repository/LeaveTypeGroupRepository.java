package com.tse.erp.module.hr.repository;

import com.tse.erp.module.hr.entity.LeaveTypeGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveTypeGroupRepository
        extends JpaRepository<LeaveTypeGroup, Long> {

    List<LeaveTypeGroup> findAllByOrderByIdDesc();

    List<LeaveTypeGroup> findByLeaveTypeIdOrderByIdDesc(Long leaveTypeId);

    List<LeaveTypeGroup> findByLeaveTypeIdAndBuId(
            Long leaveTypeId, Long buId);

    boolean existsByLeaveTypeId(Long leaveTypeId);
}
