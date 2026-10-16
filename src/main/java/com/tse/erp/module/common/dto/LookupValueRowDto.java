package com.tse.erp.module.common.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class LookupValueRowDto {

    private Long id;
    private String value;
    private String valueCode;
    private BigDecimal sortOrder;
    private Integer status;
}
