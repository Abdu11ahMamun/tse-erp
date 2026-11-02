package com.tse.erp.module.accounting.service.impl;

import com.tse.erp.common.AppConstants;
import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.accounting.dto.AfmBankDtlsDto;
import com.tse.erp.module.accounting.dto.AfmBankRequestDto;
import com.tse.erp.module.accounting.dto.AfmBankResponseDto;
import com.tse.erp.module.accounting.entity.AfmBank;
import com.tse.erp.module.accounting.entity.AfmBankDtls;
import com.tse.erp.module.accounting.repository.AfmBankDtlsRepository;
import com.tse.erp.module.accounting.repository.AfmBankRepository;
import com.tse.erp.module.accounting.service.AfmBankService;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AfmBankServiceImpl implements AfmBankService {

    private final AfmBankRepository bankRepository;
    private final AfmBankDtlsRepository bankDtlsRepository;
    private final BusinessGroupRepository businessGroupRepository;
    private final BusinessUnitRepository businessUnitRepository;

    @Override
    public List<AfmBankResponseDto> getAllBanks(Long bgId, Long buId, Integer status) {
        List<AfmBank> banks;

        if (bgId != null && buId != null && status != null) {
            banks = bankRepository.findByBgIdAndBuIdAndStatusOrderByIdDesc(bgId, buId, status);
        } else if (bgId != null && buId != null) {
            banks = bankRepository.findByBgIdAndBuIdOrderByIdDesc(bgId, buId);
        } else if (buId != null && status != null) {
            banks = bankRepository.findByBuIdAndStatusOrderByIdDesc(buId, status);
        } else if (buId != null) {
            banks = bankRepository.findByBuIdOrderByIdDesc(buId);
        } else if (bgId != null) {
            banks = bankRepository.findByBgIdOrderByIdDesc(bgId);
        } else {
            banks = bankRepository.findAllByOrderByIdDesc();
        }

        if (status != null && (bgId != null && buId == null || bgId == null && buId == null)) {
            banks = banks.stream().filter(b -> status.equals(b.getStatus())).toList();
        }

        return banks.stream().map(this::toResponse).toList();
    }

    @Override
    public AfmBankResponseDto getBankById(Long id) {
        return toResponse(findBank(id));
    }

    @Override
    public List<AfmBankDtlsDto> getBankDetails(Long bankId) {
        findBank(bankId); // ensure bank exists
        return bankDtlsRepository.findByAfmBankIdOrderByIdAsc(bankId)
                .stream()
                .map(this::toDetailDto)
                .toList();
    }

    @Override
    @Transactional
    public AfmBankResponseDto createBank(AfmBankRequestDto request) {
        validateParents(request.getBgId(), request.getBuId());
        validateBankName(request.getBankName());
        validateShortName(request.getShortName());
        StatusUtil.validate(request.getStatus());

        String bankName = request.getBankName().trim();
        String shortName = request.getShortName() != null ? request.getShortName().trim() : null;

        boolean exists = !bankRepository.findByBuIdAndBankNameIgnoreCase(request.getBuId(), bankName).isEmpty();
        if (exists) {
            throw new DuplicateResourceException("Bank name already exists for this Business Unit: " + bankName);
        }

        validateDetailRows(request.getBankDetails(), request.getBuId(), null);

        AfmBank bank = AfmBank.builder()
                .bgId(request.getBgId())
                .buId(request.getBuId())
                .bankName(bankName)
                .shortName(shortName)
                .build();

        bank.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        bank.setCreatedApp(AppConstants.APP_NAME);

        AfmBank savedBank = bankRepository.save(bank);

        if (request.getBankDetails() != null && !request.getBankDetails().isEmpty()) {
            List<AfmBankDtls> details = new ArrayList<>();
            for (AfmBankDtlsDto dto : request.getBankDetails()) {
                AfmBankDtls detail = toDetailEntity(dto, savedBank.getId(), savedBank.getBgId(), savedBank.getBuId());
                detail.setCreatedApp(AppConstants.APP_NAME);
                details.add(detail);
            }
            bankDtlsRepository.saveAll(details);
        }

        return toResponse(savedBank);
    }

    @Override
    @Transactional
    public AfmBankResponseDto updateBank(Long id, AfmBankRequestDto request) {
        AfmBank existing = findBank(id);

        validateParents(request.getBgId(), request.getBuId());
        validateBankName(request.getBankName());
        validateShortName(request.getShortName());
        StatusUtil.validate(request.getStatus());

        String bankName = request.getBankName().trim();
        String shortName = request.getShortName() != null ? request.getShortName().trim() : null;

        boolean duplicateExists = bankRepository.findByBuIdAndBankNameIgnoreCase(request.getBuId(), bankName)
                .stream()
                .anyMatch(b -> !b.getId().equals(id));
        if (duplicateExists) {
            throw new DuplicateResourceException("Bank name already exists for this Business Unit: " + bankName);
        }

        validateDetailRows(request.getBankDetails(), request.getBuId(), id);

        existing.setBgId(request.getBgId());
        existing.setBuId(request.getBuId());
        existing.setBankName(bankName);
        existing.setShortName(shortName);
        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }
        existing.setUpdatedApp(AppConstants.APP_NAME);

        AfmBank savedBank = bankRepository.save(existing);

        // Synchronize Detail Rows
        List<AfmBankDtls> currentDetails = bankDtlsRepository.findByAfmBankIdOrderByIdAsc(id);
        Map<Long, AfmBankDtls> existingMap = new HashMap<>();
        currentDetails.forEach(d -> existingMap.put(d.getId(), d));

        Set<Long> processedIds = new HashSet<>();
        List<AfmBankDtls> toSave = new ArrayList<>();
        List<AfmBankDtlsDto> incomingRows = request.getBankDetails() != null ? request.getBankDetails() : Collections.emptyList();

        for (AfmBankDtlsDto dto : incomingRows) {
            if (dto.getId() != null) {
                AfmBankDtls current = existingMap.get(dto.getId());
                if (current == null) {
                    throw new BadRequestException("Detail row with id " + dto.getId() + " does not belong to Bank " + id);
                }
                current.setBgId(savedBank.getBgId());
                current.setBuId(savedBank.getBuId());
                current.setBranchName(dto.getBranchName() != null ? dto.getBranchName().trim() : null);
                current.setAddress(dto.getAddress() != null ? dto.getAddress().trim() : null);
                current.setAccountType(dto.getAccountType() != null ? dto.getAccountType().trim() : null);
                current.setAccountName(dto.getAccountName() != null ? dto.getAccountName().trim() : null);
                current.setAccountNumber(dto.getAccountNumber() != null ? dto.getAccountNumber().trim() : null);
                current.setOpeningDate(dto.getOpeningDate());
                current.setOpeningBalance(dto.getOpeningBalance());
                if (dto.getStatus() != null) {
                    current.setStatus(dto.getStatus());
                }
                current.setUpdatedApp(AppConstants.APP_NAME);
                toSave.add(current);
                processedIds.add(dto.getId());
            } else {
                AfmBankDtls newDetail = toDetailEntity(dto, id, savedBank.getBgId(), savedBank.getBuId());
                newDetail.setCreatedApp(AppConstants.APP_NAME);
                toSave.add(newDetail);
            }
        }

        // Delete detail rows that were removed from the entry form
        List<AfmBankDtls> toDelete = currentDetails.stream()
                .filter(d -> !processedIds.contains(d.getId()))
                .toList();

        if (!toDelete.isEmpty()) {
            bankDtlsRepository.deleteAll(toDelete);
        }

        if (!toSave.isEmpty()) {
            bankDtlsRepository.saveAll(toSave);
        }

        return toResponse(savedBank);
    }

    @Override
    @Transactional
    public void deleteBank(Long id) {
        AfmBank existing = findBank(id);
        List<AfmBankDtls> details = bankDtlsRepository.findByAfmBankIdOrderByIdAsc(id);
        if (!details.isEmpty()) {
            bankDtlsRepository.deleteAll(details);
        }
        bankRepository.delete(existing);
    }

    @Override
    @Transactional
    public AfmBankDtlsDto addBankDetail(Long bankId, AfmBankDtlsDto detailDto) {
        AfmBank bank = findBank(bankId);
        validateDetailRow(detailDto, bank.getBuId(), null);

        AfmBankDtls detail = toDetailEntity(detailDto, bankId, bank.getBgId(), bank.getBuId());
        detail.setCreatedApp(AppConstants.APP_NAME);
        AfmBankDtls saved = bankDtlsRepository.save(detail);

        return toDetailDto(saved);
    }

    @Override
    @Transactional
    public AfmBankDtlsDto updateBankDetail(Long bankId, Long detailId, AfmBankDtlsDto detailDto) {
        AfmBank bank = findBank(bankId);
        AfmBankDtls existing = bankDtlsRepository.findById(detailId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank detail not found with id: " + detailId));

        if (!bankId.equals(existing.getAfmBankId())) {
            throw new BadRequestException("Detail row with id " + detailId + " does not belong to Bank " + bankId);
        }

        validateDetailRow(detailDto, bank.getBuId(), detailId);

        existing.setBgId(bank.getBgId());
        existing.setBuId(bank.getBuId());
        existing.setBranchName(detailDto.getBranchName() != null ? detailDto.getBranchName().trim() : null);
        existing.setAddress(detailDto.getAddress() != null ? detailDto.getAddress().trim() : null);
        existing.setAccountType(detailDto.getAccountType() != null ? detailDto.getAccountType().trim() : null);
        existing.setAccountName(detailDto.getAccountName() != null ? detailDto.getAccountName().trim() : null);
        existing.setAccountNumber(detailDto.getAccountNumber() != null ? detailDto.getAccountNumber().trim() : null);
        existing.setOpeningDate(detailDto.getOpeningDate());
        existing.setOpeningBalance(detailDto.getOpeningBalance());
        if (detailDto.getStatus() != null) {
            existing.setStatus(detailDto.getStatus());
        }
        existing.setUpdatedApp(AppConstants.APP_NAME);

        AfmBankDtls saved = bankDtlsRepository.save(existing);
        return toDetailDto(saved);
    }

    @Override
    @Transactional
    public void deleteBankDetail(Long bankId, Long detailId) {
        findBank(bankId);
        AfmBankDtls existing = bankDtlsRepository.findById(detailId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank detail not found with id: " + detailId));

        if (!bankId.equals(existing.getAfmBankId())) {
            throw new BadRequestException("Detail row with id " + detailId + " does not belong to Bank " + bankId);
        }

        bankDtlsRepository.delete(existing);
    }

    // =========================================
    // HELPER & VALIDATION METHODS
    // =========================================

    private AfmBank findBank(Long id) {
        return bankRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bank not found with id: " + id));
    }

    private AfmBankResponseDto toResponse(AfmBank bank) {
        List<AfmBankDtlsDto> details = bankDtlsRepository
                .findByAfmBankIdOrderByIdAsc(bank.getId())
                .stream()
                .map(this::toDetailDto)
                .toList();

        return AfmBankResponseDto.builder()
                .id(bank.getId())
                .bgId(bank.getBgId())
                .buId(bank.getBuId())
                .bankName(bank.getBankName())
                .shortName(bank.getShortName())
                .status(bank.getStatus())
                .createdUserId(bank.getCreatedUserId())
                .createdEmpId(bank.getCreatedEmpId())
                .createdDatetime(bank.getCreatedDatetime())
                .updatedUserId(bank.getUpdatedUserId())
                .updatedEmpId(bank.getUpdatedEmpId())
                .updatedDatetime(bank.getUpdatedDatetime())
                .createdApp(bank.getCreatedApp())
                .updatedApp(bank.getUpdatedApp())
                .bankDetails(details)
                .build();
    }

    private AfmBankDtlsDto toDetailDto(AfmBankDtls detail) {
        return AfmBankDtlsDto.builder()
                .id(detail.getId())
                .bgId(detail.getBgId())
                .buId(detail.getBuId())
                .afmBankId(detail.getAfmBankId())
                .branchName(detail.getBranchName())
                .address(detail.getAddress())
                .accountType(detail.getAccountType())
                .accountName(detail.getAccountName())
                .accountNumber(detail.getAccountNumber())
                .openingDate(detail.getOpeningDate())
                .openingBalance(detail.getOpeningBalance())
                .status(detail.getStatus())
                .build();
    }

    private AfmBankDtls toDetailEntity(AfmBankDtlsDto dto, Long bankId, Long bgId, Long buId) {
        return AfmBankDtls.builder()
                .id(dto.getId())
                .bgId(bgId)
                .buId(buId)
                .afmBankId(bankId)
                .branchName(dto.getBranchName() != null ? dto.getBranchName().trim() : null)
                .address(dto.getAddress() != null ? dto.getAddress().trim() : null)
                .accountType(dto.getAccountType() != null ? dto.getAccountType().trim() : null)
                .accountName(dto.getAccountName() != null ? dto.getAccountName().trim() : null)
                .accountNumber(dto.getAccountNumber() != null ? dto.getAccountNumber().trim() : null)
                .openingDate(dto.getOpeningDate())
                .openingBalance(dto.getOpeningBalance() != null ? dto.getOpeningBalance() : BigDecimal.ZERO)
                .build();
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

    private void validateBankName(String bankName) {
        if (bankName == null || bankName.trim().isEmpty()) {
            throw new BadRequestException("Bank name cannot be empty");
        }
        if (bankName.trim().length() < 2) {
            throw new BadRequestException("Bank name must be at least 2 characters");
        }
        if (bankName.trim().length() > 150) {
            throw new BadRequestException("Bank name cannot exceed 150 characters");
        }
    }

    private void validateShortName(String shortName) {
        if (shortName != null && shortName.trim().length() > 50) {
            throw new BadRequestException("Short name cannot exceed 50 characters");
        }
    }

    private void validateDetailRows(List<AfmBankDtlsDto> details, Long buId, Long existingBankId) {
        if (details == null || details.isEmpty()) {
            return;
        }

        Set<String> seenAccountNumbers = new HashSet<>();

        for (AfmBankDtlsDto row : details) {
            validateDetailRow(row, buId, row.getId());

            if (row.getAccountNumber() != null && !row.getAccountNumber().trim().isEmpty()) {
                String accNo = row.getAccountNumber().trim().toUpperCase(Locale.ROOT);
                if (!seenAccountNumbers.add(accNo)) {
                    throw new DuplicateResourceException("Duplicate account number in form: " + row.getAccountNumber());
                }

                // Check duplicate account number against existing accounts in the same BU
                List<AfmBankDtls> existingWithAccNo = bankDtlsRepository.findByBuIdAndAccountNumber(buId, row.getAccountNumber().trim());
                for (AfmBankDtls existing : existingWithAccNo) {
                    if (existingBankId == null || !existing.getAfmBankId().equals(existingBankId) || (row.getId() != null && !existing.getId().equals(row.getId()))) {
                        throw new DuplicateResourceException("Account number already exists in this Business Unit: " + row.getAccountNumber());
                    }
                }
            }
        }
    }

    private void validateDetailRow(AfmBankDtlsDto row, Long buId, Long existingDetailId) {
        if (row.getBranchName() != null && row.getBranchName().trim().length() > 150) {
            throw new BadRequestException("Branch name cannot exceed 150 characters");
        }
        if (row.getAddress() != null && row.getAddress().trim().length() > 500) {
            throw new BadRequestException("Address cannot exceed 500 characters");
        }
        if (row.getAccountType() != null && row.getAccountType().trim().length() > 50) {
            throw new BadRequestException("Account type cannot exceed 50 characters");
        }
        if (row.getAccountName() != null && row.getAccountName().trim().length() > 150) {
            throw new BadRequestException("Account name cannot exceed 150 characters");
        }
        if (row.getAccountNumber() != null && row.getAccountNumber().trim().length() > 50) {
            throw new BadRequestException("Account number cannot exceed 50 characters");
        }
        if (row.getOpeningBalance() != null && row.getOpeningBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Opening balance cannot be negative");
        }
        StatusUtil.validate(row.getStatus());
    }
}
