package com.tse.erp.module.accounting.service.impl;

import com.tse.erp.common.AppConstants;
import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.accounting.dto.AfmPTypePThroughMapRequestDto;
import com.tse.erp.module.accounting.dto.AfmPTypePThroughMapResponseDto;
import com.tse.erp.module.accounting.entity.AfmLookupValue;
import com.tse.erp.module.accounting.entity.AfmPTypePThroughMap;
import com.tse.erp.module.accounting.repository.AfmLookupValueRepository;
import com.tse.erp.module.accounting.repository.AfmPTypePThroughMapRepository;
import com.tse.erp.module.accounting.service.AfmPTypePThroughMapService;
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
public class AfmPTypePThroughMapServiceImpl implements AfmPTypePThroughMapService {

    private final AfmPTypePThroughMapRepository mappingRepository;
    private final AfmLookupValueRepository lookupValueRepository;
    private final BusinessGroupRepository businessGroupRepository;
    private final BusinessUnitRepository businessUnitRepository;

    @Override
    public List<AfmPTypePThroughMapResponseDto> getAllMappings(Long bgId, Long buId, Integer status) {
        List<AfmPTypePThroughMap> mappings;

        if (bgId != null && buId != null && status != null) {
            mappings = mappingRepository.findByBgIdAndBuIdAndStatusOrderByIdDesc(bgId, buId, status);
        } else if (bgId != null && buId != null) {
            mappings = mappingRepository.findByBgIdAndBuIdOrderByIdDesc(bgId, buId);
        } else if (buId != null && status != null) {
            mappings = mappingRepository.findByBuIdAndStatusOrderByIdDesc(buId, status);
        } else if (buId != null) {
            mappings = mappingRepository.findByBuIdOrderByIdDesc(buId);
        } else if (bgId != null) {
            mappings = mappingRepository.findByBgIdOrderByIdDesc(bgId);
        } else {
            mappings = mappingRepository.findAllByOrderByIdDesc();
        }

        if (status != null && (bgId != null && buId == null || bgId == null && buId == null)) {
            mappings = mappings.stream().filter(m -> status.equals(m.getStatus())).toList();
        }

        return toResponseList(mappings);
    }

    @Override
    public AfmPTypePThroughMapResponseDto getMappingById(Long id) {
        return toResponse(findMapping(id));
    }

    @Override
    public List<AfmPTypePThroughMapResponseDto> getMappingsByPaymentType(Long buId, Long paymentTypeId, Integer status) {
        if (buId == null) {
            throw new BadRequestException("Business Unit id cannot be empty");
        }
        if (paymentTypeId == null) {
            throw new BadRequestException("Payment Type id cannot be empty");
        }

        List<AfmPTypePThroughMap> mappings;
        if (status != null) {
            mappings = mappingRepository.findByBuIdAndPaymentTypeIdAndStatusOrderByIdDesc(buId, paymentTypeId, status);
        } else {
            mappings = mappingRepository.findByBuIdAndPaymentTypeIdOrderByIdDesc(buId, paymentTypeId);
        }

        return toResponseList(mappings);
    }

    @Override
    @Transactional
    public AfmPTypePThroughMapResponseDto createMapping(AfmPTypePThroughMapRequestDto request) {
        validateParents(request.getBgId(), request.getBuId());
        validateLookupValues(request.getBgId(), request.getBuId(), request.getPaymentTypeId(), request.getPaymentThroughId());
        StatusUtil.validate(request.getStatus());

        boolean exists = mappingRepository.existsByBuIdAndPaymentTypeIdAndPaymentThroughId(
                request.getBuId(),
                request.getPaymentTypeId(),
                request.getPaymentThroughId()
        );
        if (exists) {
            throw new DuplicateResourceException("This Payment Type and Payment Through mapping already exists for the selected Business Unit");
        }

        AfmPTypePThroughMap entity = AfmPTypePThroughMap.builder()
                .bgId(request.getBgId())
                .buId(request.getBuId())
                .paymentTypeId(request.getPaymentTypeId())
                .paymentThroughId(request.getPaymentThroughId())
                .build();

        entity.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        entity.setCreatedApp(AppConstants.APP_NAME);

        AfmPTypePThroughMap saved = mappingRepository.save(entity);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public AfmPTypePThroughMapResponseDto updateMapping(Long id, AfmPTypePThroughMapRequestDto request) {
        AfmPTypePThroughMap existing = findMapping(id);

        validateParents(request.getBgId(), request.getBuId());
        validateLookupValues(request.getBgId(), request.getBuId(), request.getPaymentTypeId(), request.getPaymentThroughId());
        StatusUtil.validate(request.getStatus());

        Optional<AfmPTypePThroughMap> duplicate = mappingRepository.findByBuIdAndPaymentTypeIdAndPaymentThroughId(
                request.getBuId(),
                request.getPaymentTypeId(),
                request.getPaymentThroughId()
        );
        if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
            throw new DuplicateResourceException("This Payment Type and Payment Through mapping already exists for the selected Business Unit");
        }

        existing.setBgId(request.getBgId());
        existing.setBuId(request.getBuId());
        existing.setPaymentTypeId(request.getPaymentTypeId());
        existing.setPaymentThroughId(request.getPaymentThroughId());
        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }
        existing.setUpdatedApp(AppConstants.APP_NAME);

        AfmPTypePThroughMap saved = mappingRepository.save(existing);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteMapping(Long id) {
        AfmPTypePThroughMap existing = findMapping(id);
        mappingRepository.delete(existing);
    }

    // =========================================
    // HELPER & VALIDATION METHODS
    // =========================================

    private AfmPTypePThroughMap findMapping(Long id) {
        return mappingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment Type to Payment Through mapping not found with id: " + id));
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

    private void validateLookupValues(Long bgId, Long buId, Long paymentTypeId, Long paymentThroughId) {
        if (paymentTypeId == null) {
            throw new BadRequestException("Payment Type cannot be empty");
        }
        if (paymentThroughId == null) {
            throw new BadRequestException("Payment Through cannot be empty");
        }

        AfmLookupValue paymentType = lookupValueRepository.findById(paymentTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment Type lookup value not found with id: " + paymentTypeId));

        if (!Integer.valueOf(1).equals(paymentType.getStatus())) {
            throw new BadRequestException("Selected Payment Type is not active");
        }
        if (!bgId.equals(paymentType.getBgId()) || !buId.equals(paymentType.getBuId())) {
            throw new BadRequestException("Payment Type does not belong to the selected Business Group/Business Unit");
        }

        AfmLookupValue paymentThrough = lookupValueRepository.findById(paymentThroughId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment Through lookup value not found with id: " + paymentThroughId));

        if (!Integer.valueOf(1).equals(paymentThrough.getStatus())) {
            throw new BadRequestException("Selected Payment Through is not active");
        }
        if (!bgId.equals(paymentThrough.getBgId()) || !buId.equals(paymentThrough.getBuId())) {
            throw new BadRequestException("Payment Through does not belong to the selected Business Group/Business Unit");
        }
    }

    private AfmPTypePThroughMapResponseDto toResponse(AfmPTypePThroughMap mapping) {
        AfmLookupValue paymentType = lookupValueRepository.findById(mapping.getPaymentTypeId()).orElse(null);
        AfmLookupValue paymentThrough = lookupValueRepository.findById(mapping.getPaymentThroughId()).orElse(null);

        return AfmPTypePThroughMapResponseDto.builder()
                .id(mapping.getId())
                .bgId(mapping.getBgId())
                .buId(mapping.getBuId())
                .paymentTypeId(mapping.getPaymentTypeId())
                .paymentTypeValue(paymentType != null ? paymentType.getValue() : null)
                .paymentTypeValueCode(paymentType != null ? paymentType.getValueCode() : null)
                .paymentThroughId(mapping.getPaymentThroughId())
                .paymentThroughValue(paymentThrough != null ? paymentThrough.getValue() : null)
                .paymentThroughValueCode(paymentThrough != null ? paymentThrough.getValueCode() : null)
                .status(mapping.getStatus())
                .createdUserId(mapping.getCreatedUserId())
                .createdEmpId(mapping.getCreatedEmpId())
                .createdDatetime(mapping.getCreatedDatetime())
                .updatedUserId(mapping.getUpdatedUserId())
                .updatedEmpId(mapping.getUpdatedEmpId())
                .updatedDatetime(mapping.getUpdatedDatetime())
                .createdApp(mapping.getCreatedApp())
                .updatedApp(mapping.getUpdatedApp())
                .build();
    }

    private List<AfmPTypePThroughMapResponseDto> toResponseList(List<AfmPTypePThroughMap> mappings) {
        if (mappings == null || mappings.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> lookupIds = new HashSet<>();
        for (AfmPTypePThroughMap m : mappings) {
            lookupIds.add(m.getPaymentTypeId());
            lookupIds.add(m.getPaymentThroughId());
        }

        Map<Long, AfmLookupValue> lookupMap = lookupValueRepository.findAllById(lookupIds)
                .stream()
                .collect(Collectors.toMap(AfmLookupValue::getId, Function.identity(), (a, b) -> a));

        return mappings.stream().map(m -> {
            AfmLookupValue paymentType = lookupMap.get(m.getPaymentTypeId());
            AfmLookupValue paymentThrough = lookupMap.get(m.getPaymentThroughId());

            return AfmPTypePThroughMapResponseDto.builder()
                    .id(m.getId())
                    .bgId(m.getBgId())
                    .buId(m.getBuId())
                    .paymentTypeId(m.getPaymentTypeId())
                    .paymentTypeValue(paymentType != null ? paymentType.getValue() : null)
                    .paymentTypeValueCode(paymentType != null ? paymentType.getValueCode() : null)
                    .paymentThroughId(m.getPaymentThroughId())
                    .paymentThroughValue(paymentThrough != null ? paymentThrough.getValue() : null)
                    .paymentThroughValueCode(paymentThrough != null ? paymentThrough.getValueCode() : null)
                    .status(m.getStatus())
                    .createdUserId(m.getCreatedUserId())
                    .createdEmpId(m.getCreatedEmpId())
                    .createdDatetime(m.getCreatedDatetime())
                    .updatedUserId(m.getUpdatedUserId())
                    .updatedEmpId(m.getUpdatedEmpId())
                    .updatedDatetime(m.getUpdatedDatetime())
                    .createdApp(m.getCreatedApp())
                    .updatedApp(m.getUpdatedApp())
                    .build();
        }).toList();
    }
}
