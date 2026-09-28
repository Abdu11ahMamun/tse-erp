package com.tse.erp.module.admin.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "permission_name", length = 100, nullable = false)
    private String permissionName;

    @Column(name = "module_id", nullable = false)
    private Long moduleId;

    @Column(name = "is_active", columnDefinition = "BIT")
    private Integer isActive;
}