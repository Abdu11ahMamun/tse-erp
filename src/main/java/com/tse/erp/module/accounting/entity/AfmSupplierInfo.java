package com.tse.erp.module.accounting.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "afm_supplier_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmSupplierInfo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "bg_id", nullable = false)
    private Long bgId;

    @Column(name = "bu_id", nullable = false)
    private Long buId;

    @Column(name = "supplier_code", length = 50, nullable = false)
    private String supplierCode;

    @Column(name = "supplier_name", length = 150, nullable = false)
    private String supplierName;

    @Column(name = "supplier_type", length = 50)
    private String supplierType;

    @Column(name = "business_type", length = 50)
    private String businessType;

    @Column(name = "contact_person", length = 100)
    private String contactPerson;

    @Column(name = "designation", length = 100)
    private String designation;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "alternate_phone", length = 50)
    private String alternatePhone;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "website", length = 150)
    private String website;

    @Column(name = "country_id")
    private Long countryId;

    @Column(name = "division", length = 100)
    private String division;

    @Column(name = "district", length = 100)
    private String district;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "postal_code", length = 50)
    private String postalCode;

    @Column(name = "tax_registration_no", length = 100)
    private String taxRegistrationNo;

    @Column(name = "tin_number", length = 100)
    private String tinNumber;

    @Column(name = "trade_license_no", length = 100)
    private String tradeLicenseNo;

    @Column(name = "bank_name", length = 150)
    private String bankName;

    @Column(name = "bank_branch", length = 150)
    private String bankBranch;

    @Column(name = "bank_account_name", length = 150)
    private String bankAccountName;

    @Column(name = "bank_account_no", length = 50)
    private String bankAccountNo;

    @Column(name = "routing_number", length = 50)
    private String routingNumber;

    @Column(name = "payment_terms", length = 50)
    private String paymentTerms;

    @Column(name = "credit_limit", precision = 18, scale = 2)
    private BigDecimal creditLimit;

    @Column(name = "credit_days")
    private Integer creditDays;

    @Column(name = "currency_code", length = 20)
    private String currencyCode;

    @Column(name = "opening_balance", precision = 18, scale = 2)
    private BigDecimal openingBalance;

    @Column(name = "balance_type", length = 20)
    private String balanceType;

    @Column(name = "remarks", length = 500)
    private String remarks;
}

