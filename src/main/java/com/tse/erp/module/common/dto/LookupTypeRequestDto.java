package com.tse.erp.module.common.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LookupTypeRequestDto {

    private Long id;
    private Long bgId;
    private Long buId;
    private String typeName;
    private String typeCode;
    private Integer status;

    private List<LookupValueRowDto> lookupValues;
}
