package com.tse.erp.module.accounting.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmPTypePThroughMapResponseDto {

    private Long id;
    private Long bgId;
    private Long buId;

    private Long paymentTypeId;
    private String paymentTypeValue;
    private String paymentTypeValueCode;

    private Long paymentThroughId;
    private String paymentThroughValue;
    private String paymentThroughValueCode;

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
