package com.tse.erp.module.asset.dto;

import lombok.*;

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
}
