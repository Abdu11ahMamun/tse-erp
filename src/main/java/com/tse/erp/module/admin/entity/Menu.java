package com.tse.erp.module.admin.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "menus")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "menu_name")
    private String menuName;

    @Column(name = "module_id")
    private Long moduleId;

    @Column(name = "is_parent", columnDefinition = "BIT")
    private Integer isParent;

    @Column(name = "parent_menu_id")
    private Long parentMenuId;

    // JSON array — String hisebe rakhbo
    @Column(name = "permission_id", columnDefinition = "LONGTEXT")
    private String permissionId;

    @Column(name = "sort_order")
    private String sortOrder;

    @Column(name = "route_name")
    private String routeName;

    @Column(name = "is_top_menu", columnDefinition = "BIT")
    private Integer isTopMenu;

    @Column(name = "is_active", columnDefinition = "BIT")
    private Integer isActive;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}