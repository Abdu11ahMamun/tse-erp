package com.tse.erp.module.inventory.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvItemCategoryRequestDto {

    private Long bgId;
    private Long buId;
    private String categoryName;
    private String categoryCode;
    private Integer status;
}

