package com.tse.erp.module.asset.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubCustodianDto {

    private Long id;
    private String subCustodianName;
    private Integer status;
}
