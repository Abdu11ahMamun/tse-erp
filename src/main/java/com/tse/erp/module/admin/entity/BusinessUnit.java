package com.tse.erp.module.admin.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "admin_bu")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessUnit extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bg_id", nullable = false)
    private Long bgId;

    @Column(name = "business_unit")
    private String businessUnit;

    @Column(name = "bu_logo")
    private String buLogo;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "e_mail")
    private String email;

    @Column(name = "mobile_no", length = 11)
    private String mobileNo;

    @Column(name = "web_address")
    private String webAddress;

    @Column(name = "report_header", length = 500)
    private String reportHeader;

    @Column(name = "report_footer", length = 500)
    private String reportFooter;
}