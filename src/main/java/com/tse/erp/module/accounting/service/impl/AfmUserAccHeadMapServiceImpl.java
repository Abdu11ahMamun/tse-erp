package com.tse.erp.module.accounting.service.impl;

import com.tse.erp.common.AppConstants;
import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.accounting.dto.AfmUserAccHeadMapDto;
import com.tse.erp.module.accounting.entity.AfmChartOfAccounts;
import com.tse.erp.module.accounting.entity.AfmUserAccHeadMap;
import com.tse.erp.module.accounting.repository.AfmChartOfAccountsRepository;
import com.tse.erp.module.accounting.repository.AfmUserAccHeadMapRepository;
import com.tse.erp.module.accounting.service.AfmUserAccHeadMapService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AfmUserAccHeadMapServiceImpl implements AfmUserAccHeadMapService {

    private final AfmUserAccHeadMapRepository userAccHeadMapRepository;
    private final AfmChartOfAccountsRepository chartOfAccountsRepository;

    @Override
    public List<AfmUserAccHeadMapDto> getByUserId(Long userId, Long buId) {
        if (userId == null) {
            return List.of();
        }
        List<AfmUserAccHeadMap> list;
        if (buId != null) {
            list = userAccHeadMapRepository.findByBuIdAndUserIdOrderByIdAsc(buId, userId);
        } else {
            list = userAccHeadMapRepository.findByUserIdOrderByIdAsc(userId);
        }

        List<Long> coaIds = list.stream().map(AfmUserAccHeadMap::getAccountHeadId).distinct().toList();
        Map<Long, AfmChartOfAccounts> coaMap = chartOfAccountsRepository.findAllById(coaIds)
                .stream()
                .collect(Collectors.toMap(AfmChartOfAccounts::getId, c -> c));

        return list.stream().map(entity -> toDto(entity, coaMap.get(entity.getAccountHeadId()))).toList();
    }

    @Override
    @Transactional
    public AfmUserAccHeadMapDto create(AfmUserAccHeadMapDto dto) {
        if (dto.getUserId() == null) throw new BadRequestException("User ID is required");
        if (dto.getAccountHeadId() == null) throw new BadRequestException("Account Head ID is required");
        if (dto.getBgId() == null) throw new BadRequestException("Business Group ID is required");
        if (dto.getBuId() == null) throw new BadRequestException("Business Unit ID is required");

        if (userAccHeadMapRepository.existsByBuIdAndUserIdAndAccountHeadId(dto.getBuId(), dto.getUserId(), dto.getAccountHeadId())) {
            throw new BadRequestException("Same Accounts Name is not allowed to be submitted against one user");
        }

        AfmChartOfAccounts coa = chartOfAccountsRepository.findById(dto.getAccountHeadId())
                .orElseThrow(() -> new ResourceNotFoundException("Account Head not found with id: " + dto.getAccountHeadId()));

        StatusUtil.validate(dto.getStatus());

        AfmUserAccHeadMap entity = AfmUserAccHeadMap.builder()
                .bgId(dto.getBgId())
                .buId(dto.getBuId())
                .userId(dto.getUserId())
                .accountHeadId(dto.getAccountHeadId())
                .build();

        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        entity.setCreatedApp(AppConstants.APP_NAME);

        return toDto(userAccHeadMapRepository.save(entity), coa);
    }

    @Override
    @Transactional
    public AfmUserAccHeadMapDto update(Long id, AfmUserAccHeadMapDto dto) {
        AfmUserAccHeadMap existing = userAccHeadMapRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User Account Head Map not found with id: " + id));

        if (dto.getAccountHeadId() != null && !dto.getAccountHeadId().equals(existing.getAccountHeadId())) {
            userAccHeadMapRepository.findByBuIdAndUserIdAndAccountHeadId(existing.getBuId(), existing.getUserId(), dto.getAccountHeadId())
                    .ifPresent(other -> {
                        if (!other.getId().equals(id)) {
                            throw new BadRequestException("Same Accounts Name is not allowed to be submitted against one user");
                        }
                    });
            existing.setAccountHeadId(dto.getAccountHeadId());
        }
        if (dto.getStatus() != null) {
            StatusUtil.validate(dto.getStatus());
            existing.setStatus(dto.getStatus());
        }
        existing.setUpdatedApp(AppConstants.APP_NAME);

        AfmChartOfAccounts coa = chartOfAccountsRepository.findById(existing.getAccountHeadId()).orElse(null);
        return toDto(userAccHeadMapRepository.save(existing), coa);
    }

    @Override
    @Transactional
    public List<AfmUserAccHeadMapDto> saveAllForUser(Long bgId, Long buId, Long userId, List<AfmUserAccHeadMapDto> items) {
        if (userId == null || items == null) {
            return List.of();
        }

        // Validate no duplicate account heads are submitted for the user
        Set<Long> seenHeadIds = new HashSet<>();
        for (AfmUserAccHeadMapDto item : items) {
            if (item.getAccountHeadId() != null) {
                if (!seenHeadIds.add(item.getAccountHeadId())) {
                    throw new BadRequestException("Same Accounts Name is not allowed to be submitted against one user (Account Head ID: " + item.getAccountHeadId() + ")");
                }
            }
        }

        List<AfmUserAccHeadMapDto> result = new ArrayList<>();
        for (AfmUserAccHeadMapDto item : items) {
            if (item.getAccountHeadId() == null) continue;

            if (item.getId() != null) {
                userAccHeadMapRepository.findById(item.getId()).ifPresent(existing -> {
                    if (!existing.getAccountHeadId().equals(item.getAccountHeadId())) {
                        userAccHeadMapRepository.findByBuIdAndUserIdAndAccountHeadId(buId, userId, item.getAccountHeadId())
                                .ifPresent(other -> {
                                    if (!other.getId().equals(existing.getId())) {
                                        throw new BadRequestException("Same Accounts Name is not allowed to be submitted against one user");
                                    }
                                });
                    }
                    existing.setAccountHeadId(item.getAccountHeadId());
                    if (item.getStatus() != null) {
                        existing.setStatus(item.getStatus());
                    }
                    existing.setUpdatedApp(AppConstants.APP_NAME);
                    AfmChartOfAccounts coa = chartOfAccountsRepository.findById(existing.getAccountHeadId()).orElse(null);
                    result.add(toDto(userAccHeadMapRepository.save(existing), coa));
                });
            } else {
                // Check if already mapped for this user & bu
                AfmUserAccHeadMap entity = userAccHeadMapRepository
                        .findByBuIdAndUserIdAndAccountHeadId(buId, userId, item.getAccountHeadId())
                        .orElseGet(() -> AfmUserAccHeadMap.builder()
                                .bgId(bgId != null ? bgId : item.getBgId())
                                .buId(buId != null ? buId : item.getBuId())
                                .userId(userId)
                                .accountHeadId(item.getAccountHeadId())
                                .build());

                entity.setStatus(item.getStatus() != null ? item.getStatus() : 1);
                entity.setCreatedApp(AppConstants.APP_NAME);
                entity.setUpdatedApp(AppConstants.APP_NAME);

                AfmChartOfAccounts coa = chartOfAccountsRepository.findById(entity.getAccountHeadId()).orElse(null);
                result.add(toDto(userAccHeadMapRepository.save(entity), coa));
            }
        }
        return result;
    }

    private AfmUserAccHeadMapDto toDto(AfmUserAccHeadMap entity, AfmChartOfAccounts coa) {
        String code = coa != null ? coa.getAccountCode() : null;
        String name = coa != null ? coa.getAccountHeadName() : null;
        String label = (code != null && name != null) ? (code + " - " + name) : (name != null ? name : (code != null ? code : String.valueOf(entity.getAccountHeadId())));

        return AfmUserAccHeadMapDto.builder()
                .id(entity.getId())
                .bgId(entity.getBgId())
                .buId(entity.getBuId())
                .userId(entity.getUserId())
                .accountHeadId(entity.getAccountHeadId())
                .accountCode(code)
                .accountHeadName(name)
                .accountDisplayLabel(label)
                .status(entity.getStatus())
                .createdUserId(entity.getCreatedUserId())
                .createdDatetime(entity.getCreatedDatetime())
                .updatedUserId(entity.getUpdatedUserId())
                .updatedDatetime(entity.getUpdatedDatetime())
                .build();
    }
}
