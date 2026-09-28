package com.tse.erp.module.admin.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "admin_bu_role_map")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuRoleMap extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @Column(name = "bu_id", nullable = false)
    private Long buId;
}