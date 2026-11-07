package com.tse.erp.module.asset.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

// Request AND response
@Getter
@Setter
@NoArgsConstructor
public class GatePassDto {
    private Long id;
    private Long bgId;
    private Long buId;
    private LocalDate gatePassDate;
    private String gatePassNo;     // server generate
    private Long supplierId;
    private Long custodianId;
    private String remark;
    private Integer status;
    private List<GatePassItemDto> items;
}