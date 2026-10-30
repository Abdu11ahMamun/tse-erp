package com.tse.erp.module.accounting.service;

import com.tse.erp.module.accounting.dto.AfmVoucherAttachDto;
import com.tse.erp.module.accounting.dto.AfmVoucherDetailDto;
import com.tse.erp.module.accounting.dto.AfmVoucherRequestDto;
import com.tse.erp.module.accounting.dto.AfmVoucherResponseDto;

import java.time.LocalDate;
import java.util.List;

public interface AfmVoucherService {

    List<AfmVoucherResponseDto> getAllVouchers(Long bgId, Long buId, String voucherType, Integer status, LocalDate fromDate, LocalDate toDate);

    AfmVoucherResponseDto getVoucherById(Long id);

    AfmVoucherResponseDto getVoucherByNo(Long buId, String voucherNo);

    List<AfmVoucherDetailDto> getVoucherDetails(Long voucherId);

    List<AfmVoucherAttachDto> getVoucherAttachments(Long voucherId);

    AfmVoucherResponseDto createVoucher(AfmVoucherRequestDto request);

    AfmVoucherResponseDto updateVoucher(Long id, AfmVoucherRequestDto request);

    void deleteVoucher(Long id);

    // Detail Sub-resource CRUD
    AfmVoucherDetailDto addVoucherDetail(Long voucherId, AfmVoucherDetailDto detailDto);

    AfmVoucherDetailDto updateVoucherDetail(Long voucherId, Long detailId, AfmVoucherDetailDto detailDto);

    void deleteVoucherDetail(Long voucherId, Long detailId);

    // Attachment Sub-resource CRUD
    AfmVoucherAttachDto addVoucherAttachment(Long voucherId, AfmVoucherAttachDto attachDto);

    AfmVoucherAttachDto updateVoucherAttachment(Long voucherId, Long attachId, AfmVoucherAttachDto attachDto);

    void deleteVoucherAttachment(Long voucherId, Long attachId);
}
