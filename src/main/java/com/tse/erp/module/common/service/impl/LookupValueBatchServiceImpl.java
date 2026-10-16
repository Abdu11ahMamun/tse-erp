package com.tse.erp.module.common.service.impl;

import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.common.dto.LookupValueBatchDto;
import com.tse.erp.module.common.dto.LookupValueRowDto;
import com.tse.erp.module.common.entity.LookupType;
import com.tse.erp.module.common.entity.LookupValue;
import com.tse.erp.module.common.repository.LookupTypeRepository;
import com.tse.erp.module.common.repository.LookupValueRepository;
import com.tse.erp.module.common.service.LookupValueBatchService;
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
public class LookupValueBatchServiceImpl
        implements LookupValueBatchService {

    private final LookupValueRepository lookupValueRepository;
    private final LookupTypeRepository lookupTypeRepository;
    private final BusinessGroupRepository businessGroupRepository;
    private final BusinessUnitRepository businessUnitRepository;

    @Override
    public LookupValueBatchDto getByType(Long typeId) {
        LookupType type = findType(typeId);
        List<LookupValueRowDto> rows = new ArrayList<>();
        for (LookupValue value
                : lookupValueRepository.findByTypeIdOrderByIdAsc(typeId)) {
            LookupValueRowDto row = new LookupValueRowDto();
            row.setId(value.getId());
            row.setValue(value.getValue());
            row.setValueCode(value.getValueCode());
            row.setSortOrder(value.getSortOrder());
            row.setStatus(value.getStatus());
            rows.add(row);
        }

        LookupValueBatchDto result = new LookupValueBatchDto();
        result.setBgId(type.getBgId());
        result.setBuId(type.getBuId());
        result.setTypeId(type.getId());
        result.setTypeCode(type.getTypeCode());
        result.setValues(rows);
        return result;
    }

    @Override
    @Transactional
    public LookupValueBatchDto saveByType(
            Long typeId, LookupValueBatchDto dto) {
        LookupType type = findType(typeId);
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

        List<LookupValueRowDto> rows = dto.getValues() == null
                ? Collections.emptyList()
                : dto.getValues();
        validateRows(rows);

        List<LookupValue> current =
                lookupValueRepository.findByTypeIdOrderByIdAsc(typeId);
        Map<Long, LookupValue> byId = new HashMap<>();
        current.forEach(value -> byId.put(value.getId(), value));
        Set<Long> handled = new HashSet<>();
        List<LookupValue> newRows = new ArrayList<>();

        for (LookupValueRowDto row : rows) {
            if (row.getId() != null) {
                LookupValue existing = byId.get(row.getId());
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
                LookupValue newRow = new LookupValue();
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

        for (LookupValue value : current) {
            if (!handled.contains(value.getId())) {
                value.setStatus(0);
            }
        }

        List<LookupValue> allRows = new ArrayList<>(current);
        allRows.addAll(newRows);
        for (LookupValue value : allRows) {
            value.setTypeCode(typeCode);
        }
        validateAllRows(allRows);

        lookupValueRepository.saveAll(allRows);
        return getByType(typeId);
    }

    private LookupType findType(Long typeId) {
        return lookupTypeRepository.findById(typeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Lookup Type not found with id: " + typeId));
    }

    private void validateRows(List<LookupValueRowDto> rows) {
        Set<String> values = new HashSet<>();
        Set<String> valueCodes = new HashSet<>();
        for (LookupValueRowDto row : rows) {
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

    private void validateAllRows(List<LookupValue> rows) {
        Set<String> values = new HashSet<>();
        Set<String> valueCodes = new HashSet<>();
        for (LookupValue row : rows) {
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
