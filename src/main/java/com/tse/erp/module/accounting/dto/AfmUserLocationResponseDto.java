package com.tse.erp.module.accounting.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmUserLocationResponseDto {

    private Long id;
    private Long bgId;
    private String groupName;

    private Long buId;
    private String businessUnitName;

    private String location;
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

