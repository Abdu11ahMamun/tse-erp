package com.tse.erp.module.accounting.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmSupplierInfoResponseDto {

    private Long id;
    private Long bgId;
    private String groupName;
    private Long buId;
    private String businessUnitName;

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

    private Long createdUserId;
    private String createdUserName;
    private Long createdEmpId;
    private String createdEmpName;
    private LocalDateTime createdDatetime;
    private String createdApp;

    private Long updatedUserId;
    private String updatedUserName;
    private Long updatedEmpId;
    private String updatedEmpName;
    private LocalDateTime updatedDatetime;
    private String updatedApp;
}

