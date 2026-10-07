package com.tse.erp.module.asset.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "asset_custodian_dtl")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustodianDtl extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "custodian_id", nullable = false)
    private Long custodianId;

    @Column(name = "sub_custodian_name", nullable = false, length = 100)
    private String subCustodianName;
}
