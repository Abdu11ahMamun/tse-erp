package com.tse.erp.module.admin.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "admin_users_empl_map")
@Getter
@Setter
@NoArgsConstructor
public class UserEmplMap extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "empl_id", nullable = false)
    private Long emplId;

    @Column(name = "active_from")
    private java.time.LocalDateTime activeFrom;

    @Column(name = "active_to")
    private java.time.LocalDateTime activeTo;
}
