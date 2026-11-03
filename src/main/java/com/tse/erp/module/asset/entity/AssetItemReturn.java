package com.tse.erp.module.asset.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "asset_item_return")
@Getter
@Setter
@NoArgsConstructor
public class AssetItemReturn extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bg_id", nullable = false)
    private Long bgId;

    @Column(name = "bu_id", nullable = false)
    private Long buId;

    // Original assignment
    @Column(name = "assign_id", nullable = false)
    private Long assignId;

    @Column(name = "asset_date", nullable = false)
    private LocalDate assetDate;

    @Column(name = "item_name", nullable = false)
    private Long itemId;

    @Column(name = "hr_empl_id", nullable = false)
    private Long emplId;

    @Column(name = "expected_return_date")
    private LocalDate expectedReturnDate;

    @Column(name = "actual_return_date", nullable = false)
    private LocalDate actualReturnDate;

    @Column(name = "condition_on_return", nullable = false, length = 20)
    private String conditionOnReturn;

    @Column(name = "remarks", length = 500)
    private String remarks;
}