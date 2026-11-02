package com.tse.erp.module.accounting.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "AFM_PERIOD")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmPeriod extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "BG_ID", nullable = false)
    private Long bgId;

    @Column(name = "BU_ID", nullable = false)
    private Long buId;

    @Column(name = "PERIOD_YEAR", nullable = false, length = 50)
    private String periodYear;

    @Column(name = "PERIOD_NAME", nullable = false, length = 100)
    private String periodName;

    @Column(name = "PERIOD_STATUS", nullable = false, length = 20)
    private String periodStatus;
}

