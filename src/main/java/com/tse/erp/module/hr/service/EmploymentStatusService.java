package com.tse.erp.module.hr.service;

import com.tse.erp.module.hr.entity.EmploymentStatus;

import java.util.List;

public interface EmploymentStatusService {

    List<EmploymentStatus> getAllEmploymentStatuses();

    EmploymentStatus getEmploymentStatusById(Long id);

    EmploymentStatus createEmploymentStatus(EmploymentStatus employmentStatus);

    EmploymentStatus updateEmploymentStatus(
            Long id, EmploymentStatus employmentStatus);

    void deleteEmploymentStatus(Long id);
}