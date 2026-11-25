package com.tse.erp.module.asset.service.impl;

import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.asset.dto.AssetLookupValueBatchDto;
import com.tse.erp.module.asset.dto.AssetLookupValueRowDto;
import com.tse.erp.module.asset.entity.AssetLookupType;
import com.tse.erp.module.asset.entity.AssetLookupValue;
import com.tse.erp.module.asset.repository.AssetLookupTypeRepository;
import com.tse.erp.module.asset.repository.AssetLookupValueRepository;
import com.tse.erp.module.asset.service.AssetLookupValueBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AssetLookupValueBatchServiceImpl
        implements AssetLookupValueBatchService {

    private final AssetLookupValueRepository lookupValueRepository;
    private final AssetLookupTypeRepository lookupTypeRepository;
    private final BusinessGroupRepository businessGroupRepository;
    private final BusinessUnitRepository businessUnitRepository;

    @Override
    public AssetLookupValueBatchDto getByType(Long typeId) {
        AssetLookupType type = findType(typeId);
        List<AssetLookupValueRowDto> rows = new ArrayList<>();
        for (AssetLookupValue value
                : lookupValueRepository.findByTypeIdOrderByIdAsc(typeId)) {
            AssetLookupValueRowDto row = new AssetLookupValueRowDto();
            row.setId(value.getId());
            row.setValue(value.getValue());
            row.setValueCode(value.getValueCode());
            row.setSortOrder(value.getSortOrder());
            row.setStatus(value.getStatus());
            rows.add(row);
        }

        AssetLookupValueBatchDto result = new AssetLookupValueBatchDto();
        result.setBgId(type.getBgId());
        result.setBuId(type.getBuId());
        result.setTypeId(type.getId());
        result.setTypeCode(type.getTypeCode());
        result.setValues(rows);
        return result;
    }

    @Override
    @Transactional
    public AssetLookupValueBatchDto saveByType(
            Long typeId, AssetLookupValueBatchDto dto) {
        AssetLookupType type = findType(typeId);
        if (!Integer.valueOf(1).equals(type.getStatus())) {
            throw new BadRequestException(
                    "Selected Lookup Type is not active");
        }

        if (dto.getBgId() == null) {
            throw new BadRequestException(
                    "Business Group id cannot be empty");
        }
        if (dto.getBuId() == null) {
            throw new BadRequestException(
                    "Business Unit id cannot be empty");
        }
        businessGroupRepository.findById(dto.getBgId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Business Group not found with id: " + dto.getBgId()));
        BusinessUnit bu = businessUnitRepository.findById(dto.getBuId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Business Unit not found with id: " + dto.getBuId()));
        if (!dto.getBgId().equals(bu.getBgId())) {
            throw new BadRequestException(
                    "Business Unit does not belong to selected Business Group");
        }
        if (!dto.getBuId().equals(type.getBuId())) {
            throw new BadRequestException(
                    "Lookup Type does not belong to selected Business Unit");
        }

        String typeCode = cleanCode(
                dto.getTypeCode(), "Type code");
        if (typeCode == null) {
            typeCode = type.getTypeCode();
        }

        List<AssetLookupValueRowDto> rows = dto.getValues() == null
                ? Collections.emptyList()
                : dto.getValues();
        validateRows(rows);

        List<AssetLookupValue> current =
                lookupValueRepository.findByTypeIdOrderByIdAsc(typeId);
        Map<Long, AssetLookupValue> byId = new HashMap<>();
        current.forEach(value -> byId.put(value.getId(), value));
        Set<Long> handled = new HashSet<>();
        List<AssetLookupValue> newRows = new ArrayList<>();

        for (AssetLookupValueRowDto row : rows) {
            if (row.getId() != null) {
                AssetLookupValue existing = byId.get(row.getId());
                if (existing == null) {
                    throw new BadRequestException(
                            "Row id " + row.getId()
                                     + " does not belong to this Lookup Type");
                }
                existing.setValue(row.getValue());
                existing.setValueCode(row.getValueCode());
                existing.setSortOrder(row.getSortOrder());
                if (row.getStatus() != null) {
                    existing.setStatus(row.getStatus());
                }
                handled.add(row.getId());
            } else {
                AssetLookupValue newRow = new AssetLookupValue();
                newRow.setBgId(dto.getBgId());
                newRow.setBuId(dto.getBuId());
                newRow.setTypeId(typeId);
                newRow.setValue(row.getValue());
                newRow.setValueCode(row.getValueCode());
                newRow.setSortOrder(row.getSortOrder());
                if (row.getStatus() != null) {
                    newRow.setStatus(row.getStatus());
                }
                newRows.add(newRow);
            }
        }

        for (AssetLookupValue value : current) {
            if (!handled.contains(value.getId())) {
                value.setStatus(0);
            }
        }

        List<AssetLookupValue> allRows = new ArrayList<>(current);
        allRows.addAll(newRows);
        for (AssetLookupValue value : allRows) {
            value.setTypeCode(typeCode);
        }
        validateAllRows(allRows);

        lookupValueRepository.saveAll(allRows);
        return getByType(typeId);
    }

    private AssetLookupType findType(Long typeId) {
        return lookupTypeRepository.findById(typeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Lookup Type not found with id: " + typeId));
    }

    private void validateRows(List<AssetLookupValueRowDto> rows) {
        Set<String> values = new HashSet<>();
        Set<String> valueCodes = new HashSet<>();
        for (AssetLookupValueRowDto row : rows) {
            StatusUtil.validate(row.getStatus());
            String value = cleanValue(row.getValue());
            String valueCode = cleanCode(
                    row.getValueCode(), "Value code");
            if (!values.add(value.toLowerCase(Locale.ROOT))) {
                throw new DuplicateResourceException(
                        "Duplicate value in request: " + value);
            }
            if (valueCode != null
                    && !valueCodes.add(valueCode.toLowerCase(Locale.ROOT))) {
                throw new DuplicateResourceException(
                        "Duplicate value in request: " + valueCode);
            }
            if (row.getSortOrder() != null
                    && row.getSortOrder().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException(
                        "Sort order must be greater than 0");
            }
            row.setValue(value);
            row.setValueCode(valueCode);
        }
    }

    private void validateAllRows(List<AssetLookupValue> rows) {
        Set<String> values = new HashSet<>();
        Set<String> valueCodes = new HashSet<>();
        for (AssetLookupValue row : rows) {
            String valueKey = row.getValue().toLowerCase(Locale.ROOT);
            if (!values.add(valueKey)) {
                throw new DuplicateResourceException(
                        "Lookup Value already exists in this Type: "
                                + row.getValue());
            }
            if (row.getValueCode() != null
                    && !valueCodes.add(row.getValueCode()
                            .toLowerCase(Locale.ROOT))) {
                throw new DuplicateResourceException(
                        "Value code already exists in this Type: "
                                + row.getValueCode());
            }
        }
    }

    private String cleanValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new BadRequestException("Value cannot be empty");
        }
        String trimmed = value.trim();
        if (trimmed.length() < 2) {
            throw new BadRequestException(
                    "Value must be at least 2 characters");
        }
        if (trimmed.length() > 20) {
            throw new BadRequestException(
                    "Value cannot exceed 20 characters");
        }
        return trimmed;
    }

    private String cleanCode(String value, String label) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() > 10) {
            throw new BadRequestException(
                    label + " cannot exceed 10 characters");
        }
        return trimmed;
    }
}
