package com.tse.erp.module.admin.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "admin_user_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserType extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bg_id")
    private Long bgId;

    @Column(name = "type_name", length = 50)
    private String typeName;

    @Column(name = "short_name", length = 5)
    private String shortName;

    @Column(name = "sort_order", precision = 10, scale = 2)
    private java.math.BigDecimal sortOrder;
}