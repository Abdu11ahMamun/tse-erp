package com.tse.erp.module.accounting.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmUserSetupResponseDto {

    private Long id;
    private Long bgId;
    private String groupName;

    private Long buId;
    private String businessUnitName;

    private Long userId;
    private String userName;
    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private String userDisplayLabel;

    private String voucherDate;
    private String voucherType;
    private String voucherTypeDefault;
    private String voucherTypeDefaultName;

    private String location;
    private String locationDefault;
    private String locationDefaultName;

    private String bankAccount;
    private String bankAccountDefault;
    private String bankAccountDefaultName;

    private String costCenter;
    private String costCenterDefault;
    private String costCenterDefaultName;

    private Integer status;

    private Long createdUserId;
    private String createdUserName;
    private Long createdEmpId;
    private String createdEmpName;
    private LocalDateTime createdDatetime;
    private String createdApp;

    private Long updatedUserId;
    private String updatedUserName;
    private Long updatedEmpId;
    private String updatedEmpName;
    private LocalDateTime updatedDatetime;
    private String updatedApp;
}

