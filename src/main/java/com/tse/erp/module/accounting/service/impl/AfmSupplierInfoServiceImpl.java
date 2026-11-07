package com.tse.erp.module.accounting.service.impl;

import com.tse.erp.common.AppConstants;
import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.accounting.dto.AfmSupplierInfoRequestDto;
import com.tse.erp.module.accounting.dto.AfmSupplierInfoResponseDto;
import com.tse.erp.module.accounting.entity.AfmSupplierInfo;
import com.tse.erp.module.accounting.repository.AfmSupplierInfoRepository;
import com.tse.erp.module.accounting.service.AfmSupplierInfoService;
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
public class AfmSupplierInfoServiceImpl implements AfmSupplierInfoService {

    private final AfmSupplierInfoRepository supplierInfoRepository;
    private final BusinessGroupRepository businessGroupRepository;
    private final BusinessUnitRepository businessUnitRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public List<AfmSupplierInfoResponseDto> getAllSuppliers(
            Long bgId,
            Long buId,
            String query,
            String supplierType,
            Integer status
    ) {
        String cleanQuery = (query != null && !query.trim().isEmpty()) ? query.trim() : null;
        String cleanType = (supplierType != null && !supplierType.trim().isEmpty()) ? supplierType.trim() : null;

        List<AfmSupplierInfo> suppliers = supplierInfoRepository.searchSuppliers(bgId, buId, cleanQuery, cleanType, status);
        return suppliers.stream().map(this::toResponse).toList();
    }

    @Override
    public AfmSupplierInfoResponseDto getSupplierById(Long id) {
        return toResponse(findSupplier(id));
    }

    @Override
    public String generateNextSupplierCode(Long buId) {
        long count = supplierInfoRepository.countByBuId(buId);
        long next = count + 1;
        String code = String.format("SUP-%05d", next);
        while (supplierInfoRepository.existsByBuIdAndSupplierCodeIgnoreCase(buId, code)) {
            next++;
            code = String.format("SUP-%05d", next);
        }
        return code;
    }

    @Override
    @Transactional
    public AfmSupplierInfoResponseDto createSupplier(AfmSupplierInfoRequestDto request) {
        validateParents(request.getBgId(), request.getBuId());
        validateRequired(request);

        String supplierCode = request.getSupplierCode();
        if (supplierCode == null || supplierCode.trim().isEmpty()) {
            supplierCode = generateNextSupplierCode(request.getBuId());
        } else {
            supplierCode = supplierCode.trim();
        }

        if (supplierInfoRepository.existsByBuIdAndSupplierCodeIgnoreCase(request.getBuId(), supplierCode)) {
            throw new DuplicateResourceException("Supplier Code already exists in this Business Unit: " + supplierCode);
        }

        AfmSupplierInfo entity = AfmSupplierInfo.builder()
                .bgId(request.getBgId())
                .buId(request.getBuId())
                .supplierCode(supplierCode)
                .supplierName(request.getSupplierName().trim())
                .supplierType(cleanString(request.getSupplierType()))
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
                .taxRegistrationNo(cleanString(request.getTaxRegistrationNo()))
                .tinNumber(cleanString(request.getTinNumber()))
                .tradeLicenseNo(cleanString(request.getTradeLicenseNo()))
                .bankName(cleanString(request.getBankName()))
                .bankBranch(cleanString(request.getBankBranch()))
                .bankAccountName(cleanString(request.getBankAccountName()))
                .bankAccountNo(cleanString(request.getBankAccountNo()))
                .routingNumber(cleanString(request.getRoutingNumber()))
                .paymentTerms(cleanString(request.getPaymentTerms()))
                .creditLimit(request.getCreditLimit() != null ? request.getCreditLimit() : BigDecimal.ZERO)
                .creditDays(request.getCreditDays() != null ? request.getCreditDays() : 0)
                .currencyCode(request.getCurrencyCode() != null ? request.getCurrencyCode().trim() : "BDT")
                .openingBalance(request.getOpeningBalance() != null ? request.getOpeningBalance() : BigDecimal.ZERO)
                .balanceType(cleanString(request.getBalanceType()))
                .remarks(cleanString(request.getRemarks()))
                .build();

        Integer status = request.getStatus() != null ? request.getStatus() : 1;
        StatusUtil.validate(status);
        entity.setStatus(status);
        entity.setCreatedApp(AppConstants.APP_NAME);

        AfmSupplierInfo saved = supplierInfoRepository.save(entity);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public AfmSupplierInfoResponseDto updateSupplier(Long id, AfmSupplierInfoRequestDto request) {
        AfmSupplierInfo existing = findSupplier(id);

        Long bgId = request.getBgId() != null ? request.getBgId() : existing.getBgId();
        Long buId = request.getBuId() != null ? request.getBuId() : existing.getBuId();
        validateParents(bgId, buId);
        validateRequired(request);

        String supplierCode = request.getSupplierCode();
        if (supplierCode != null && !supplierCode.trim().isEmpty()) {
            supplierCode = supplierCode.trim();
            if (!supplierCode.equalsIgnoreCase(existing.getSupplierCode()) &&
                supplierInfoRepository.existsByBuIdAndSupplierCodeIgnoreCase(buId, supplierCode)) {
                throw new DuplicateResourceException("Supplier Code already exists in this Business Unit: " + supplierCode);
            }
            existing.setSupplierCode(supplierCode);
        }

        existing.setBgId(bgId);
        existing.setBuId(buId);
        existing.setSupplierName(request.getSupplierName().trim());
        existing.setSupplierType(cleanString(request.getSupplierType()));
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
        existing.setTaxRegistrationNo(cleanString(request.getTaxRegistrationNo()));
        existing.setTinNumber(cleanString(request.getTinNumber()));
        existing.setTradeLicenseNo(cleanString(request.getTradeLicenseNo()));
        existing.setBankName(cleanString(request.getBankName()));
        existing.setBankBranch(cleanString(request.getBankBranch()));
        existing.setBankAccountName(cleanString(request.getBankAccountName()));
        existing.setBankAccountNo(cleanString(request.getBankAccountNo()));
        existing.setRoutingNumber(cleanString(request.getRoutingNumber()));
        existing.setPaymentTerms(cleanString(request.getPaymentTerms()));
        existing.setCreditLimit(request.getCreditLimit() != null ? request.getCreditLimit() : BigDecimal.ZERO);
        existing.setCreditDays(request.getCreditDays() != null ? request.getCreditDays() : 0);
        existing.setCurrencyCode(request.getCurrencyCode() != null ? request.getCurrencyCode().trim() : "BDT");
        existing.setOpeningBalance(request.getOpeningBalance() != null ? request.getOpeningBalance() : BigDecimal.ZERO);
        existing.setBalanceType(cleanString(request.getBalanceType()));
        existing.setRemarks(cleanString(request.getRemarks()));

        if (request.getStatus() != null) {
            StatusUtil.validate(request.getStatus());
            existing.setStatus(request.getStatus());
        }
        existing.setUpdatedApp(AppConstants.APP_NAME);

        AfmSupplierInfo saved = supplierInfoRepository.save(existing);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteSupplier(Long id) {
        AfmSupplierInfo existing = findSupplier(id);
        supplierInfoRepository.delete(existing);
    }

    private AfmSupplierInfo findSupplier(Long id) {
        return supplierInfoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
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

    private void validateRequired(AfmSupplierInfoRequestDto request) {
        if (request.getSupplierName() == null || request.getSupplierName().trim().isEmpty()) {
            throw new BadRequestException("Supplier Name is required");
        }
        if (request.getSupplierName().trim().length() > 150) {
            throw new BadRequestException("Supplier Name cannot exceed 150 characters");
        }
    }

    private String cleanString(String val) {
        return (val != null && !val.trim().isEmpty()) ? val.trim() : null;
    }

    private AfmSupplierInfoResponseDto toResponse(AfmSupplierInfo entity) {
        String groupName = businessGroupRepository.findById(entity.getBgId())
                .map(BusinessGroup::getGroupName)
                .orElse(null);

        String businessUnitName = businessUnitRepository.findById(entity.getBuId())
                .map(BusinessUnit::getBusinessUnit)
                .orElse(null);

        String createdUserName = entity.getCreatedUserId() != null
                ? userRepository.findById(entity.getCreatedUserId()).map(User::getUserName).orElse(null)
                : null;

        String createdEmpName = null;
        if (entity.getCreatedEmpId() != null) {
            Optional<Employee> empOpt = employeeRepository.findById(entity.getCreatedEmpId());
            if (empOpt.isPresent()) {
                Employee emp = empOpt.get();
                createdEmpName = (emp.getEmployeeCode() != null ? emp.getEmployeeCode() : "") + ": " + (emp.getEmployeeName() != null ? emp.getEmployeeName() : "");
            }
        }

        String updatedUserName = entity.getUpdatedUserId() != null
                ? userRepository.findById(entity.getUpdatedUserId()).map(User::getUserName).orElse(null)
                : null;

        String updatedEmpName = null;
        if (entity.getUpdatedEmpId() != null) {
            Optional<Employee> empOpt = employeeRepository.findById(entity.getUpdatedEmpId());
            if (empOpt.isPresent()) {
                Employee emp = empOpt.get();
                updatedEmpName = (emp.getEmployeeCode() != null ? emp.getEmployeeCode() : "") + ": " + (emp.getEmployeeName() != null ? emp.getEmployeeName() : "");
            }
        }

        return AfmSupplierInfoResponseDto.builder()
                .id(entity.getId())
                .bgId(entity.getBgId())
                .groupName(groupName)
                .buId(entity.getBuId())
                .businessUnitName(businessUnitName)
                .supplierCode(entity.getSupplierCode())
                .supplierName(entity.getSupplierName())
                .supplierType(entity.getSupplierType())
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
                .taxRegistrationNo(entity.getTaxRegistrationNo())
                .tinNumber(entity.getTinNumber())
                .tradeLicenseNo(entity.getTradeLicenseNo())
                .bankName(entity.getBankName())
                .bankBranch(entity.getBankBranch())
                .bankAccountName(entity.getBankAccountName())
                .bankAccountNo(entity.getBankAccountNo())
                .routingNumber(entity.getRoutingNumber())
                .paymentTerms(entity.getPaymentTerms())
                .creditLimit(entity.getCreditLimit())
                .creditDays(entity.getCreditDays())
                .currencyCode(entity.getCurrencyCode())
                .openingBalance(entity.getOpeningBalance())
                .balanceType(entity.getBalanceType())
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
}

