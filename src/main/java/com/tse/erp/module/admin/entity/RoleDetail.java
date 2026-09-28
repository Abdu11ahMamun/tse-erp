package com.tse.erp.module.admin.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "admin_role_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleDetail extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @Column(name = "module_id", nullable = false)
    private Long moduleId;

    @Column(name = "menu_id", nullable = false)
    private Long menuId;

    // JSON array store hocche — String hisebe rakhbo
    @Column(name = "permission_id", columnDefinition = "LONGTEXT")
    private String permissionId;
}