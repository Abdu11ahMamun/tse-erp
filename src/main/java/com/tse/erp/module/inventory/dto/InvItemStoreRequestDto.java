package com.tse.erp.module.inventory.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvItemStoreRequestDto {

    private Long bgId;
    private Long buId;
    private String storeName;
    private String storeCode;
    private Integer isDefault;
    private Integer status;
    private List<Long> employeeIds;
    private String employeeId;
}

