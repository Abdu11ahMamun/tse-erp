package com.tse.erp.module.accounting.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmVTypePTypeMapResponseDto {

    private Long id;
    private Long bgId;
    private Long buId;

    private Long voucherTypeId;
    private String voucherTypeValue;
    private String voucherTypeValueCode;

    private Long paymentTypeId;
    private String paymentTypeValue;
    private String paymentTypeValueCode;

    private Integer status;

    private Long createdUserId;
    private Long createdEmpId;
    private LocalDateTime createdDatetime;
    private Long updatedUserId;
    private Long updatedEmpId;
    private LocalDateTime updatedDatetime;
    private String createdApp;
    private String updatedApp;
}