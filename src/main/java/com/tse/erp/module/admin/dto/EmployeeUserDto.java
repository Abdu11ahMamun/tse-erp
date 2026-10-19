package com.tse.erp.module.admin.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EmployeeUserDto {
    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private Long bgId;
    private Long buId;
    private Long deptId;
    private Long desigId;
    // null hole ei employee-r kono user nai
    private Long userId;
    private String userName;
    private Long roleId;
    private Long userTypeId;
    private Integer status;
}