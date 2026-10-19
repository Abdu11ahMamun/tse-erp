package com.tse.erp.module.admin.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class EmployeeUserCreateDto {
    private String userName;
    private String password;
    private String confirmPassword;
    private LocalDate fromDate;
    private LocalDate toDate;
    private Long roleId;
    private Long userTypeId;
    private Integer isAndroidFixed;
    private String androidId;
    private Integer status;
}