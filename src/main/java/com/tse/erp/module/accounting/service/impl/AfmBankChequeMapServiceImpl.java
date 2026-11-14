package com.tse.erp.module.accounting.service.impl;

import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.accounting.dto.AfmBankChequeMapRequestDto;
import com.tse.erp.module.accounting.dto.AfmBankChequeMapResponseDto;
import com.tse.erp.module.accounting.dto.AfmBankChequeMapUpdateDto;
import com.tse.erp.module.accounting.entity.AfmBank;
import com.tse.erp.module.accounting.entity.AfmBankDtls;
import com.tse.erp.module.accounting.entity.AfmBankChequeMap;
import com.tse.erp.module.accounting.repository.AfmBankChequeMapRepository;
import com.tse.erp.module.accounting.repository.AfmBankDtlsRepository;
import com.tse.erp.module.accounting.repository.AfmBankRepository;
import com.tse.erp.module.accounting.service.AfmBankChequeMapService;
import com.tse.erp.module.admin.entity.BusinessGroup;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AfmBankChequeMapServiceImpl implements AfmBankChequeMapService {

    private static final int MAX_BATCH_SIZE = 500;

    private final AfmBankChequeMapRepository chequeMapRepository;
    private final AfmBankRepository bankRepository;
    private final AfmBankDtlsRepository bankDtlsRepository;
    private final BusinessGroupRepository businessGroupRepository;
    private final BusinessUnitRepository businessUnitRepository;

    @Override
    public List<AfmBankChequeMapResponseDto> getAllChequeMaps(
            Long bgId,
            Long buId,
            Long bankId,
            Long bankAccountId,
            String chequeStatus,
            Integer status) {

        List<AfmBankChequeMap> list;

        if (bgId != null && buId != null && status != null) {
            list = chequeMapRepository.findByBgIdAndBuIdAndStatusOrderByIdDesc(bgId, buId, status);
        } else if (bgId != null && buId != null) {
            list = chequeMapRepository.findByBgIdAndBuIdOrderByIdDesc(bgId, buId);
        } else if (buId != null && status != null) {
            list = chequeMapRepository.findByBuIdAndStatusOrderByIdDesc(buId, status);
        } else if (buId != null) {
            list = chequeMapRepository.findByBuIdOrderByIdDesc(buId);
        } else if (bgId != null) {
            list = chequeMapRepository.findByBgIdOrderByIdDesc(bgId);
        } else {
            list = chequeMapRepository.findAllByOrderByIdDesc();
        }

        if (bankId != null) {
            list = list.stream().filter(item -> bankId.equals(item.getBankId())).toList();
        }
        if (bankAccountId != null) {
            list = list.stream().filter(item -> bankAccountId.equals(item.getBankAccountId())).toList();
        }
        if (chequeStatus != null && !chequeStatus.trim().isEmpty()) {
            list = list.stream().filter(item -> chequeStatus.equalsIgnoreCase(item.getChequeStatus())).toList();
        }
        if (status != null && (bgId == null || buId == null)) {
            list = list.stream().filter(item -> status.equals(item.getStatus())).toList();
        }

        return toResponseList(list);
    }

    @Override
    public AfmBankChequeMapResponseDto getChequeMapById(Long id) {
        AfmBankChequeMap entity = findByIdOrThrow(id);
        return toResponse(entity);
    }

    @Override
    @Transactional
    public List<AfmBankChequeMapResponseDto> createChequeBatch(AfmBankChequeMapRequestDto request) {
        validateParents(request.getBgId(), request.getBuId());
        validateBankAndAccount(request.getBuId(), request.getBankId(), request.getBankAccountId());
        StatusUtil.validate(request.getStatus());

        String fromStr = request.getChequeNoFrom();
        String toStr = request.getChequeNoTo();

        if (fromStr == null || fromStr.trim().isEmpty()) {
            throw new BadRequestException("From Cheque number cannot be empty");
        }
        if (toStr == null || toStr.trim().isEmpty()) {
            throw new BadRequestException("To Cheque number cannot be empty");
        }

        long from;
        long to;
        try {
            from = Long.parseLong(fromStr.trim());
        } catch (NumberFormatException e) {
            throw new BadRequestException("From Cheque must be a valid positive integer number");
        }
        try {
            to = Long.parseLong(toStr.trim());
        } catch (NumberFormatException e) {
            throw new BadRequestException("To Cheque must be a valid positive integer number");
        }

        if (from < 0 || to < 0) {
            throw new BadRequestException("Cheque numbers cannot be negative");
        }
        if (to < from) {
            throw new BadRequestException("To Cheque (" + toStr + ") must be greater than or equal to From Cheque (" + fromStr + ")");
        }

        long count = (to - from) + 1;
        if (count > MAX_BATCH_SIZE) {
            throw new BadRequestException("Maximum allowed batch size is " + MAX_BATCH_SIZE + " cheques per range (requested " + count + ")");
        }

        int width = fromStr.trim().length();
        List<String> generatedChequeNos = new ArrayList<>((int) count);
        for (long i = from; i <= to; i++) {
            generatedChequeNos.add(String.format("%0" + width + "d", i));
        }

        List<String> existingNumbers = chequeMapRepository.findExistingChequeNumbers(
                request.getBankAccountId(),
                generatedChequeNos
        );
        if (existingNumbers != null && !existingNumbers.isEmpty()) {
            throw new DuplicateResourceException(
                    "The following cheque number(s) already exist for this bank account: " +
                            String.join(", ", existingNumbers)
            );
        }

        String chequeStatus = request.getChequeStatus();
        if (chequeStatus == null || chequeStatus.trim().isEmpty()) {
            chequeStatus = "Not Use";
        } else {
            chequeStatus = chequeStatus.trim();
        }

        Integer status = request.getStatus() != null ? request.getStatus() : 1;

        List<AfmBankChequeMap> entities = new ArrayList<>((int) count);
        for (String chequeNo : generatedChequeNos) {
            AfmBankChequeMap entity = AfmBankChequeMap.builder()
                    .bgId(request.getBgId())
                    .buId(request.getBuId())
                    .bankId(request.getBankId())
                    .bankAccountId(request.getBankAccountId())
                    .chequeNoFrom(fromStr.trim())
                    .chequeNoTo(toStr.trim())
                    .chequeNo(chequeNo)
                    .chequeStatus(chequeStatus)
                    .build();
            entity.setStatus(status);
            entities.add(entity);
        }

        List<AfmBankChequeMap> saved = chequeMapRepository.saveAll(entities);
        return toResponseList(saved);
    }

    @Override
    @Transactional
    public AfmBankChequeMapResponseDto updateChequeMap(Long id, AfmBankChequeMapUpdateDto request) {
        AfmBankChequeMap entity = findByIdOrThrow(id);

        if (request.getChequeStatus() != null && !request.getChequeStatus().trim().isEmpty()) {
            entity.setChequeStatus(request.getChequeStatus().trim());
        }
        if (request.getStatus() != null) {
            StatusUtil.validate(request.getStatus());
            entity.setStatus(request.getStatus());
        }

        AfmBankChequeMap updated = chequeMapRepository.save(entity);
        return toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteChequeMap(Long id) {
        AfmBankChequeMap entity = findByIdOrThrow(id);
        if ("Used".equalsIgnoreCase(entity.getChequeStatus())) {
            throw new BadRequestException("Cannot delete cheque that has already been marked as Used");
        }
        chequeMapRepository.delete(entity);
    }

    private AfmBankChequeMap findByIdOrThrow(Long id) {
        return chequeMapRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bank Cheque Mapping with id " + id + " not found"));
    }

    private void validateParents(Long bgId, Long buId) {
        if (bgId == null) {
            throw new BadRequestException("Group id cannot be empty");
        }
        if (buId == null) {
            throw new BadRequestException("Business Unit id cannot be empty");
        }

        BusinessGroup bg = businessGroupRepository.findById(bgId)
                .orElseThrow(() -> new ResourceNotFoundException("Business Group with id " + bgId + " not found"));
        if (!Integer.valueOf(1).equals(bg.getStatus())) {
            throw new BadRequestException("Business Group is inactive");
        }

        BusinessUnit bu = businessUnitRepository.findById(buId)
                .orElseThrow(() -> new ResourceNotFoundException("Business Unit with id " + buId + " not found"));
        if (!Integer.valueOf(1).equals(bu.getStatus())) {
            throw new BadRequestException("Business Unit is inactive");
        }
        if (!Objects.equals(bu.getBgId(), bgId)) {
            throw new BadRequestException("Business Unit " + buId + " does not belong to Business Group " + bgId);
        }
    }

    private void validateBankAndAccount(Long buId, Long bankId, Long bankAccountId) {
        if (bankId == null) {
            throw new BadRequestException("Bank id cannot be empty");
        }
        if (bankAccountId == null) {
            throw new BadRequestException("Bank Account id cannot be empty");
        }

        AfmBank bank = bankRepository.findById(bankId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank with id " + bankId + " not found"));
        if (!Objects.equals(bank.getBuId(), buId)) {
            throw new BadRequestException("Bank " + bankId + " does not belong to Business Unit " + buId);
        }

        AfmBankDtls account = bankDtlsRepository.findById(bankAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank Account with id " + bankAccountId + " not found"));
        if (!Objects.equals(account.getAfmBankId(), bankId)) {
            throw new BadRequestException("Bank Account " + bankAccountId + " does not belong to Bank " + bankId);
        }
    }

    private AfmBankChequeMapResponseDto toResponse(AfmBankChequeMap entity) {
        return toResponseList(List.of(entity)).get(0);
    }

    private List<AfmBankChequeMapResponseDto> toResponseList(List<AfmBankChequeMap> list) {
        if (list.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> bgIds = list.stream().map(AfmBankChequeMap::getBgId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> buIds = list.stream().map(AfmBankChequeMap::getBuId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> bankIds = list.stream().map(AfmBankChequeMap::getBankId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> accountIds = list.stream().map(AfmBankChequeMap::getBankAccountId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<Long, BusinessGroup> bgMap = businessGroupRepository.findAllById(bgIds).stream()
                .collect(Collectors.toMap(BusinessGroup::getId, Function.identity()));
        Map<Long, BusinessUnit> buMap = businessUnitRepository.findAllById(buIds).stream()
                .collect(Collectors.toMap(BusinessUnit::getId, Function.identity()));
        Map<Long, AfmBank> bankMap = bankRepository.findAllById(bankIds).stream()
                .collect(Collectors.toMap(AfmBank::getId, Function.identity()));
        Map<Long, AfmBankDtls> accountMap = bankDtlsRepository.findAllById(accountIds).stream()
                .collect(Collectors.toMap(AfmBankDtls::getId, Function.identity()));

        return list.stream().map(item -> {
            BusinessGroup bg = bgMap.get(item.getBgId());
            BusinessUnit bu = buMap.get(item.getBuId());
            AfmBank bank = bankMap.get(item.getBankId());
            AfmBankDtls account = accountMap.get(item.getBankAccountId());

            return AfmBankChequeMapResponseDto.builder()
                    .id(item.getId())
                    .bgId(item.getBgId())
                    .groupName(bg != null ? bg.getGroupName() : null)
                    .buId(item.getBuId())
                    .businessUnitName(bu != null ? bu.getBusinessUnit() : null)
                    .bankId(item.getBankId())
                    .bankName(bank != null ? bank.getBankName() : null)
                    .bankAccountId(item.getBankAccountId())
                    .accountNumber(account != null ? account.getAccountNumber() : null)
                    .accountName(account != null ? account.getAccountName() : null)
                    .branchName(account != null ? account.getBranchName() : null)
                    .chequeNoFrom(item.getChequeNoFrom())
                    .chequeNoTo(item.getChequeNoTo())
                    .chequeNo(item.getChequeNo())
                    .chequeStatus(item.getChequeStatus())
                    .status(item.getStatus())
                    .createdUserId(item.getCreatedUserId())
                    .createdEmpId(item.getCreatedEmpId())
                    .createdDatetime(item.getCreatedDatetime())
                    .updatedUserId(item.getUpdatedUserId())
                    .updatedEmpId(item.getUpdatedEmpId())
                    .updatedDatetime(item.getUpdatedDatetime())
                    .createdApp(item.getCreatedApp())
                    .updatedApp(item.getUpdatedApp())
                    .build();
        }).collect(Collectors.toList());
    }
}
