package com.tse.erp.module.hr.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "hr_level_of_edu")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LevelOfEducation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "level_of_edu", nullable = false, length = 50)
    private String levelOfEdu;

    @Column(name = "short_name", length = 5)
    private String shortName;

    @Column(name = "sort_order")
    private Integer sortOrder;
}
