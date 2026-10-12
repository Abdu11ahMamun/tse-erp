package com.tse.erp.module.admin.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserEmployeeDto {

    private Long userId;
    private Long employeeId;
    private String employeeName;
    private String employeeCode;
}
