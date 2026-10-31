package com.tse.erp.module.accounting.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "AFM_BANK")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmBank extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "BG_ID", nullable = false)
    private Long bgId;

    @Column(name = "BU_ID", nullable = false)
    private Long buId;

    @Column(name = "BANK_NAME", nullable = false, length = 150)
    private String bankName;

    @Column(name = "SHORT_NAME", length = 50)
    private String shortName;
}
