package com.tse.erp.module.asset.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustodianResponseDto {

    private Long id;
    private Long bgId;
    private Long buId;
    private String custodianName;
    private Integer status;
    private List<SubCustodianDto> subCustodians;

    private Long createdUserId;
    private Long createdEmpId;
    private LocalDateTime createdDatetime;
    private String createdApp;

    private Long updatedUserId;
    private Long updatedEmpId;
    private LocalDateTime updatedDatetime;
    private String updatedApp;
}
