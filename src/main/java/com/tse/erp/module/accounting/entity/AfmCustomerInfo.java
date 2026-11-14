package com.tse.erp.module.accounting.entity;

import com.tse.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "AFM_CUSTOMER_INFO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmCustomerInfo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "BG_ID", nullable = false)
    private Long bgId;

    @Column(name = "BU_ID", nullable = false)
    private Long buId;

    @Column(name = "CUSTOMER_CODE", length = 50, nullable = false)
    private String customerCode;

    @Column(name = "CUSTOMER_NAME", length = 150, nullable = false)
    private String customerName;

    @Column(name = "CUSTOMER_TYPE", length = 50)
    private String customerType;

    @Column(name = "BUSINESS_TYPE", length = 50)
    private String businessType;

    @Column(name = "CONTACT_PERSON", length = 100)
    private String contactPerson;

    @Column(name = "DESIGNATION", length = 100)
    private String designation;

    @Column(name = "PHONE", length = 50)
    private String phone;

    @Column(name = "ALTERNATE_PHONE", length = 50)
    private String alternatePhone;

    @Column(name = "EMAIL", length = 100)
    private String email;

    @Column(name = "WEBSITE", length = 150)
    private String website;

    @Column(name = "COUNTRY_ID")
    private Long countryId;

    @Column(name = "DIVISION", length = 100)
    private String division;

    @Column(name = "DISTRICT", length = 100)
    private String district;

    @Column(name = "ADDRESS", length = 500)
    private String address;

    @Column(name = "POSTAL_CODE", length = 50)
    private String postalCode;

    @Column(name = "SHIPPING_ADDRESS", length = 500)
    private String shippingAddress;

    @Column(name = "TAX_REGISTRATION_NO", length = 100)
    private String taxRegistrationNo;

    @Column(name = "TIN_NUMBER", length = 100)
    private String tinNumber;

    @Column(name = "PAYMENT_TERMS", length = 50)
    private String paymentTerms;

    @Column(name = "CREDIT_LOWER_LIMIT", precision = 18, scale = 2)
    private BigDecimal creditLowerLimit;

    @Column(name = "CREDIT_UPPER_LIMIT", precision = 18, scale = 2)
    private BigDecimal creditUpperLimit;

    @Column(name = "AGREEMENT_DATE_FROM")
    private LocalDate agreementDateFrom;

    @Column(name = "AGREEMENT_DATE_TO")
    private LocalDate agreementDateTo;

    @Column(name = "FINANCE_STATUS", length = 20)
    private String financeStatus;

    @Column(name = "CREDIT_DAYS")
    private Integer creditDays;

    @Column(name = "CURRENCY_CODE", length = 20)
    private String currencyCode;

    @Column(name = "OPENING_BALANCE", precision = 18, scale = 2)
    private BigDecimal openingBalance;

    @Column(name = "BALANCE_TYPE", length = 20)
    private String balanceType;

    @Column(name = "CUSTOMER_CATEGORY_ID")
    private Long customerCategoryId;

    @Column(name = "REMARKS", length = 500)
    private String remarks;
}

