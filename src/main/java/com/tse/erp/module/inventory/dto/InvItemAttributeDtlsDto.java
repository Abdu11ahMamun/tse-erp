package com.tse.erp.module.inventory.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvItemAttributeDtlsDto {

    private Long id;
    private Long bgId;
    private Long buId;
    private Long itemId;

    private Long attributeId;
    private String attributeName;
    private String attributeCode;

    private BigDecimal price;
    private BigDecimal openingStock;
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

