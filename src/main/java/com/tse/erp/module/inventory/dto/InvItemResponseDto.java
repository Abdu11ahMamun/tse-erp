package com.tse.erp.module.inventory.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvItemResponseDto {

    private Long id;
    private Long bgId;
    private String groupName;

    private Long buId;
    private String businessUnitName;

    private String itemName;
    private String itemCode;

    private Long categoryId;
    private String categoryName;
    private String categoryCode;

    private Long brandId;
    private String brandName;
    private String brandCode;

    private Long uomId;
    private String uomName;

    private String storeId;
    private List<Long> storeIds;
    private List<String> storeNames;
    private String storeName;
    private String storeCode;

    private String itemImage;
    private Integer isWarrantyApplicable;
    private Integer warrantyDay;
    private String warrantyCriteria;

    private Integer status;

    private Long createdUserId;
    private String createdUserName;
    private Long createdEmpId;
    private String createdEmpName;
    private LocalDateTime createdDatetime;
    private String createdApp;

    private Long updatedUserId;
    private String updatedUserName;
    private Long updatedEmpId;
    private String updatedEmpName;
    private LocalDateTime updatedDatetime;
    private String updatedApp;

    private List<InvItemAttributeDtlsDto> attributeDetails;
}

