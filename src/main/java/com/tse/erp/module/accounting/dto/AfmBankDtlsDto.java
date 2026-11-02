package com.tse.erp.module.accounting.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmBankDtlsDto {

    private Long id;
    private Long bgId;
    private Long buId;
    private Long afmBankId;
    private String branchName;
    private String address;
    private String accountType;
    private String accountName;
    private String accountNumber;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate openingDate;

    private BigDecimal openingBalance;
    private Integer status;
}
