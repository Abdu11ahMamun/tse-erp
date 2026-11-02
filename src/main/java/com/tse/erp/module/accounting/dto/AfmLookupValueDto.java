package com.tse.erp.module.accounting.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmLookupValueDto {

    private Long id;
    private Long bgId;
    private Long buId;
    private Long typeNameId;
    private String typeCode;
    private String value;
    private String valueCode;
    private String note;
    private BigDecimal sortOrder;
    private Integer status;
    private Boolean inUse;
}
