package com.tse.erp.module.hr.service;

import com.tse.erp.module.hr.entity.LeaveType;

import java.util.List;

public interface LeaveTypeService {

    List<LeaveType> getAllLeaveTypes(Long bgId);

    LeaveType getLeaveTypeById(Long id);

    LeaveType createLeaveType(LeaveType leaveType);

    LeaveType updateLeaveType(Long id, LeaveType leaveType);

    void deleteLeaveType(Long id);
}
