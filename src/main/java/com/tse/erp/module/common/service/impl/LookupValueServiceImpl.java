package com.tse.erp.module.common.service.impl;

import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.common.entity.LookupType;
import com.tse.erp.module.common.entity.LookupValue;
import com.tse.erp.module.common.repository.LookupTypeRepository;
import com.tse.erp.module.common.repository.LookupValueRepository;
import com.tse.erp.module.common.service.LookupValueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LookupValueServiceImpl
        implements LookupValueService {

    private final LookupValueRepository lookupValueRepository;
    private final LookupTypeRepository lookupTypeRepository;
    private final BusinessGroupRepository businessGroupRepository;
    private final BusinessUnitRepository businessUnitRepository;

    @Override
    public List<LookupValue> getAllLookupValues(
            Long buId, Long typeId) {
        if (buId != null && typeId != null) {
            return lookupValueRepository
                    .findByBuIdAndTypeIdOrderBySortOrderAscIdAsc(
                            buId, typeId);
        }
        if (typeId != null) {
            return lookupValueRepository
                    .findByTypeIdOrderBySortOrderAscIdAsc(typeId);
        }
        if (buId != null) {
            return lookupValueRepository
                    .findByBuIdOrderByIdDesc(buId);
        }
        return lookupValueRepository.findAllByOrderByIdDesc();
    }

    @Override
    public LookupValue getLookupValueById(Long id) {
        return lookupValueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Lookup Value not found with id: " + id));
    }

    @Override
    public LookupValue createLookupValue(LookupValue lookupValue) {

        validateParents(lookupValue);
        validateValue(lookupValue.getValue());
        validateSortOrder(lookupValue.getSortOrder());
        StatusUtil.validate(lookupValue.getStatus());

        String value = lookupValue.getValue().trim();

        // Duplicate check
        boolean exists = !lookupValueRepository
                .findByTypeIdAndValueIgnoreCase(
                        lookupValue.getTypeId(), value)
                .isEmpty();
        if (exists) {
            throw new DuplicateResourceException(
                    "Lookup Value already exists: " + value);
        }

        lookupValue.setId(null);
        lookupValue.setValue(value);

        // Status default 1 — BaseEntity @PrePersist handle korbe
        return lookupValueRepository.save(lookupValue);
    }

    @Override
    public LookupValue updateLookupValue(
            Long id, LookupValue lookupValue) {

        LookupValue existing = getLookupValueById(id);

        validateParents(lookupValue);
        validateValue(lookupValue.getValue());
        validateSortOrder(lookupValue.getSortOrder());
        StatusUtil.validate(lookupValue.getStatus());

        String value = lookupValue.getValue().trim();

        // Duplicate check — nijer id bade
        boolean duplicateExists = lookupValueRepository
                .findByTypeIdAndValueIgnoreCase(
                        lookupValue.getTypeId(), value)
                .stream()
                .anyMatch(x -> !x.getId().equals(id));
        if (duplicateExists) {
            throw new DuplicateResourceException(
                    "Lookup Value already exists: " + value);
        }

        existing.setBgId(lookupValue.getBgId());
        existing.setBuId(lookupValue.getBuId());
        existing.setTypeId(lookupValue.getTypeId());
        existing.setValue(value);
        existing.setSortOrder(lookupValue.getSortOrder());
        if (lookupValue.getStatus() != null) {
            existing.setStatus(lookupValue.getStatus());
        }

        return lookupValueRepository.save(existing);
    }

    @Override
    public void deleteLookupValue(Long id) {
        LookupValue existing = getLookupValueById(id);
        lookupValueRepository.delete(existing);
    }

    // =========================================
    // VALIDATION HELPERS
    // =========================================
    private void validateParents(LookupValue lv) {
        if (lv.getBgId() == null) {
            throw new BadRequestException(
                    "Business Group id cannot be empty");
        }
        if (lv.getBuId() == null) {
            throw new BadRequestException(
                    "Business Unit id cannot be empty");
        }
        if (lv.getTypeId() == null) {
            throw new BadRequestException(
                    "Type cannot be empty");
        }

        businessGroupRepository.findById(lv.getBgId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Business Group not found with id: "
                                + lv.getBgId()));

        BusinessUnit bu = businessUnitRepository
                .findById(lv.getBuId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Business Unit not found with id: "
                                + lv.getBuId()));

        if (!lv.getBgId().equals(bu.getBgId())) {
            throw new BadRequestException(
                    "Business Unit does not belong to selected Business Group");
        }

        LookupType type = lookupTypeRepository
                .findById(lv.getTypeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Lookup Type not found with id: "
                                + lv.getTypeId()));

        if (!lv.getBuId().equals(type.getBuId())) {
            throw new BadRequestException(
                    "Lookup Type does not belong to selected Business Unit");
        }
    }

    private void validateValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new BadRequestException(
                    "Value cannot be empty");
        }
        if (value.trim().length() < 2) {
            throw new BadRequestException(
                    "Value must be at least 2 characters");
        }
        if (value.trim().length() > 20) {
            throw new BadRequestException(
                    "Value cannot exceed 20 characters");
        }
    }

    // Sort order optional — thakle >= 1
    private void validateSortOrder(Integer sortOrder) {
        if (sortOrder != null && sortOrder < 1) {
            throw new BadRequestException(
                    "Sort order must be greater than 0");
        }
    }
}