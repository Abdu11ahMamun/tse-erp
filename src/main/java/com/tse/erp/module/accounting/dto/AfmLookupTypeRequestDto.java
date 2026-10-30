package com.tse.erp.module.accounting.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmLookupTypeRequestDto {

    private Long bgId;
    private Long buId;
    private String typeName;
    private String typeCode;
    private Integer status;

    private List<AfmLookupValueDto> lookupValues;
}
