package com.tse.erp.module.admin.service;

import com.tse.erp.module.admin.dto.UserBuDto;
import com.tse.erp.module.admin.dto.UserEmployeeDto;

public interface UserAssignmentService {

    UserEmployeeDto getEmployee(Long userId);

    UserEmployeeDto saveEmployee(Long userId, UserEmployeeDto dto);

    UserBuDto getBusinessUnits(Long userId);

    UserBuDto saveBusinessUnits(Long userId, UserBuDto dto);
}
