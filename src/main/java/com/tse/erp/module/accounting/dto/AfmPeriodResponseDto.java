package com.tse.erp.module.accounting.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmPeriodResponseDto {

    private Long id;
    private Long bgId;
    private String groupName;

    private Long buId;
    private String businessUnitName;

    private String periodYear;
    private String periodName;
    private String periodStatus;

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

