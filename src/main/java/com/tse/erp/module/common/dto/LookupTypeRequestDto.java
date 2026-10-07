package com.tse.erp.module.common.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LookupTypeRequestDto {
    @NotNull(message = "Business Group is required")
    private Long bgId;

    @NotNull(message = "Business Unit is required")
    private Long buId;

    private String typeName;   // length rules service-e (trim er por)
    private String typeCode;   // optional

    @Min(0) @Max(1)
    private Integer status;    // null hole 1 (Active)
}