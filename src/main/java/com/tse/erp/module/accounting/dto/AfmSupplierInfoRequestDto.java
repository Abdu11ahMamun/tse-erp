package com.tse.erp.module.accounting.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmSupplierInfoRequestDto {

    private Long bgId;
    private Long buId;

    private String supplierCode;
    private String supplierName;
    private String supplierType;
    private String businessType;

    private String contactPerson;
    private String designation;
    private String phone;
    private String alternatePhone;
    private String email;
    private String website;

    private Long countryId;
    private String division;
    private String district;
    private String address;
    private String postalCode;

    private String taxRegistrationNo;
    private String tinNumber;
    private String tradeLicenseNo;

    private String bankName;
    private String bankBranch;
    private String bankAccountName;
    private String bankAccountNo;
    private String routingNumber;

    private String paymentTerms;
    private BigDecimal creditLimit;
    private Integer creditDays;
    private String currencyCode;
    private BigDecimal openingBalance;
    private String balanceType;

    private String remarks;
    private Integer status;
}

