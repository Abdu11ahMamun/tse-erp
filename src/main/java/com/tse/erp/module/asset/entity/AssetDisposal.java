package com.tse.erp.module.asset.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "asset_disposal")
@Getter
@Setter
@NoArgsConstructor
public class AssetDisposal extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bg_id", nullable = false)
    private Long bgId;

    @Column(name = "bu_id", nullable = false)
    private Long buId;

    @Column(name = "item_name", nullable = false)
    private Long itemId;

    @Column(name = "disposal_date", nullable = false)
    private LocalDate disposalDate;

    @Column(name = "disposal_type", nullable = false, length = 20)
    private String disposalType;

    @Column(name = "disposal_value", nullable = false, precision = 18, scale = 2)
    private BigDecimal disposalValue;

    // Snapshot (spec-e nai, gain/loss-er jonno)
    @Column(name = "written_down_value", nullable = false, precision = 18, scale = 2)
    private BigDecimal writtenDownValue;

    @Column(name = "gain_on_disposal", nullable = false, precision = 18, scale = 2)
    private BigDecimal gainOnDisposal;

    @Column(name = "loss_on_disposal", nullable = false, precision = 18, scale = 2)
    private BigDecimal lossOnDisposal;

    // Employee id
    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "reason", nullable = false, length = 100)
    private String reason;

    @Column(name = "remark", length = 500)
    private String remark;

    @Column(name = "disposal_status", nullable = false, length = 30)
    private String disposalStatus;
}