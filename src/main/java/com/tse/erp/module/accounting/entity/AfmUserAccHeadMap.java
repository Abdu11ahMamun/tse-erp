package com.tse.erp.module.accounting.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "afm_user_acc_head_map")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmUserAccHeadMap extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "bg_id", nullable = false)
    private Long bgId;

    @Column(name = "bu_id", nullable = false)
    private Long buId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "account_head_id", nullable = false)
    private Long accountHeadId;
}
