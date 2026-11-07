package com.tse.erp.module.accounting.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserOptionDto {

    private Long userId;
    private String userName;
    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private String displayLabel; // e.g. "user_name : employee_code : employee_name"
}

