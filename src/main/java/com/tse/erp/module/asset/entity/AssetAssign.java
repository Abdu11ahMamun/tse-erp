package com.tse.erp.module.asset.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "asset_assign_to_empl")
@Getter
@Setter
@NoArgsConstructor
public class AssetAssign extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bg_id", nullable = false)
    private Long bgId;

    @Column(name = "bu_id", nullable = false)
    private Long buId;

    @Column(name = "asset_date", nullable = false)
    private LocalDate assetDate;

    // FK: asset_item
    @Column(name = "item_name", nullable = false)
    private Long itemId;

    // FK: hr_employee
    @Column(name = "hr_empl_id", nullable = false)
    private Long emplId;

    @Column(name = "actual_return_date")
    private LocalDate actualReturnDate;

    @Column(name = "expected_return_date")
    private LocalDate expectedReturnDate;

    @Column(name = "condition_on_issue", nullable = false, length = 20)
    private String conditionOnIssue;

    @Column(name = "remarks", length = 500)
    private String remarks;
}