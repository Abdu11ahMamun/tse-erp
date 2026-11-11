package com.tse.erp.module.accounting.service.impl;

import com.tse.erp.common.AppConstants;
import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.accounting.dto.AfmCustomerInfoRequestDto;
import com.tse.erp.module.accounting.dto.AfmCustomerInfoResponseDto;
import com.tse.erp.module.accounting.entity.AfmCustomerInfo;
import com.tse.erp.module.accounting.repository.AfmCustomerInfoRepository;
import com.tse.erp.module.accounting.service.AfmCustomerInfoService;
import com.tse.erp.module.admin.entity.BusinessGroup;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.entity.User;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.admin.repository.UserRepository;
import com.tse.erp.module.hr.entity.Employee;
import com.tse.erp.module.hr.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AfmCustomerInfoServiceImpl implements AfmCustomerInfoService {

    private final AfmCustomerInfoRepository customerInfoRepository;
    private final BusinessGroupRepository businessGroupRepository;
    private final BusinessUnitRepository businessUnitRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public List<AfmCustomerInfoResponseDto> getAllCustomers(
            Long bgId,
            Long buId,
            String query,
            String customerType,
            Integer status
    ) {
        String cleanQuery = (query != null && !query.trim().isEmpty()) ? query.trim() : null;
        String cleanType = (customerType != null && !customerType.trim().isEmpty()) ? customerType.trim() : null;

        List<AfmCustomerInfo> customers = customerInfoRepository.searchCustomers(bgId, buId, cleanQuery, cleanType, status);
        return customers.stream().map(this::toResponse).toList();
    }

    @Override
    public AfmCustomerInfoResponseDto getCustomerById(Long id) {
        return toResponse(findCustomer(id));
    }

    @Override
    public String generateNextCustomerCode(Long buId) {
        long count = customerInfoRepository.countByBuId(buId);
        long next = count + 1;
        String code = String.format("CUS-%05d", next);
        while (customerInfoRepository.existsByBuIdAndCustomerCodeIgnoreCase(buId, code)) {
            next++;
            code = String.format("CUS-%05d", next);
        }
        return code;
    }

    @Override
    @Transactional
    public AfmCustomerInfoResponseDto createCustomer(AfmCustomerInfoRequestDto request) {
        validateParents(request.getBgId(), request.getBuId());
        validateRequired(request);

        String customerCode = request.getCustomerCode();
        if (customerCode == null || customerCode.trim().isEmpty()) {
            customerCode = generateNextCustomerCode(request.getBuId());
        } else {
            customerCode = customerCode.trim();
        }

        if (customerInfoRepository.existsByBuIdAndCustomerCodeIgnoreCase(request.getBuId(), customerCode)) {
            throw new DuplicateResourceException("Customer Code already exists in this Business Unit: " + customerCode);
        }

        AfmCustomerInfo entity = AfmCustomerInfo.builder()
                .bgId(request.getBgId())
                .buId(request.getBuId())
                .customerCode(customerCode)
                .customerName(request.getCustomerName().trim())
                .customerType(cleanString(request.getCustomerType()))
                .businessType(cleanString(request.getBusinessType()))
                .contactPerson(cleanString(request.getContactPerson()))
                .designation(cleanString(request.getDesignation()))
                .phone(cleanString(request.getPhone()))
                .alternatePhone(cleanString(request.getAlternatePhone()))
                .email(cleanString(request.getEmail()))
                .website(cleanString(request.getWebsite()))
                .countryId(request.getCountryId())
                .division(cleanString(request.getDivision()))
                .district(cleanString(request.getDistrict()))
                .address(cleanString(request.getAddress()))
                .postalCode(cleanString(request.getPostalCode()))
                .shippingAddress(cleanString(request.getShippingAddress()))
                .taxRegistrationNo(cleanString(request.getTaxRegistrationNo()))
                .tinNumber(cleanString(request.getTinNumber()))
                .paymentTerms(cleanString(request.getPaymentTerms()))
                .creditLowerLimit(request.getCreditLowerLimit())
                .creditUpperLimit(request.getCreditUpperLimit())
                .creditDays(request.getCreditDays() != null ? request.getCreditDays() : 0)
                .currencyCode(request.getCurrencyCode() != null ? request.getCurrencyCode().trim() : "BDT")
                .openingBalance(request.getOpeningBalance() != null ? request.getOpeningBalance() : BigDecimal.ZERO)
                .balanceType(cleanString(request.getBalanceType()))
                .customerCategoryId(request.getCustomerCategoryId())
                .agreementDateFrom(request.getAgreementDateFrom())
                .agreementDateTo(request.getAgreementDateTo())
                .financeStatus(request.getFinanceStatus() != null && !request.getFinanceStatus().trim().isEmpty() ? request.getFinanceStatus().trim() : "Draft")
                .remarks(cleanString(request.getRemarks()))
                .build();

        Integer status = request.getStatus() != null ? request.getStatus() : 1;
        StatusUtil.validate(status);
        entity.setStatus(status);
        entity.setCreatedApp(AppConstants.APP_NAME);

        AfmCustomerInfo saved = customerInfoRepository.save(entity);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public AfmCustomerInfoResponseDto updateCustomer(Long id, AfmCustomerInfoRequestDto request) {
        AfmCustomerInfo existing = findCustomer(id);

        Long bgId = request.getBgId() != null ? request.getBgId() : existing.getBgId();
        Long buId = request.getBuId() != null ? request.getBuId() : existing.getBuId();
        validateParents(bgId, buId);
        validateRequired(request);

        String customerCode = request.getCustomerCode();
        if (customerCode != null && !customerCode.trim().isEmpty()) {
            customerCode = customerCode.trim();
            if (!customerCode.equalsIgnoreCase(existing.getCustomerCode()) &&
                customerInfoRepository.existsByBuIdAndCustomerCodeIgnoreCase(buId, customerCode)) {
                throw new DuplicateResourceException("Customer Code already exists in this Business Unit: " + customerCode);
            }
            existing.setCustomerCode(customerCode);
        }

        existing.setBgId(bgId);
        existing.setBuId(buId);
        existing.setCustomerName(request.getCustomerName().trim());
        existing.setCustomerType(cleanString(request.getCustomerType()));
        existing.setBusinessType(cleanString(request.getBusinessType()));
        existing.setContactPerson(cleanString(request.getContactPerson()));
        existing.setDesignation(cleanString(request.getDesignation()));
        existing.setPhone(cleanString(request.getPhone()));
        existing.setAlternatePhone(cleanString(request.getAlternatePhone()));
        existing.setEmail(cleanString(request.getEmail()));
        existing.setWebsite(cleanString(request.getWebsite()));
        existing.setCountryId(request.getCountryId());
        existing.setDivision(cleanString(request.getDivision()));
        existing.setDistrict(cleanString(request.getDistrict()));
        existing.setAddress(cleanString(request.getAddress()));
        existing.setPostalCode(cleanString(request.getPostalCode()));
        existing.setShippingAddress(cleanString(request.getShippingAddress()));
        existing.setTaxRegistrationNo(cleanString(request.getTaxRegistrationNo()));
        existing.setTinNumber(cleanString(request.getTinNumber()));
        existing.setPaymentTerms(cleanString(request.getPaymentTerms()));
        existing.setCreditLowerLimit(request.getCreditLowerLimit());
        existing.setCreditUpperLimit(request.getCreditUpperLimit());
        existing.setCreditDays(request.getCreditDays() != null ? request.getCreditDays() : 0);
        existing.setCurrencyCode(request.getCurrencyCode() != null ? request.getCurrencyCode().trim() : "BDT");
        existing.setOpeningBalance(request.getOpeningBalance() != null ? request.getOpeningBalance() : BigDecimal.ZERO);
        existing.setBalanceType(cleanString(request.getBalanceType()));
        existing.setCustomerCategoryId(request.getCustomerCategoryId());
        existing.setAgreementDateFrom(request.getAgreementDateFrom());
        existing.setAgreementDateTo(request.getAgreementDateTo());
        if (request.getFinanceStatus() != null && !request.getFinanceStatus().trim().isEmpty()) {
            existing.setFinanceStatus(request.getFinanceStatus().trim());
        }
        existing.setRemarks(cleanString(request.getRemarks()));

        if (request.getStatus() != null) {
            StatusUtil.validate(request.getStatus());
            existing.setStatus(request.getStatus());
        }
        existing.setUpdatedApp(AppConstants.APP_NAME);

        AfmCustomerInfo saved = customerInfoRepository.save(existing);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteCustomer(Long id) {
        AfmCustomerInfo existing = findCustomer(id);
        customerInfoRepository.delete(existing);
    }

    @Override
    @Transactional
    public AfmCustomerInfoResponseDto updateFinanceStatus(Long id, String financeStatus) {
        AfmCustomerInfo existing = findCustomer(id);
        String targetStatus = (financeStatus != null && !financeStatus.trim().isEmpty())
                ? financeStatus.trim()
                : ("Approved".equalsIgnoreCase(existing.getFinanceStatus()) ? "Draft" : "Approved");
        existing.setFinanceStatus(targetStatus);
        existing.setUpdatedApp(AppConstants.APP_NAME);
        AfmCustomerInfo saved = customerInfoRepository.save(existing);
        return toResponse(saved);
    }

    private AfmCustomerInfo findCustomer(Long id) {
        return customerInfoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
    }

    private void validateParents(Long bgId, Long buId) {
        if (bgId == null) {
            throw new BadRequestException("Business Group id cannot be empty");
        }
        if (buId == null) {
            throw new BadRequestException("Business Unit id cannot be empty");
        }

        businessGroupRepository.findById(bgId)
                .orElseThrow(() -> new ResourceNotFoundException("Business Group not found with id: " + bgId));

        BusinessUnit bu = businessUnitRepository.findById(buId)
                .orElseThrow(() -> new ResourceNotFoundException("Business Unit not found with id: " + buId));

        if (!Integer.valueOf(1).equals(bu.getStatus())) {
            throw new BadRequestException("Selected Business Unit is not active");
        }

        if (!bgId.equals(bu.getBgId())) {
            throw new BadRequestException("Business Unit does not belong to selected Business Group");
        }
    }

    private void validateRequired(AfmCustomerInfoRequestDto request) {
        if (request.getCustomerName() == null || request.getCustomerName().trim().isEmpty()) {
            throw new BadRequestException("Customer Name is required");
        }
        if (request.getCustomerName().trim().length() > 150) {
            throw new BadRequestException("Customer Name cannot exceed 150 characters");
        }
    }

    private String cleanString(String val) {
        return (val != null && !val.trim().isEmpty()) ? val.trim() : null;
    }

    private AfmCustomerInfoResponseDto toResponse(AfmCustomerInfo entity) {
        String groupName = businessGroupRepository.findById(entity.getBgId())
                .map(BusinessGroup::getGroupName)
                .orElse(null);

        String businessUnitName = businessUnitRepository.findById(entity.getBuId())
                .map(BusinessUnit::getBusinessUnit)
                .orElse(null);

        String createdUserName = null;
        if (entity.getCreatedUserId() != null) {
            createdUserName = userRepository.findById(entity.getCreatedUserId())
                    .map(User::getUserName)
                    .orElse(null);
        }

        String createdEmpName = null;
        if (entity.getCreatedEmpId() != null) {
            createdEmpName = employeeRepository.findById(entity.getCreatedEmpId())
                    .map(this::formatEmployeeDisplay)
                    .orElse(null);
        }

        String updatedUserName = null;
        if (entity.getUpdatedUserId() != null) {
            updatedUserName = userRepository.findById(entity.getUpdatedUserId())
                    .map(User::getUserName)
                    .orElse(null);
        }

        String updatedEmpName = null;
        if (entity.getUpdatedEmpId() != null) {
            updatedEmpName = employeeRepository.findById(entity.getUpdatedEmpId())
                    .map(this::formatEmployeeDisplay)
                    .orElse(null);
        }

        return AfmCustomerInfoResponseDto.builder()
                .id(entity.getId())
                .bgId(entity.getBgId())
                .groupName(groupName)
                .buId(entity.getBuId())
                .businessUnitName(businessUnitName)
                .customerCode(entity.getCustomerCode())
                .customerName(entity.getCustomerName())
                .customerType(entity.getCustomerType())
                .businessType(entity.getBusinessType())
                .contactPerson(entity.getContactPerson())
                .designation(entity.getDesignation())
                .phone(entity.getPhone())
                .alternatePhone(entity.getAlternatePhone())
                .email(entity.getEmail())
                .website(entity.getWebsite())
                .countryId(entity.getCountryId())
                .division(entity.getDivision())
                .district(entity.getDistrict())
                .address(entity.getAddress())
                .postalCode(entity.getPostalCode())
                .shippingAddress(entity.getShippingAddress())
                .taxRegistrationNo(entity.getTaxRegistrationNo())
                .tinNumber(entity.getTinNumber())
                .paymentTerms(entity.getPaymentTerms())
                .creditLowerLimit(entity.getCreditLowerLimit())
                .creditUpperLimit(entity.getCreditUpperLimit())
                .creditDays(entity.getCreditDays())
                .currencyCode(entity.getCurrencyCode())
                .openingBalance(entity.getOpeningBalance())
                .balanceType(entity.getBalanceType())
                .customerCategoryId(entity.getCustomerCategoryId())
                .agreementDateFrom(entity.getAgreementDateFrom())
                .agreementDateTo(entity.getAgreementDateTo())
                .financeStatus(entity.getFinanceStatus() != null && !entity.getFinanceStatus().trim().isEmpty() ? entity.getFinanceStatus().trim() : "Draft")
                .remarks(entity.getRemarks())
                .status(entity.getStatus())
                .createdUserId(entity.getCreatedUserId())
                .createdUserName(createdUserName)
                .createdEmpId(entity.getCreatedEmpId())
                .createdEmpName(createdEmpName)
                .createdDatetime(entity.getCreatedDatetime())
                .createdApp(entity.getCreatedApp())
                .updatedUserId(entity.getUpdatedUserId())
                .updatedUserName(updatedUserName)
                .updatedEmpId(entity.getUpdatedEmpId())
                .updatedEmpName(updatedEmpName)
                .updatedDatetime(entity.getUpdatedDatetime())
                .updatedApp(entity.getUpdatedApp())
                .build();
    }

    private String formatEmployeeDisplay(Employee emp) {
        String code = emp.getEmployeeCode() != null ? emp.getEmployeeCode().trim() : "";
        String name = emp.getEmployeeName() != null ? emp.getEmployeeName().trim() : "";
        if (!code.isEmpty() && !name.isEmpty()) {
            return code + ": " + name;
        }
        return !code.isEmpty() ? code : name;
    }
}

