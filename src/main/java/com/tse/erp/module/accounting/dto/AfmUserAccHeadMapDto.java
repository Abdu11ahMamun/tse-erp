package com.tse.erp.module.accounting.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmUserAccHeadMapDto {

    private Long id;
    private Long bgId;
    private Long buId;
    private Long userId;
    private Long accountHeadId;

    private String accountCode;
    private String accountHeadName;
    private String accountDisplayLabel;

    private Integer status;

    private Long createdUserId;
    private LocalDateTime createdDatetime;
    private Long updatedUserId;
    private LocalDateTime updatedDatetime;
}
