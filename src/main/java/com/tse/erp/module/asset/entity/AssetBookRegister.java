package com.tse.erp.module.asset.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "asset_book_register")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetBookRegister extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bg_id", nullable = false)
    private Long bgId;

    @Column(name = "bu_id", nullable = false)
    private Long buId;

    // fk; table: asset_item
    @Column(name = "asset_item_id")
    private Long assetItemId;

    @Column(name = "asset_no", length = 50)
    private String assetNo;

    // set / piece
    @Column(name = "asset_unit", length = 20)
    private String assetUnit;

    // fk; table: asset_item
    @Column(name = "asset_major_category_id")
    private Long assetMajorCategoryId;

    // fk; table: hr_dept
    @Column(name = "asset_owner_id")
    private Long assetOwnerId;

    @Column(name = "allocation_type", length = 50)
    private String allocationType;

    // fk; table: hr_employee
    @Column(name = "allocation_ref_id")
    private Long allocationRefId;

    @Column(name = "usage_status", length = 50)
    private String usageStatus;

    @Column(name = "asset_condition", length = 50)
    private String assetCondition;

    @Column(name = "asset_status", length = 50)
    private String assetStatus;

    @Column(name = "allocation_date")
    private LocalDate allocationDate;

    @Column(name = "purchase_through", length = 50)
    private String purchaseThrough;

    @Column(name = "purchase_order_no", length = 50)
    private String purchaseOrderNo;

    // fk; table: afm_supplier_info
    @Column(name = "supplier_id")
    private Long supplierId;

    @Column(name = "received_date")
    private LocalDate receivedDate;

    @Column(name = "war_coverage", length = 50)
    private String warCoverage;

    @Column(name = "war_provider_type", length = 50)
    private String warProviderType;

    // fk; table: afm_supplier_info
    @Column(name = "war_provider_supplier_id")
    private Long warProviderSupplierId;

    @Column(name = "war_ref_no", length = 50)
    private String warRefNo;

    @Column(name = "war_terms", length = 500)
    private String warTerms;

    @Column(name = "war_months")
    private Integer warMonths;

    @Column(name = "war_start_date")
    private LocalDate warStartDate;

    @Column(name = "war_end_date")
    private LocalDate warEndDate;

    @Column(name = "war_status", length = 50)
    private String warStatus;

    @Column(name = "guar_coverage", length = 50)
    private String guarCoverage;

    @Column(name = "guar_provider_type", length = 50)
    private String guarProviderType;

    // fk; table: afm_supplier_info
    @Column(name = "guar_provider_supplier_id")
    private Long guarProviderSupplierId;

    @Column(name = "guar_ref_no", length = 50)
    private String guarRefNo;

    @Column(name = "guar_terms", length = 500)
    private String guarTerms;

    @Column(name = "guar_months")
    private Integer guarMonths;

    @Column(name = "guar_start_date")
    private LocalDate guarStartDate;

    @Column(name = "guar_end_date")
    private LocalDate guarEndDate;

    @Column(name = "guar_status", length = 50)
    private String guarStatus;

    @Column(name = "asset_value", precision = 18, scale = 2)
    private BigDecimal assetValue;

    @Column(name = "salvage_value", precision = 18, scale = 2)
    private BigDecimal salvageValue;

    @Column(name = "depriciation_value", precision = 18, scale = 2)
    private BigDecimal depriciationValue;

    // fk; table: asset_depri_category
    @Column(name = "depriciation_method_id")
    private Long depriciationMethodId;

    @Column(name = "depriciation_frequency", length = 50)
    private String depriciationFrequency;

    @Column(name = "useful_life")
    private Integer usefulLife;

    @Column(name = "depriciation_start_date")
    private LocalDate depriciationStartDate;

    @Column(name = "depriciation_end_date")
    private LocalDate depriciationEndDate;

    @Column(name = "accumulated_depriciation", precision = 18, scale = 2)
    private BigDecimal accumulatedDepriciation;

    @Column(name = "asset_book_value", precision = 18, scale = 2)
    private BigDecimal assetBookValue;

    @Column(name = "asset_approval_status", length = 50)
    private String assetApprovalStatus;
}
