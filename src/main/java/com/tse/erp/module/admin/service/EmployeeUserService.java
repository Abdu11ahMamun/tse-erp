package com.tse.erp.module.admin.service;

import com.tse.erp.module.admin.dto.EmployeeUserCreateDto;
import com.tse.erp.module.admin.dto.EmployeeUserDto;

public interface EmployeeUserService {

    EmployeeUserDto getByEmployee(Long employeeId);

    EmployeeUserDto createForEmployee(Long employeeId, EmployeeUserCreateDto dto);
}