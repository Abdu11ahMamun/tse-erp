package com.tse.erp.module.hr.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "hr_degree")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Degree extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "level_of_edu_id", nullable = false)
    private Long levelOfEduId;

    @Column(name = "degree_name", nullable = false, length = 50)
    private String degreeName;

    @Column(name = "short_name", length = 5)
    private String shortName;

    @Column(name = "sort_order")
    private Integer sortOrder;
}
