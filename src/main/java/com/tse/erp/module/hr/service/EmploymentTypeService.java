package com.tse.erp.module.hr.service;

import com.tse.erp.module.hr.entity.EmploymentType;

import java.util.List;

public interface EmploymentTypeService {

    List<EmploymentType> getAllEmploymentTypes();

    EmploymentType getEmploymentTypeById(Long id);

    EmploymentType createEmploymentType(EmploymentType employmentType);

    EmploymentType updateEmploymentType(
            Long id, EmploymentType employmentType);

    void deleteEmploymentType(Long id);
}
