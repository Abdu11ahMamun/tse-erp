package com.tse.erp.module.asset.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GatePassItemDto {
    private Long id;          // null = notun row
    private Long itemId;
    private Integer quantity;
    private Integer status;
}