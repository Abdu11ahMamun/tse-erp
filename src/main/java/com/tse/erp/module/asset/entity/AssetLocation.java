package com.tse.erp.module.asset.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "asset_location")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetLocation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bg_id", nullable = false)
    private Long bgId;

    @Column(name = "bu_id", nullable = false)
    private Long buId;

    @Column(name = "location_name", nullable = false, length = 100)
    private String locationName;

    @Column(name = "location_code", nullable = false, length = 30)
    private String locationCode;

    @Column(name = "custodian_id", nullable = false)
    private Long custodianId;

    @Column(name = "sub_custodian_id", nullable = false)
    private Long subCustodianId;

    @Column(name = "dept_id", nullable = false)
    private Long deptId;

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    @Column(name = "building", nullable = false, length = 100)
    private String building;

    @Column(name = "floor", nullable = false, length = 20)
    private String floor;

    @Column(name = "room", nullable = false, length = 50)
    private String room;
}
