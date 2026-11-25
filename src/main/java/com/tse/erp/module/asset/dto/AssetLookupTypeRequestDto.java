package com.tse.erp.module.asset.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetLookupTypeRequestDto {

    private Long id;
    private Long bgId;
    private Long buId;
    private String typeName;
    private String typeCode;
    private Integer status;

    private List<AssetLookupValueRowDto> lookupValues;
}
