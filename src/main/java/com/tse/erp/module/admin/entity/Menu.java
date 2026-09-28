package com.tse.erp.module.admin.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "menus")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Menu extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "menu_name", length = 100, nullable = false)
    private String menuName;

    @Column(name = "module_id", nullable = false)
    private Long moduleId;

    @Column(name = "is_parent", columnDefinition = "BIT")
    private Integer isParent;

    @Column(name = "parent_menu_id")
    private Long parentMenuId;

    @Column(name = "is_top_menu", columnDefinition = "BIT")
    private Integer isTopMenu;

    // JSON array — permission ids
    @Column(name = "permission_id", columnDefinition = "LONGTEXT")
    private String permissionId;

    @Column(name = "route_name", length = 100)
    private String routeName;

    @Column(name = "sort_order")
    private String sortOrder;

    @Column(name = "is_active", columnDefinition = "BIT")
    private Integer isActive;
}