package com.tse.erp.module.asset.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetBookRegisterDto {

    private Long id;
    private Long bgId;
    private Long buId;

    private Long assetItemId;
    private String assetNo;
    private String assetUnit;
    private Long assetMajorCategoryId;
    private Long assetOwnerId;
    private String allocationType;
    private Long allocationRefId;
    private String usageStatus;
    private String assetCondition;
    private String assetStatus;
    private LocalDate allocationDate;

    private String purchaseThrough;
    private String purchaseOrderNo;
    private Long supplierId;
    private LocalDate receivedDate;

    private String warCoverage;
    private String warProviderType;
    private Long warProviderSupplierId;
    private String warRefNo;
    private String warTerms;
    private Integer warMonths;
    private LocalDate warStartDate;
    private LocalDate warEndDate;
    private String warStatus;

    private String guarCoverage;
    private String guarProviderType;
    private Long guarProviderSupplierId;
    private String guarRefNo;
    private String guarTerms;
    private Integer guarMonths;
    private LocalDate guarStartDate;
    private LocalDate guarEndDate;
    private String guarStatus;

    private BigDecimal assetValue;
    private BigDecimal salvageValue;
    private BigDecimal depriciationValue;
    private Long depriciationMethodId;
    private String depriciationFrequency;
    private Integer usefulLife;
    private LocalDate depriciationStartDate;
    private LocalDate depriciationEndDate;

    private BigDecimal accumulatedDepriciation;
    private BigDecimal assetBookValue;
    private String assetApprovalStatus;

    // Audit Info
    private Long createdUserId;
    private Long createdEmpId;
    private LocalDateTime createdDatetime;
    private Long updatedUserId;
    private Long updatedEmpId;
    private LocalDateTime updatedDatetime;
    private String createdApp;
    private String updatedApp;
    private Integer status;
}
