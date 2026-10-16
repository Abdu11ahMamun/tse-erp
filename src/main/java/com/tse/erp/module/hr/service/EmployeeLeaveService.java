package com.tse.erp.module.hr.service;

import com.tse.erp.module.hr.dto.LeaveBalanceDto;
import com.tse.erp.module.hr.dto.LeaveCalcDto;
import com.tse.erp.module.hr.entity.EmployeeLeave;

import java.time.LocalDate;
import java.util.List;

public interface EmployeeLeaveService {

    List<EmployeeLeave> getAllEmployeeLeaves(
            String view, Long buId, Long employeeId);

    EmployeeLeave getEmployeeLeaveById(Long id);

    EmployeeLeave createEmployeeLeave(
            EmployeeLeave row, boolean submit);

    EmployeeLeave updateEmployeeLeave(
            Long id, EmployeeLeave row, boolean submit);

    EmployeeLeave submitEmployeeLeave(Long id);

    void deleteEmployeeLeave(Long id);

    List<LeaveBalanceDto> getAvailableCategories(
            Long employeeId, Integer year);

    LeaveCalcDto calculate(
            Long employeeId, Long leaveCategoryId,
            LocalDate fromDate, LocalDate toDate);
}
