package com.tse.erp.module.accounting.service.impl;

import com.tse.erp.common.AppConstants;
import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.accounting.dto.AfmCalendarMonthDto;
import com.tse.erp.module.accounting.dto.AfmPeriodRequestDto;
import com.tse.erp.module.accounting.dto.AfmPeriodResponseDto;
import com.tse.erp.module.accounting.entity.AfmCalendarMonth;
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
                .afmCalendarMonthId(request.getAfmCalendarMonthId())
                .build();
        entity.setStatus(request.getStatus() != null ? request.getStatus() : 1);

        AfmPeriod saved = periodRepository.save(entity);

        // Sync AFM_CALENDAR_MONTH PERIOD_STATUS if afmCalendarMonthId or periodName match
        syncCalendarMonthStatus(saved.getBuId(), saved.getPeriodYear(), saved.getPeriodName(), saved.getAfmCalendarMonthId(), saved.getPeriodStatus());

        return toResponse(saved);
    }

    @Override
    @Transactional
    public AfmPeriodResponseDto updatePeriod(Long id, AfmPeriodRequestDto request) {
        AfmPeriod existing = findPeriod(id);

        if (request.getBgId() != null || request.getBuId() != null) {
            Long nextBgId = request.getBgId() != null ? request.getBgId() : existing.getBgId();
            Long nextBuId = request.getBuId() != null ? request.getBuId() : existing.getBuId();
            validateParents(nextBgId, nextBuId);
            existing.setBgId(nextBgId);
            existing.setBuId(nextBuId);
        }

        if (request.getPeriodYear() != null && !request.getPeriodYear().trim().isEmpty()) {
            existing.setPeriodYear(request.getPeriodYear().trim());
        }

        if (request.getPeriodName() != null && !request.getPeriodName().trim().isEmpty()) {
            String newName = request.getPeriodName().trim();
            if (!newName.equalsIgnoreCase(existing.getPeriodName())) {
                boolean exists = periodRepository.existsByBuIdAndPeriodYearAndPeriodNameIgnoreCase(
                        existing.getBuId(), existing.getPeriodYear(), newName
                );
                if (exists) {
                    throw new DuplicateResourceException(
                            "Accounting Period '" + newName + "' for Year " + existing.getPeriodYear()
                                    + " already exists for this Business Unit"
                    );
                }
            }
            existing.setPeriodName(newName);
        }

        if (request.getPeriodStatus() != null && !request.getPeriodStatus().trim().isEmpty()) {
            existing.setPeriodStatus(request.getPeriodStatus().trim());
        }

        if (request.getAfmCalendarMonthId() != null) {
            existing.setAfmCalendarMonthId(request.getAfmCalendarMonthId());
        }

        if (request.getStatus() != null) {
            StatusUtil.validate(request.getStatus());
            existing.setStatus(request.getStatus());
        }

        AfmPeriod saved = periodRepository.save(existing);

        // Sync AFM_CALENDAR_MONTH PERIOD_STATUS
        syncCalendarMonthStatus(saved.getBuId(), saved.getPeriodYear(), saved.getPeriodName(), saved.getAfmCalendarMonthId(), saved.getPeriodStatus());

        return toResponse(saved);
    }

    private void syncCalendarMonthStatus(Long buId, String periodYear, String periodName, Long calendarMonthId, String periodStatus) {
        if (calendarMonthId != null) {
            calendarMonthRepository.findById(calendarMonthId).ifPresent(month -> {
                month.setPeriodStatus(periodStatus);
                calendarMonthRepository.save(month);
            });
            return;
        }

        if (buId != null && periodYear != null && periodName != null) {
            calendarMonthRepository.findByBuIdAndYearAndPeriodName(buId, periodYear, periodName).ifPresent(month -> {
                month.setPeriodStatus(periodStatus);
                calendarMonthRepository.save(month);
            });
        }
    }

    @Override
    @Transactional
    public void deletePeriod(Long id) {
        findPeriod(id);
        periodRepository.deleteById(id);
    }

    @Override
    public List<String> getOpenPeriodNames(Long buId, String year) {
        if (buId == null) {
            return Collections.emptyList();
        }
        String cleanYear = (year != null && !year.trim().isEmpty()) ? year.trim() : null;
        return calendarMonthRepository.findOpenPeriodNamesByBuIdAndYear(buId, cleanYear);
    }

    @Override
    public List<String> getAvailableYears(Long buId) {
        if (buId == null) {
            return Collections.emptyList();
        }
        return calendarMonthRepository.findDistinctCalendarYearsByBuId(buId);
    }

    @Override
    public List<AfmCalendarMonthDto> getCandidateMonths(Long buId, String year, String monthPeriodStatus) {
        if (buId == null || year == null || year.trim().isEmpty() || monthPeriodStatus == null) {
            return Collections.emptyList();
        }

        String targetStatus = monthPeriodStatus.trim().toLowerCase();
        List<AfmCalendarMonth> allMonths = calendarMonthRepository.findByBuIdAndYear(buId, year.trim());

        List<AfmCalendarMonth> matchingMonths = allMonths.stream()
                .filter(m -> {
                    String status = m.getPeriodStatus() == null ? "" : m.getPeriodStatus().trim().toLowerCase();
                    return status.equals(targetStatus);
                })
                .toList();

        List<AfmPeriod> existingPeriods = periodRepository.findByBuIdAndPeriodYearOrderByIdDesc(buId, year.trim());
        Set<String> insertedNames = existingPeriods.stream()
                .map(p -> p.getPeriodName() == null ? "" : p.getPeriodName().trim().toLowerCase())
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());

        return matchingMonths.stream()
                .filter(m -> m.getPeriodName() != null && !insertedNames.contains(m.getPeriodName().trim().toLowerCase()))
                .map(m -> AfmCalendarMonthDto.builder()
                        .id(m.getId())
                        .afmCalendarId(m.getAfmCalendarId())
                        .serialNo(m.getSerialNo())
                        .calendarYear(m.getCalendarYear())
                        .calendarMonth(m.getCalendarMonth())
                        .calendarQuater(m.getCalendarQuater())
                        .fromDate(m.getFromDate())
                        .toDate(m.getToDate())
                        .periodName(m.getPeriodName())
                        .isAdjustmentPeriod(m.getIsAdjustmentPeriod())
                        .periodStatus(m.getPeriodStatus())
                        .status(m.getStatus())
                        .build())
                .collect(Collectors.toList());
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
                    .afmCalendarMonthId(item.getAfmCalendarMonthId())
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
