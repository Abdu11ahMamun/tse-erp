package com.tse.erp.module.inventory.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvItemRequestDto {

    private Long bgId;
    private Long buId;
    private String itemName;
    private String itemCode;

    private Long categoryId;
    private Long brandId;
    private Long uomId;
    private String storeId;

    private String itemImage;
    private Integer isWarrantyApplicable;
    private Integer warrantyDay;
    private String warrantyCriteria;

    private Integer status;

    private List<InvItemAttributeDtlsDto> attributeDetails;
}

