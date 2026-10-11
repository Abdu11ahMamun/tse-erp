package com.tse.erp.module.asset.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "asset_receive_register")
@Getter
@Setter
@NoArgsConstructor
public class AssetReceive extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bg_id", nullable = false)
    private Long bgId;

    @Column(name = "bu_id", nullable = false)
    private Long buId;

    @Column(name = "grn_no", unique = true, length = 30)
    private String grnNo;

    @Column(name = "grn_date", nullable = false)
    private LocalDate grnDate;

    @Column(name = "received_by_id", nullable = false)
    private Long receivedById;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "purchase_through", length = 50)
    private String purchaseThrough;

    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    @Column(name = "item_name_id", nullable = false)
    private Long itemNameId;

    @Column(name = "asset_no", length = 50)
    private String assetNo;

    @Column(name = "req_no", length = 50)
    private String reqNo;

    @Column(name = "po_no", length = 50)
    private String poNo;

    @Column(name = "qty", nullable = false)
    private Integer qty;

    @Column(name = "uom", nullable = false)
    private Long uomId;

    @Column(name = "unit_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "gross_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal grossAmount;

    @Column(name = "discount", precision = 18, scale = 2)
    private BigDecimal discount;

    @Column(name = "vat_tax", precision = 18, scale = 2)
    private BigDecimal vatTax;

    @Column(name = "other_charges", precision = 18, scale = 2)
    private BigDecimal otherCharges;

    @Column(name = "capitalized_cost", nullable = false, precision = 18, scale = 2)
    private BigDecimal capitalizedCost;

    @Column(name = "currency", nullable = false)
    private Long currencyId;

    @Column(name = "is_w_g_applicable", nullable = false, length = 20)
    private String wgApplicable;

    @Column(name = "w_g_period")
    private Integer wgPeriod;

    @Column(name = "w_g_type")
    private Long wgTypeId;

    @Column(name = "w_g_start_date")
    private LocalDate wgStartDate;

    @Column(name = "w_g_end_date")
    private LocalDate wgEndDate;

    @Column(name = "w_g_other_info", length = 500)
    private String wgOtherInfo;

    @Column(name = "custodian_id", nullable = false)
    private Long custodianId;

    @Column(name = "sub_custodian_id", nullable = false)
    private Long subCustodianId;

    @Column(name = "location", nullable = false)
    private Long locationId;

    @Column(name = "building", length = 100)
    private String building;

    @Column(name = "floor", length = 20)
    private String floor;

    @Column(name = "room", length = 50)
    private String room;

    @Column(name = "rac", length = 50)
    private String rac;

    @Column(name = "bin", length = 50)
    private String bin;

    @Column(name = "depri_category", nullable = false)
    private Long depriCategoryId;

    @Column(name = "depri_method", nullable = false)
    private Long depriMethodId;

    @Column(name = "asset_type", nullable = false)
    private Long assetTypeId;

    @Column(name = "depri_life", nullable = false)
    private Integer depriLife;

    @Column(name = "depri_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal depriPercentage;

    @Column(name = "asset_owner", nullable = false)
    private Long assetOwnerId;

    @Column(name = "remarks", length = 500)
    private String remarks;
}
