package com.tse.erp.module.accounting.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmCustomerInfoRequestDto {

    private Long bgId;
    private Long buId;

    private String customerCode;
    private String customerName;
    private String customerType;
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
    private String shippingAddress;

    private String taxRegistrationNo;
    private String tinNumber;

    private String paymentTerms;
    private BigDecimal creditLowerLimit;
    private BigDecimal creditUpperLimit;
    private Integer creditDays;
    private String currencyCode;
    private BigDecimal openingBalance;
    private String balanceType;
    private LocalDate agreementDateFrom;
    private LocalDate agreementDateTo;
    private String financeStatus;

    private Long customerCategoryId;
    private String remarks;
    private Integer status;
}

