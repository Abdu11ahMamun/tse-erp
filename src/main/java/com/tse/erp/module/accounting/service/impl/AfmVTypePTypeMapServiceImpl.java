package com.tse.erp.module.accounting.service.impl;

import com.tse.erp.common.AppConstants;
import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.accounting.dto.AfmVTypePTypeMapRequestDto;
import com.tse.erp.module.accounting.dto.AfmVTypePTypeMapResponseDto;
import com.tse.erp.module.accounting.entity.AfmLookupValue;
import com.tse.erp.module.accounting.entity.AfmVTypePTypeMap;
import com.tse.erp.module.accounting.repository.AfmLookupValueRepository;
import com.tse.erp.module.accounting.repository.AfmVTypePTypeMapRepository;
import com.tse.erp.module.accounting.service.AfmVTypePTypeMapService;
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
public class AfmVTypePTypeMapServiceImpl implements AfmVTypePTypeMapService {

    private final AfmVTypePTypeMapRepository mappingRepository;
    private final AfmLookupValueRepository lookupValueRepository;
    private final BusinessGroupRepository businessGroupRepository;
    private final BusinessUnitRepository businessUnitRepository;

    @Override
    public List<AfmVTypePTypeMapResponseDto> getAllMappings(Long bgId, Long buId, Integer status) {
        List<AfmVTypePTypeMap> mappings;

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
    public AfmVTypePTypeMapResponseDto getMappingById(Long id) {
        return toResponse(findMapping(id));
    }

    @Override
    public List<AfmVTypePTypeMapResponseDto> getMappingsByVoucherType(Long buId, Long voucherTypeId, Integer status) {
        if (buId == null) {
            throw new BadRequestException("Business Unit id cannot be empty");
        }
        if (voucherTypeId == null) {
            throw new BadRequestException("Voucher Type id cannot be empty");
        }

        List<AfmVTypePTypeMap> mappings;
        if (status != null) {
            mappings = mappingRepository.findByBuIdAndVoucherTypeIdAndStatusOrderByIdDesc(buId, voucherTypeId, status);
        } else {
            mappings = mappingRepository.findByBuIdAndVoucherTypeIdOrderByIdDesc(buId, voucherTypeId);
        }

        return toResponseList(mappings);
    }

    @Override
    @Transactional
    public AfmVTypePTypeMapResponseDto createMapping(AfmVTypePTypeMapRequestDto request) {
        validateParents(request.getBgId(), request.getBuId());
        validateLookupValues(request.getBgId(), request.getBuId(), request.getVoucherTypeId(), request.getPaymentTypeId());
        StatusUtil.validate(request.getStatus());

        boolean exists = mappingRepository.existsByBuIdAndVoucherTypeIdAndPaymentTypeId(
                request.getBuId(),
                request.getVoucherTypeId(),
                request.getPaymentTypeId()
        );
        if (exists) {
            throw new DuplicateResourceException("This Voucher Type and Payment Type mapping already exists for the selected Business Unit");
        }

        AfmVTypePTypeMap entity = AfmVTypePTypeMap.builder()
                .bgId(request.getBgId())
                .buId(request.getBuId())
                .voucherTypeId(request.getVoucherTypeId())
                .paymentTypeId(request.getPaymentTypeId())
                .build();

        entity.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        entity.setCreatedApp(AppConstants.APP_NAME);

        AfmVTypePTypeMap saved = mappingRepository.save(entity);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public AfmVTypePTypeMapResponseDto updateMapping(Long id, AfmVTypePTypeMapRequestDto request) {
        AfmVTypePTypeMap existing = findMapping(id);

        validateParents(request.getBgId(), request.getBuId());
        validateLookupValues(request.getBgId(), request.getBuId(), request.getVoucherTypeId(), request.getPaymentTypeId());
        StatusUtil.validate(request.getStatus());

        Optional<AfmVTypePTypeMap> duplicate = mappingRepository.findByBuIdAndVoucherTypeIdAndPaymentTypeId(
                request.getBuId(),
                request.getVoucherTypeId(),
                request.getPaymentTypeId()
        );
        if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
            throw new DuplicateResourceException("This Voucher Type and Payment Type mapping already exists for the selected Business Unit");
        }

        existing.setBgId(request.getBgId());
        existing.setBuId(request.getBuId());
        existing.setVoucherTypeId(request.getVoucherTypeId());
        existing.setPaymentTypeId(request.getPaymentTypeId());
        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }
        existing.setUpdatedApp(AppConstants.APP_NAME);

        AfmVTypePTypeMap saved = mappingRepository.save(existing);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteMapping(Long id) {
        AfmVTypePTypeMap existing = findMapping(id);
        mappingRepository.delete(existing);
    }

    // =========================================
    // HELPER & VALIDATION METHODS
    // =========================================

    private AfmVTypePTypeMap findMapping(Long id) {
        return mappingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher Type to Payment Type mapping not found with id: " + id));
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

    private void validateLookupValues(Long bgId, Long buId, Long voucherTypeId, Long paymentTypeId) {
        if (voucherTypeId == null) {
            throw new BadRequestException("Voucher Type cannot be empty");
        }
        if (paymentTypeId == null) {
            throw new BadRequestException("Payment Type cannot be empty");
        }

        AfmLookupValue voucherType = lookupValueRepository.findById(voucherTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher Type lookup value not found with id: " + voucherTypeId));

        if (!Integer.valueOf(1).equals(voucherType.getStatus())) {
            throw new BadRequestException("Selected Voucher Type is not active");
        }
        if (!bgId.equals(voucherType.getBgId()) || !buId.equals(voucherType.getBuId())) {
            throw new BadRequestException("Voucher Type does not belong to the selected Business Group/Business Unit");
        }

        AfmLookupValue paymentType = lookupValueRepository.findById(paymentTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment Type lookup value not found with id: " + paymentTypeId));

        if (!Integer.valueOf(1).equals(paymentType.getStatus())) {
            throw new BadRequestException("Selected Payment Type is not active");
        }
        if (!bgId.equals(paymentType.getBgId()) || !buId.equals(paymentType.getBuId())) {
            throw new BadRequestException("Payment Type does not belong to the selected Business Group/Business Unit");
        }
    }

    private AfmVTypePTypeMapResponseDto toResponse(AfmVTypePTypeMap mapping) {
        AfmLookupValue voucherType = lookupValueRepository.findById(mapping.getVoucherTypeId()).orElse(null);
        AfmLookupValue paymentType = lookupValueRepository.findById(mapping.getPaymentTypeId()).orElse(null);

        return AfmVTypePTypeMapResponseDto.builder()
                .id(mapping.getId())
                .bgId(mapping.getBgId())
                .buId(mapping.getBuId())
                .voucherTypeId(mapping.getVoucherTypeId())
                .voucherTypeValue(voucherType != null ? voucherType.getValue() : null)
                .voucherTypeValueCode(voucherType != null ? voucherType.getValueCode() : null)
                .paymentTypeId(mapping.getPaymentTypeId())
                .paymentTypeValue(paymentType != null ? paymentType.getValue() : null)
                .paymentTypeValueCode(paymentType != null ? paymentType.getValueCode() : null)
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

    private List<AfmVTypePTypeMapResponseDto> toResponseList(List<AfmVTypePTypeMap> mappings) {
        if (mappings == null || mappings.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> lookupIds = new HashSet<>();
        for (AfmVTypePTypeMap m : mappings) {
            lookupIds.add(m.getVoucherTypeId());
            lookupIds.add(m.getPaymentTypeId());
        }

        Map<Long, AfmLookupValue> lookupMap = lookupValueRepository.findAllById(lookupIds)
                .stream()
                .collect(Collectors.toMap(AfmLookupValue::getId, Function.identity(), (a, b) -> a));

        return mappings.stream().map(m -> {
            AfmLookupValue voucherType = lookupMap.get(m.getVoucherTypeId());
            AfmLookupValue paymentType = lookupMap.get(m.getPaymentTypeId());

            return AfmVTypePTypeMapResponseDto.builder()
                    .id(m.getId())
                    .bgId(m.getBgId())
                    .buId(m.getBuId())
                    .voucherTypeId(m.getVoucherTypeId())
                    .voucherTypeValue(voucherType != null ? voucherType.getValue() : null)
                    .voucherTypeValueCode(voucherType != null ? voucherType.getValueCode() : null)
                    .paymentTypeId(m.getPaymentTypeId())
                    .paymentTypeValue(paymentType != null ? paymentType.getValue() : null)
                    .paymentTypeValueCode(paymentType != null ? paymentType.getValueCode() : null)
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