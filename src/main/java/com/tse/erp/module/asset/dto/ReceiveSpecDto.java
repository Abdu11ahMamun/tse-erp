package com.tse.erp.module.asset.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReceiveSpecDto {

    private Long id;
    private String equipmentName;
    private String equipmentValue;
    private Integer status;
}
