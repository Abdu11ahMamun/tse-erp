package com.tse.erp.module.asset.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AssetLookupValueBatchDto {

    private Long bgId;
    private Long buId;
    private Long typeId;
    private String typeCode;
    private List<AssetLookupValueRowDto> values;
}
