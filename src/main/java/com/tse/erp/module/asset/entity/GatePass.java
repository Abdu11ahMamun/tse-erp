package com.tse.erp.module.asset.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "asset_gate_pass")
@Getter
@Setter
@NoArgsConstructor
public class GatePass extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bg_id", nullable = false)
    private Long bgId;

    @Column(name = "bu_id", nullable = false)
    private Long buId;

    @Column(name = "gate_pass_date", nullable = false)
    private LocalDate gatePassDate;

    @Column(name = "gate_pass_no", length = 30, unique = true)
    private String gatePassNo;

    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    @Column(name = "custodian", nullable = false)
    private Long custodianId;

    @Column(name = "remark", length = 500)
    private String remark;
}