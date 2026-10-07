package com.tse.erp.module.common.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LookupTypeResponseDto {
    private Long id;
    private Long bgId;
    private Long buId;
    private String typeName;
    private String typeCode;
    private Integer status;
}