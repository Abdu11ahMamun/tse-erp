package com.tse.erp.module.admin.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "admin_user_type_group")
@Getter
@Setter
@NoArgsConstructor
public class UserTypeGroup extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bg_id", nullable = false)
    private Long bgId;

    @Column(name = "user_type_id", nullable = false)
    private Long userTypeId;

    @Column(name = "bu_id", nullable = false)
    private Long buId;
}
