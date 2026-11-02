package com.tse.erp.module.accounting.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmCostCenterResponseDto {

    private Long id;
    private Long bgId;
    private Long buId;

    private Long projectId;

    private Long departmentId;
    private String departmentName;
    private String departmentShortName;

    private String costCenter;
    private String shortName;
    private Integer status;

    private Long createdUserId;
    private Long createdEmpId;
    private LocalDateTime createdDatetime;
    private Long updatedUserId;
    private Long updatedEmpId;
    private LocalDateTime updatedDatetime;
    private String createdApp;
    private String updatedApp;
}

