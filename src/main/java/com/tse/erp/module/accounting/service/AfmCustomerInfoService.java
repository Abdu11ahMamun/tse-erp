package com.tse.erp.module.accounting.service;

import com.tse.erp.module.accounting.dto.AfmCustFinAgreementRequestDto;
import com.tse.erp.module.accounting.dto.AfmCustFinAgreementResponseDto;
import com.tse.erp.module.accounting.dto.AfmCustomerInfoRequestDto;
import com.tse.erp.module.accounting.dto.AfmCustomerInfoResponseDto;

import java.util.List;

public interface AfmCustomerInfoService {

    List<AfmCustomerInfoResponseDto> getAllCustomers(
            Long bgId,
            Long buId,
            String query,
            String customerType,
            Integer status
    );

    AfmCustomerInfoResponseDto getCustomerById(Long id);

    AfmCustomerInfoResponseDto createCustomer(AfmCustomerInfoRequestDto request);

    AfmCustomerInfoResponseDto updateCustomer(Long id, AfmCustomerInfoRequestDto request);

    void deleteCustomer(Long id);

    AfmCustomerInfoResponseDto updateFinanceStatus(Long id, String financeStatus);

    List<AfmCustFinAgreementResponseDto> getFinancialAgreementsByCustomerId(Long customerId);

    AfmCustomerInfoResponseDto createNewAgreement(Long customerId, AfmCustFinAgreementRequestDto request);

    String generateNextCustomerCode(Long buId);
}

