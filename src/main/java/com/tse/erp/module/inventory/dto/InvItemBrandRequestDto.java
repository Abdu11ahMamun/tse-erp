package com.tse.erp.module.inventory.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvItemBrandRequestDto {

    private Long bgId;
    private Long buId;
    private String brandName;
    private String brandCode;
    private Integer status;
}

