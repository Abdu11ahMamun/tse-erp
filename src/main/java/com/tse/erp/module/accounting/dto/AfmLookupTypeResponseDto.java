package com.tse.erp.module.accounting.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmLookupTypeResponseDto {

    private Long id;
    private Long bgId;
    private Long buId;
    private String typeName;
    private String typeCode;
    private Integer status;

    private Long createdUserId;
    private Long createdEmpId;
    private LocalDateTime createdDatetime;
    private Long updatedUserId;
    private Long updatedEmpId;
    private LocalDateTime updatedDatetime;
    private String createdApp;
    private String updatedApp;

    private List<AfmLookupValueDto> lookupValues;
}
