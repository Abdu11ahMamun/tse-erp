package com.tse.erp.module.common.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class LookupValueBatchDto {

    private Long bgId;
    private Long buId;
    private Long typeId;
    private String typeCode;
    private List<LookupValueRowDto> values;
}
