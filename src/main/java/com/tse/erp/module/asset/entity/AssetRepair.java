package com.tse.erp.module.asset.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "asset_repair")
@Getter
@Setter
@NoArgsConstructor
public class AssetRepair extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bg_id", nullable = false)
    private Long bgId;

    @Column(name = "bu_id", nullable = false)
    private Long buId;

    @Column(name = "item_name", nullable = false)
    private Long itemId;

    @Column(name = "receive_date", nullable = false)
    private LocalDate receiveDate;

    // Vendor / service provider
    @Column(name = "receive_id", nullable = false)
    private Long receiveId;

    @Column(name = "prob_spec", nullable = false, length = 1000)
    private String probSpec;

    @Column(name = "support_engineer", nullable = false, length = 100)
    private String supportEngineer;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "finish_date")
    private LocalDate finishDate;

    @Column(name = "delivery_date")
    private LocalDate deliveryDate;

    @Column(name = "remark", length = 500)
    private String remark;
}