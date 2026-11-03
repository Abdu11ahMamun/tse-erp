package com.tse.erp.module.asset.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "asset_transfer")
@Getter
@Setter
@NoArgsConstructor
public class AssetTransfer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bg_id", nullable = false)
    private Long bgId;

    @Column(name = "bu_id", nullable = false)
    private Long buId;

    @Column(name = "item_name", nullable = false)
    private Long itemId;

    @Column(name = "from_custodian_id", nullable = false)
    private Long fromCustodianId;
    @Column(name = "from_sub_custodian_id", nullable = false)
    private Long fromSubCustodianId;
    @Column(name = "from_department_id", nullable = false)
    private Long fromDepartmentId;
    @Column(name = "from_location", nullable = false)
    private Long fromLocationId;
    @Column(name = "from_assign_date", nullable = false)
    private LocalDate fromAssignDate;
    @Column(name = "from_condition_on_issue", nullable = false, length = 20)
    private String fromConditionOnIssue;
    @Column(name = "from_remarks", length = 500)
    private String fromRemarks;

    @Column(name = "to_custodian_id", nullable = false)
    private Long toCustodianId;
    @Column(name = "to_sub_custodian_id", nullable = false)
    private Long toSubCustodianId;
    @Column(name = "to_department_id", nullable = false)
    private Long toDepartmentId;
    @Column(name = "to_location", nullable = false)
    private Long toLocationId;
    @Column(name = "to_assign_date", nullable = false)
    private LocalDate toAssignDate;
    @Column(name = "to_condition_on_issue", nullable = false, length = 20)
    private String toConditionOnIssue;
    @Column(name = "to_remarks", length = 500)
    private String toRemarks;
}