package com.tse.erp.module.accounting.service;

import com.tse.erp.module.accounting.dto.AfmLedgerAttachDto;
import com.tse.erp.module.accounting.dto.AfmLedgerDetailDto;
import com.tse.erp.module.accounting.dto.AfmLedgerRequestDto;
import com.tse.erp.module.accounting.dto.AfmLedgerResponseDto;
import com.tse.erp.module.accounting.entity.AfmVoucher;
import com.tse.erp.module.accounting.entity.AfmVoucherAttach;
import com.tse.erp.module.accounting.entity.AfmVoucherDetail;

import java.time.LocalDate;
import java.util.List;

public interface AfmLedgerService {

    List<AfmLedgerResponseDto> getAllLedgers(Long bgId, Long buId, String voucherType, Integer status, LocalDate fromDate, LocalDate toDate);

    AfmLedgerResponseDto getLedgerById(Long id);

    AfmLedgerResponseDto getLedgerByNo(Long buId, String voucherNo);

    List<AfmLedgerDetailDto> getLedgerDetails(Long ledgerId);

    List<AfmLedgerAttachDto> getLedgerAttachments(Long ledgerId);

    AfmLedgerResponseDto createLedger(AfmLedgerRequestDto request);

    AfmLedgerResponseDto updateLedger(Long id, AfmLedgerRequestDto request);

    void deleteLedger(Long id);

    // Sync methods from AfmVoucher -> AfmLedger
    void syncFromVoucherCreate(AfmVoucher voucher, List<AfmVoucherDetail> details, List<AfmVoucherAttach> attaches);

    void syncFromVoucherUpdate(AfmVoucher voucher, List<AfmVoucherDetail> details, List<AfmVoucherAttach> attaches);

    void syncFromVoucherDelete(Long voucherId, Long buId, String voucherNo);
}
