package com.tse.erp.module.accounting.service.impl;

import com.tse.erp.common.AppConstants;
import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.accounting.dto.AfmCustFinAgreementRequestDto;
import com.tse.erp.module.accounting.dto.AfmCustFinAgreementResponseDto;
import com.tse.erp.module.accounting.dto.AfmCustomerInfoRequestDto;
import com.tse.erp.module.accounting.dto.AfmCustomerInfoResponseDto;
import com.tse.erp.module.accounting.entity.AfmCustFinAgreement;
import com.tse.erp.module.accounting.entity.AfmCustomerInfo;
import com.tse.erp.module.accounting.repository.AfmCustFinAgreementRepository;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AfmCustomerInfoServiceImpl implements AfmCustomerInfoService {

    private final AfmCustomerInfoRepository customerInfoRepository;
    private final AfmCustFinAgreementRepository custFinAgreementRepository;
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

        if ("Approved".equalsIgnoreCase(entity.getFinanceStatus())) {
            validateFinancialBlockMandatory(
                    entity.getPaymentTerms(),
                    entity.getCurrencyCode(),
                    entity.getCreditLowerLimit(),
                    entity.getCreditUpperLimit(),
                    entity.getCreditDays(),
                    entity.getOpeningBalance(),
                    entity.getBalanceType(),
                    entity.getAgreementDateFrom(),
                    entity.getAgreementDateTo()
            );
        }

        AfmCustomerInfo saved = customerInfoRepository.save(entity);
        if ("Approved".equalsIgnoreCase(saved.getFinanceStatus())) {
            syncFinancialAgreement(saved);
        }
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
        boolean wasApproved = "Approved".equalsIgnoreCase(existing.getFinanceStatus());
        boolean targetApproved = request.getFinanceStatus() != null && "Approved".equalsIgnoreCase(request.getFinanceStatus().trim());

        if (!wasApproved && targetApproved) {
            validateFinancialBlockMandatory(
                    request.getPaymentTerms(),
                    request.getCurrencyCode(),
                    request.getCreditLowerLimit(),
                    request.getCreditUpperLimit(),
                    request.getCreditDays(),
                    request.getOpeningBalance(),
                    request.getBalanceType(),
                    request.getAgreementDateFrom(),
                    request.getAgreementDateTo()
            );
        }

        // Only allow updating financial block if not already approved; approved customer financial info is readonly
        if (!wasApproved) {
            existing.setPaymentTerms(cleanString(request.getPaymentTerms()));
            existing.setCreditLowerLimit(request.getCreditLowerLimit());
            existing.setCreditUpperLimit(request.getCreditUpperLimit());
            existing.setCreditDays(request.getCreditDays() != null ? request.getCreditDays() : 0);
            existing.setCurrencyCode(request.getCurrencyCode() != null ? request.getCurrencyCode().trim() : "BDT");
            existing.setOpeningBalance(request.getOpeningBalance() != null ? request.getOpeningBalance() : BigDecimal.ZERO);
            existing.setBalanceType(cleanString(request.getBalanceType()));
            existing.setAgreementDateFrom(request.getAgreementDateFrom());
            existing.setAgreementDateTo(request.getAgreementDateTo());
            if (request.getFinanceStatus() != null && !request.getFinanceStatus().trim().isEmpty()) {
                existing.setFinanceStatus(request.getFinanceStatus().trim());
            }
        }
        existing.setCustomerCategoryId(request.getCustomerCategoryId());
        existing.setRemarks(cleanString(request.getRemarks()));

        if (request.getStatus() != null) {
            StatusUtil.validate(request.getStatus());
            existing.setStatus(request.getStatus());
        }
        existing.setUpdatedApp(AppConstants.APP_NAME);

        AfmCustomerInfo saved = customerInfoRepository.save(existing);
        if ("Approved".equalsIgnoreCase(saved.getFinanceStatus())) {
            syncFinancialAgreement(saved);
        } else {
            inactivateAgreements(saved.getId());
        }
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteCustomer(Long id) {
        AfmCustomerInfo existing = findCustomer(id);
        List<AfmCustFinAgreement> agreements = custFinAgreementRepository.findByCustomerIdOrderByIdDesc(id);
        if (!agreements.isEmpty()) {
            custFinAgreementRepository.deleteAll(agreements);
        }
        customerInfoRepository.delete(existing);
    }

    @Override
    @Transactional
    public AfmCustomerInfoResponseDto updateFinanceStatus(Long id, String financeStatus) {
        AfmCustomerInfo existing = findCustomer(id);
        String targetStatus = (financeStatus != null && !financeStatus.trim().isEmpty())
                ? financeStatus.trim()
                : ("Approved".equalsIgnoreCase(existing.getFinanceStatus()) ? "Draft" : "Approved");

        if ("Approved".equalsIgnoreCase(targetStatus)) {
            validateFinancialBlockMandatory(
                    existing.getPaymentTerms(),
                    existing.getCurrencyCode(),
                    existing.getCreditLowerLimit(),
                    existing.getCreditUpperLimit(),
                    existing.getCreditDays(),
                    existing.getOpeningBalance(),
                    existing.getBalanceType(),
                    existing.getAgreementDateFrom(),
                    existing.getAgreementDateTo()
            );
        }

        existing.setFinanceStatus(targetStatus);
        existing.setUpdatedApp(AppConstants.APP_NAME);
        AfmCustomerInfo saved = customerInfoRepository.save(existing);
        if ("Approved".equalsIgnoreCase(saved.getFinanceStatus())) {
            syncFinancialAgreement(saved);
        } else {
            inactivateAgreements(saved.getId());
        }
        return toResponse(saved);
    }

    @Override
    @Transactional
    public AfmCustomerInfoResponseDto createNewAgreement(Long customerId, AfmCustFinAgreementRequestDto request) {
        if (request == null) {
            throw new BadRequestException("Agreement data cannot be empty");
        }
        AfmCustomerInfo customer = findCustomer(customerId);

        validateFinancialBlockMandatory(
                request.getPaymentTerms(),
                request.getCurrencyCode(),
                request.getCreditLowerLimit(),
                request.getCreditUpperLimit(),
                request.getCreditDays(),
                request.getOpeningBalance(),
                request.getBalanceType(),
                request.getAgreementDateFrom(),
                request.getAgreementDateTo()
        );

        // Update customer's 5. Payment & Financial Information block data with latest submitted data
        customer.setPaymentTerms(cleanString(request.getPaymentTerms()));
        customer.setCurrencyCode(request.getCurrencyCode() != null ? request.getCurrencyCode().trim() : "BDT");
        customer.setCreditLowerLimit(request.getCreditLowerLimit());
        customer.setCreditUpperLimit(request.getCreditUpperLimit());
        customer.setCreditDays(request.getCreditDays());
        customer.setOpeningBalance(request.getOpeningBalance());
        customer.setBalanceType(cleanString(request.getBalanceType()));
        customer.setAgreementDateFrom(request.getAgreementDateFrom());
        customer.setAgreementDateTo(request.getAgreementDateTo());
        customer.setFinanceStatus("Approved");
        customer.setUpdatedApp(AppConstants.APP_NAME);
        AfmCustomerInfo saved = customerInfoRepository.save(customer);

        // Inactivate all previous approved agreements of that customer (STATUS = 0)
        inactivateAgreements(customerId);

        // Insert new agreement record into AFM_CUST_FIN_AGREEMENT with STATUS = 1 and FINANCE_STATUS = "Approved"
        AfmCustFinAgreement newAgreement = AfmCustFinAgreement.builder()
                .bgId(saved.getBgId())
                .buId(saved.getBuId())
                .customerId(saved.getId())
                .paymentTerms(saved.getPaymentTerms())
                .currencyCode(saved.getCurrencyCode())
                .creditLowerLimit(saved.getCreditLowerLimit())
                .creditUpperLimit(saved.getCreditUpperLimit())
                .creditDays(saved.getCreditDays())
                .openingBalance(saved.getOpeningBalance())
                .balanceType(saved.getBalanceType())
                .agreementDateFrom(saved.getAgreementDateFrom())
                .agreementDateTo(saved.getAgreementDateTo())
                .financeStatus("Approved")
                .build();
        newAgreement.setStatus(1);
        newAgreement.setCreatedApp(AppConstants.APP_NAME);
        custFinAgreementRepository.save(newAgreement);

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

    private void validateFinancialBlockMandatory(
            String paymentTerms,
            String currencyCode,
            BigDecimal creditLowerLimit,
            BigDecimal creditUpperLimit,
            Integer creditDays,
            BigDecimal openingBalance,
            String balanceType,
            LocalDate agreementDateFrom,
            LocalDate agreementDateTo
    ) {
        if (paymentTerms == null || paymentTerms.trim().isEmpty()) {
            throw new BadRequestException("Payment Terms is required when Finance Status is Approved");
        }
        if (currencyCode == null || currencyCode.trim().isEmpty()) {
            throw new BadRequestException("Currency Code is required when Finance Status is Approved");
        }
        if (creditLowerLimit == null) {
            throw new BadRequestException("Credit Lower Limit is required when Finance Status is Approved");
        }
        if (creditUpperLimit == null) {
            throw new BadRequestException("Credit Upper Limit is required when Finance Status is Approved");
        }
        if (creditUpperLimit.compareTo(creditLowerLimit) < 0) {
            throw new BadRequestException("Credit Upper Limit must be greater than or equal to Credit Lower Limit");
        }
        if (creditDays == null) {
            throw new BadRequestException("Credit Days is required when Finance Status is Approved");
        }
        if (openingBalance == null) {
            throw new BadRequestException("Opening Balance is required when Finance Status is Approved");
        }
        if (balanceType == null || balanceType.trim().isEmpty()) {
            throw new BadRequestException("Balance Type is required when Finance Status is Approved");
        }
        if (agreementDateFrom == null) {
            throw new BadRequestException("Agreement Date (From) is required when Finance Status is Approved");
        }
        if (agreementDateTo == null) {
            throw new BadRequestException("Agreement Date (To) is required when Finance Status is Approved");
        }
        if (agreementDateTo.isBefore(agreementDateFrom)) {
            throw new BadRequestException("Agreement Date (To) must be on or after Agreement Date (From)");
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

        Long activeAgreementId = entity.getId() != null
                ? custFinAgreementRepository.findFirstByCustomerIdAndStatusOrderByIdDesc(entity.getId(), 1)
                        .map(AfmCustFinAgreement::getId)
                        .orElse(null)
                : null;

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
                .activeAgreementId(activeAgreementId)
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

    @Override
    public List<AfmCustFinAgreementResponseDto> getFinancialAgreementsByCustomerId(Long customerId) {
        return custFinAgreementRepository.findByCustomerIdOrderByIdDesc(customerId)
                .stream()
                .map(this::toAgreementResponse)
                .toList();
    }

    private void syncFinancialAgreement(AfmCustomerInfo customer) {
        if (customer == null || customer.getId() == null) {
            return;
        }
        if (!"Approved".equalsIgnoreCase(customer.getFinanceStatus())) {
            return;
        }

        Optional<AfmCustFinAgreement> currentActiveOpt = custFinAgreementRepository
                .findFirstByCustomerIdAndStatusOrderByIdDesc(customer.getId(), 1);

        if (currentActiveOpt.isPresent()) {
            AfmCustFinAgreement currentActive = currentActiveOpt.get();
            if (!isFinancialInfoChanged(currentActive, customer)) {
                return;
            }
        }

        inactivateAgreements(customer.getId());

        AfmCustFinAgreement newAgreement = AfmCustFinAgreement.builder()
                .bgId(customer.getBgId())
                .buId(customer.getBuId())
                .customerId(customer.getId())
                .paymentTerms(customer.getPaymentTerms())
                .currencyCode(customer.getCurrencyCode())
                .creditLowerLimit(customer.getCreditLowerLimit())
                .creditUpperLimit(customer.getCreditUpperLimit())
                .creditDays(customer.getCreditDays())
                .openingBalance(customer.getOpeningBalance())
                .balanceType(customer.getBalanceType())
                .agreementDateFrom(customer.getAgreementDateFrom())
                .agreementDateTo(customer.getAgreementDateTo())
                .financeStatus("Approved")
                .build();

        newAgreement.setStatus(1);
        newAgreement.setCreatedApp(AppConstants.APP_NAME);
        custFinAgreementRepository.save(newAgreement);
    }

    private void inactivateAgreements(Long customerId) {
        List<AfmCustFinAgreement> list = custFinAgreementRepository.findByCustomerIdOrderByIdDesc(customerId);
        for (AfmCustFinAgreement a : list) {
            if (a.getStatus() == null || a.getStatus() != 0) {
                a.setStatus(0);
                a.setUpdatedApp(AppConstants.APP_NAME);
                custFinAgreementRepository.save(a);
            }
        }
    }

    private boolean isFinancialInfoChanged(AfmCustFinAgreement current, AfmCustomerInfo updated) {
        if (current == null) return true;
        return !Objects.equals(current.getPaymentTerms(), updated.getPaymentTerms())
                || !Objects.equals(current.getCurrencyCode(), updated.getCurrencyCode())
                || !isEqualBigDecimal(current.getCreditLowerLimit(), updated.getCreditLowerLimit())
                || !isEqualBigDecimal(current.getCreditUpperLimit(), updated.getCreditUpperLimit())
                || !Objects.equals(current.getCreditDays(), updated.getCreditDays())
                || !isEqualBigDecimal(current.getOpeningBalance(), updated.getOpeningBalance())
                || !Objects.equals(current.getBalanceType(), updated.getBalanceType())
                || !Objects.equals(current.getAgreementDateFrom(), updated.getAgreementDateFrom())
                || !Objects.equals(current.getAgreementDateTo(), updated.getAgreementDateTo());
    }

    private boolean isEqualBigDecimal(BigDecimal a, BigDecimal b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.compareTo(b) == 0;
    }

    private AfmCustFinAgreementResponseDto toAgreementResponse(AfmCustFinAgreement entity) {
        String createdUserName = null;
        if (entity.getCreatedUserId() != null) {
            createdUserName = userRepository.findById(entity.getCreatedUserId())
                    .map(User::getUserName)
                    .orElse(null);
        }

        return AfmCustFinAgreementResponseDto.builder()
                .id(entity.getId())
                .bgId(entity.getBgId())
                .buId(entity.getBuId())
                .customerId(entity.getCustomerId())
                .paymentTerms(entity.getPaymentTerms())
                .currencyCode(entity.getCurrencyCode())
                .creditLowerLimit(entity.getCreditLowerLimit())
                .creditUpperLimit(entity.getCreditUpperLimit())
                .creditDays(entity.getCreditDays())
                .openingBalance(entity.getOpeningBalance())
                .balanceType(entity.getBalanceType())
                .agreementDateFrom(entity.getAgreementDateFrom())
                .agreementDateTo(entity.getAgreementDateTo())
                .financeStatus(entity.getFinanceStatus())
                .createdUserId(entity.getCreatedUserId())
                .createdUserName(createdUserName)
                .createdEmpId(entity.getCreatedEmpId())
                .createdDatetime(entity.getCreatedDatetime())
                .updatedUserId(entity.getUpdatedUserId())
                .updatedEmpId(entity.getUpdatedEmpId())
                .updatedDatetime(entity.getUpdatedDatetime())
                .createdApp(entity.getCreatedApp())
                .updatedApp(entity.getUpdatedApp())
                .status(entity.getStatus())
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

