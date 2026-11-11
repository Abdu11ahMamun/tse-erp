package com.tse.erp.module.inventory.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvItemAttributeRequestDto {

    private Long bgId;
    private Long buId;
    private String attributeName;
    private String attributeCode;
    private Integer status;
}

