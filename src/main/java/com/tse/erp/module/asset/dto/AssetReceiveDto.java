package com.tse.erp.module.asset.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AssetReceiveDto {

    private Long id;
    private String grnNo;
    private Long bgId;
    private Long buId;
    private LocalDate grnDate;
    private LocalDate purchaseDate;
    private String purchaseThrough;
    private Long receivedById;
    private Long supplierId;
    private Long itemNameId;
    private String assetNo;
    private String reqNo;
    private String poNo;
    private Integer qty;
    private Long uomId;
    private BigDecimal unitPrice;
    private BigDecimal grossAmount;
    private BigDecimal discount;
    private BigDecimal vatTax;
    private BigDecimal otherCharges;
    private BigDecimal capitalizedCost;
    private Long currencyId;
    private String wgApplicable;
    private Integer wgPeriod;
    private Long wgTypeId;
    private LocalDate wgStartDate;
    private LocalDate wgEndDate;
    private String wgOtherInfo;
    private Long custodianId;
    private Long subCustodianId;
    private Long locationId;
    private String building;
    private String floor;
    private String room;
    private String rac;
    private String bin;
    private Long depriCategoryId;
    private Long depriMethodId;
    private Long assetTypeId;
    private Integer depriLife;
    private BigDecimal depriPercentage;
    private Long assetOwnerId;
    private String remarks;
    private Integer status;
    private List<ReceiveSpecDto> specs;
}
