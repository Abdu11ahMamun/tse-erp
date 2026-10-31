package com.tse.erp.module.accounting.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmCostCenterRequestDto {

    private Long bgId;
    private Long buId;
    private Long projectId;
    private Long departmentId;
    private String costCenter;
    private String shortName;
    private Integer status;
}

