package com.tse.erp.module.asset.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "asset_receive_spec")
@Getter
@Setter
@NoArgsConstructor
public class AssetReceiveSpec extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bg_id", nullable = false)
    private Long bgId;

    @Column(name = "bu_id", nullable = false)
    private Long buId;

    @Column(name = "asset_receive_id", nullable = false)
    private Long assetReceiveId;

    @Column(name = "equipment_name", nullable = false, length = 50)
    private String equipmentName;

    @Column(name = "equipment_value", nullable = false, length = 100)
    private String equipmentValue;
}
