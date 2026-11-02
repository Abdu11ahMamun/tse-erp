package com.tse.erp.module.accounting.service;

import com.tse.erp.module.accounting.dto.AfmBankDtlsDto;
import com.tse.erp.module.accounting.dto.AfmBankRequestDto;
import com.tse.erp.module.accounting.dto.AfmBankResponseDto;

import java.util.List;

public interface AfmBankService {

    List<AfmBankResponseDto> getAllBanks(Long bgId, Long buId, Integer status);

    AfmBankResponseDto getBankById(Long id);

    List<AfmBankDtlsDto> getBankDetails(Long bankId);

    AfmBankResponseDto createBank(AfmBankRequestDto request);

    AfmBankResponseDto updateBank(Long id, AfmBankRequestDto request);

    void deleteBank(Long id);

    AfmBankDtlsDto addBankDetail(Long bankId, AfmBankDtlsDto detailDto);

    AfmBankDtlsDto updateBankDetail(Long bankId, Long detailId, AfmBankDtlsDto detailDto);

    void deleteBankDetail(Long bankId, Long detailId);
}
