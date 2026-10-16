package com.tse.erp.module.hr.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "hr_office_notice")
@Getter
@Setter
@NoArgsConstructor
public class OfficeNotice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bg_id", nullable = false)
    private Long bgId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "publish_date", nullable = false)
    private LocalDate publishDate;

    @Column(name = "expire_date", nullable = false)
    private LocalDate expireDate;

    @Column(name = "attachment", length = 255)
    private String attachment;

    @Column(name = "is_important", nullable = false, columnDefinition = "TINYINT")
    private Integer isImportant;

    @Column(name = "description", length = 5000)
    private String description;
}