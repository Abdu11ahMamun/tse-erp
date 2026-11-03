package com.tse.erp.module.accounting.service;

import com.tse.erp.module.accounting.dto.AfmBankChequeMapRequestDto;
import com.tse.erp.module.accounting.dto.AfmBankChequeMapResponseDto;
import com.tse.erp.module.accounting.dto.AfmBankChequeMapUpdateDto;

import java.util.List;

public interface AfmBankChequeMapService {

    List<AfmBankChequeMapResponseDto> getAllChequeMaps(
            Long bgId,
            Long buId,
            Long bankId,
            Long bankAccountId,
            String chequeStatus,
            Integer status);

    AfmBankChequeMapResponseDto getChequeMapById(Long id);

    List<AfmBankChequeMapResponseDto> createChequeBatch(AfmBankChequeMapRequestDto request);

    AfmBankChequeMapResponseDto updateChequeMap(Long id, AfmBankChequeMapUpdateDto request);

    void deleteChequeMap(Long id);
}

