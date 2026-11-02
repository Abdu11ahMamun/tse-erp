package com.tse.erp.module.accounting.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmBankChequeMapRequestDto {

    private Long bgId;
    private Long buId;
    private Long bankId;
    private Long bankAccountId;
    private String chequeNoFrom;
    private String chequeNoTo;
    private String chequeNo;
    private String chequeStatus;
    private Integer status;
}

