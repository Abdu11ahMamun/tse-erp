package com.tse.erp.module.inventory.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvItemWarehouseRequestDto {

    private Long bgId;
    private Long buId;
    private String warehouseName;
    private String warehouseCode;
    private Integer isDefault;
    private String storeId;
    private Integer status;
    private List<Long> employeeIds;
    private String employeeId;
}

