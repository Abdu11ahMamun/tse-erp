package com.tse.erp.module.accounting.service.impl;

import com.tse.erp.common.AppConstants;
import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.accounting.dto.AfmPeriodRequestDto;
import com.tse.erp.module.accounting.dto.AfmPeriodResponseDto;
import com.tse.erp.module.accounting.entity.AfmPeriod;
import com.tse.erp.module.accounting.repository.AfmCalendarMonthRepository;
import com.tse.erp.module.accounting.repository.AfmCalendarRepository;
import com.tse.erp.module.accounting.repository.AfmPeriodRepository;
import com.tse.erp.module.accounting.service.AfmPeriodService;
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
public class AfmPeriodServiceImpl implements AfmPeriodService {

    private final AfmPeriodRepository periodRepository;
    private final AfmCalendarRepository calendarRepository;
    private final AfmCalendarMonthRepository calendarMonthRepository;
    private final BusinessGroupRepository businessGroupRepository;
    private final BusinessUnitRepository businessUnitRepository;

    @Override
    public List<AfmPeriodResponseDto> getAllPeriods(
            Long bgId,
            Long buId,
            String periodYear,
            String periodStatus,
            Integer status) {

        List<AfmPeriod> list;

        if (bgId != null && buId != null && status != null) {
            list = periodRepository.findByBgIdAndBuIdAndStatusOrderByIdDesc(bgId, buId, status);
        } else if (bgId != null && buId != null) {
            list = periodRepository.findByBgIdAndBuIdOrderByIdDesc(bgId, buId);
        } else if (buId != null && status != null) {
            list = periodRepository.findByBuIdAndStatusOrderByIdDesc(buId, status);
        } else if (buId != null) {
            list = periodRepository.findByBuIdOrderByIdDesc(buId);
        } else if (bgId != null) {
            list = periodRepository.findByBgIdOrderByIdDesc(bgId);
        } else {
            list = periodRepository.findAllByOrderByIdDesc();
        }

        if (periodYear != null && !periodYear.trim().isEmpty()) {
            list = list.stream().filter(p -> periodYear.trim().equalsIgnoreCase(p.getPeriodYear())).toList();
        }
        if (periodStatus != null && !periodStatus.trim().isEmpty()) {
            list = list.stream().filter(p -> periodStatus.trim().equalsIgnoreCase(p.getPeriodStatus())).toList();
        }
        if (status != null && (bgId == null || buId == null)) {
            list = list.stream().filter(p -> status.equals(p.getStatus())).toList();
        }

        return toResponseList(list);
    }

    @Override
    public AfmPeriodResponseDto getPeriodById(Long id) {
        return toResponse(findPeriod(id));
    }

    @Override
    @Transactional
    public AfmPeriodResponseDto createPeriod(AfmPeriodRequestDto request) {
        validateParents(request.getBgId(), request.getBuId());
        validatePeriodFields(request);
        StatusUtil.validate(request.getStatus());

        String year = request.getPeriodYear().trim();
        String name = request.getPeriodName().trim();

        boolean exists = periodRepository.existsByBuIdAndPeriodYearAndPeriodNameIgnoreCase(
                request.getBuId(), year, name
        );
        if (exists) {
            throw new DuplicateResourceException(
                    "Accounting Period '" + name + "' for Year " + year + " already exists for this Business Unit"
            );
        }

        String periodStatus = request.getPeriodStatus() != null && !request.getPeriodStatus().trim().isEmpty()
                ? request.getPeriodStatus().trim()
                : "Open";

        AfmPeriod entity = AfmPeriod.builder()
                .bgId(request.getBgId())
                .buId(request.getBuId())
                .periodYear(year)
                .periodName(name)
                .periodStatus(periodStatus)
                .build();

        entity.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        entity.setCreatedApp(AppConstants.APP_NAME);

        return toResponse(periodRepository.save(entity));
    }

    @Override
    @Transactional
    public AfmPeriodResponseDto updatePeriod(Long id, AfmPeriodRequestDto request) {
        AfmPeriod entity = findPeriod(id);
        validateParents(request.getBgId(), request.getBuId());
        validatePeriodFields(request);
        StatusUtil.validate(request.getStatus());

        String year = request.getPeriodYear().trim();
        String name = request.getPeriodName().trim();

        List<AfmPeriod> duplicates = periodRepository.findByBuIdAndPeriodYearAndPeriodNameIgnoreCase(
                request.getBuId(), year, name
        );
        boolean duplicate = duplicates.stream().anyMatch(p -> !p.getId().equals(id));
        if (duplicate) {
            throw new DuplicateResourceException(
                    "Accounting Period '" + name + "' for Year " + year + " already exists for this Business Unit"
            );
        }

        entity.setBgId(request.getBgId());
        entity.setBuId(request.getBuId());
        entity.setPeriodYear(year);
        entity.setPeriodName(name);
        if (request.getPeriodStatus() != null && !request.getPeriodStatus().trim().isEmpty()) {
            entity.setPeriodStatus(request.getPeriodStatus().trim());
        }
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }
        entity.setUpdatedApp(AppConstants.APP_NAME);

        return toResponse(periodRepository.save(entity));
    }

    @Override
    @Transactional
    public void deletePeriod(Long id) {
        findPeriod(id);
        periodRepository.deleteById(id);
    }

    @Override
    public List<String> getOpenPeriodNames(Long buId, String year) {
        if (buId == null || year == null || year.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return calendarMonthRepository.findOpenPeriodNamesByBuIdAndYear(buId, year.trim());
    }

    @Override
    public List<String> getAvailableYears(Long buId) {
        if (buId == null) {
            return Collections.emptyList();
        }
        return calendarMonthRepository.findDistinctCalendarYearsByBuId(buId);
    }

    private AfmPeriod findPeriod(Long id) {
        return periodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Accounting Period with id " + id + " not found"));
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

    private void validatePeriodFields(AfmPeriodRequestDto request) {
        if (request.getPeriodYear() == null || request.getPeriodYear().trim().isEmpty()) {
            throw new BadRequestException("Year cannot be empty");
        }
        if (request.getPeriodName() == null || request.getPeriodName().trim().isEmpty()) {
            throw new BadRequestException("Period Name cannot be empty");
        }
    }

    private AfmPeriodResponseDto toResponse(AfmPeriod entity) {
        return toResponseList(List.of(entity)).get(0);
    }

    private List<AfmPeriodResponseDto> toResponseList(List<AfmPeriod> list) {
        if (list.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> bgIds = list.stream().map(AfmPeriod::getBgId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> buIds = list.stream().map(AfmPeriod::getBuId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<Long, BusinessGroup> bgMap = businessGroupRepository.findAllById(bgIds).stream()
                .collect(Collectors.toMap(BusinessGroup::getId, Function.identity()));
        Map<Long, BusinessUnit> buMap = businessUnitRepository.findAllById(buIds).stream()
                .collect(Collectors.toMap(BusinessUnit::getId, Function.identity()));

        return list.stream().map(item -> {
            BusinessGroup bg = bgMap.get(item.getBgId());
            BusinessUnit bu = buMap.get(item.getBuId());

            return AfmPeriodResponseDto.builder()
                    .id(item.getId())
                    .bgId(item.getBgId())
                    .groupName(bg != null ? bg.getGroupName() : null)
                    .buId(item.getBuId())
                    .businessUnitName(bu != null ? bu.getBusinessUnit() : null)
                    .periodYear(item.getPeriodYear())
                    .periodName(item.getPeriodName())
                    .periodStatus(item.getPeriodStatus())
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

