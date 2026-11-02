package com.tse.erp.module.accounting.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfmVoucherAttachDto {

    private Long id;
    private Long bgId;
    private Long buId;
    private Long voucherId;
    private String attachmentFileName;
    private String attachmentFile;
    private Integer isActive;
    private Integer status;
}
