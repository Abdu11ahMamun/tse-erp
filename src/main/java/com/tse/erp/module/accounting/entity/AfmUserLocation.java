package com.tse.erp.module.accounting.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "afm_user_location")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmUserLocation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "bg_id", nullable = false)
    private Long bgId;

    @Column(name = "bu_id", nullable = false)
    private Long buId;

    @Column(name = "location", nullable = false, length = 150)
    private String location;

    @Column(name = "short_name", length = 50)
    private String shortName;
}

