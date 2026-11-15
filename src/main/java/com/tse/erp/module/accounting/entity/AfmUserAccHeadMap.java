package com.tse.erp.module.accounting.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "AFM_USER_ACC_HEAD_MAP")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmUserAccHeadMap extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "BG_ID", nullable = false)
    private Long bgId;

    @Column(name = "BU_ID", nullable = false)
    private Long buId;

    @Column(name = "USER_ID", nullable = false)
    private Long userId;

    @Column(name = "ACCOUNT_HEAD_ID", nullable = false)
    private Long accountHeadId;
}
