package com.tse.erp.module.hr.service;

import com.tse.erp.module.hr.entity.LeaveAllocation;

import java.util.List;

public interface LeaveAllocationService {

    List<LeaveAllocation> getAllLeaveAllocations(
            Long buId, Integer leaveYear);

    LeaveAllocation getLeaveAllocationById(Long id);

    LeaveAllocation createLeaveAllocation(
            LeaveAllocation leaveAllocation);

    LeaveAllocation updateLeaveAllocation(
            Long id, LeaveAllocation leaveAllocation);

    void deleteLeaveAllocation(Long id);
}
