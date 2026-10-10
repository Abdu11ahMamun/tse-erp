package com.tse.erp.module.hr.service;

import com.tse.erp.module.hr.entity.LeaveTypeGroup;

import java.util.List;

public interface LeaveTypeGroupService {

    List<LeaveTypeGroup> getAllLeaveTypeGroups(Long leaveTypeId);

    LeaveTypeGroup getLeaveTypeGroupById(Long id);

    LeaveTypeGroup createLeaveTypeGroup(LeaveTypeGroup leaveTypeGroup);

    LeaveTypeGroup updateLeaveTypeGroup(
            Long id, LeaveTypeGroup leaveTypeGroup);

    void deleteLeaveTypeGroup(Long id);
}
