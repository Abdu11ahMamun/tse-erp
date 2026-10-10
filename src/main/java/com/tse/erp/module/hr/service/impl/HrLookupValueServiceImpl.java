package com.tse.erp.module.hr.service.impl;

import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.hr.entity.HrLookupType;
import com.tse.erp.module.hr.entity.HrLookupValue;
import com.tse.erp.module.hr.repository.HrLookupTypeRepository;
import com.tse.erp.module.hr.repository.HrLookupValueRepository;
import com.tse.erp.module.hr.service.HrLookupValueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HrLookupValueServiceImpl
        implements HrLookupValueService {

    private final HrLookupValueRepository hrLookupValueRepository;
    private final HrLookupTypeRepository hrLookupTypeRepository;
    private final BusinessGroupRepository businessGroupRepository;

    @Override
    public List<HrLookupValue> getAllHrLookupValues(
            Long bgId, Long typeId) {
        if (bgId != null && typeId != null) {
            return hrLookupValueRepository
                    .findByBgIdAndTypeIdOrderBySortOrderAscIdAsc(
                            bgId, typeId);
        }
        if (typeId != null) {
            return hrLookupValueRepository
                    .findByTypeIdOrderBySortOrderAscIdAsc(typeId);
        }
        if (bgId != null) {
            return hrLookupValueRepository.findByBgIdOrderByIdDesc(bgId);
        }
        return hrLookupValueRepository.findAllByOrderByIdDesc();
    }

    @Override
    public HrLookupValue getHrLookupValueById(Long id) {
        return hrLookupValueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "HR Lookup Value not found with id: " + id));
    }

    @Override
    public HrLookupValue createHrLookupValue(
            HrLookupValue hrLookupValue) {
        validateGroup(hrLookupValue.getBgId());
        validateType(hrLookupValue.getTypeId());
        validateValue(hrLookupValue.getValue());
        validateSortOrder(hrLookupValue.getSortOrder());
        StatusUtil.validate(hrLookupValue.getStatus());

        String value = hrLookupValue.getValue().trim();

        boolean exists = !hrLookupValueRepository
                .findByBgIdAndTypeIdAndValueIgnoreCase(
                        hrLookupValue.getBgId(),
                        hrLookupValue.getTypeId(),
                        value)
                .isEmpty();
        if (exists) {
            throw new DuplicateResourceException(
                    "Lookup Value already exists: " + value);
        }

        hrLookupValue.setId(null);
        hrLookupValue.setValue(value);
        return hrLookupValueRepository.save(hrLookupValue);
    }

    @Override
    public HrLookupValue updateHrLookupValue(
            Long id, HrLookupValue hrLookupValue) {
        HrLookupValue existing = getHrLookupValueById(id);

        validateGroup(hrLookupValue.getBgId());
        validateType(hrLookupValue.getTypeId());
        validateValue(hrLookupValue.getValue());
        validateSortOrder(hrLookupValue.getSortOrder());
        StatusUtil.validate(hrLookupValue.getStatus());

        String value = hrLookupValue.getValue().trim();

        boolean duplicateExists = hrLookupValueRepository
                .findByBgIdAndTypeIdAndValueIgnoreCase(
                        hrLookupValue.getBgId(),
                        hrLookupValue.getTypeId(),
                        value)
                .stream()
                .anyMatch(x -> !x.getId().equals(id));
        if (duplicateExists) {
            throw new DuplicateResourceException(
                    "Lookup Value already exists: " + value);
        }

        existing.setBgId(hrLookupValue.getBgId());
        existing.setTypeId(hrLookupValue.getTypeId());
        existing.setValue(value);
        existing.setSortOrder(hrLookupValue.getSortOrder());
        if (hrLookupValue.getStatus() != null) {
            existing.setStatus(hrLookupValue.getStatus());
        }

        return hrLookupValueRepository.save(existing);
    }

    @Override
    public void deleteHrLookupValue(Long id) {
        HrLookupValue existing = getHrLookupValueById(id);

        // TODO: block delete when Employee records use this value.
        hrLookupValueRepository.delete(existing);
    }

    private void validateGroup(Long bgId) {
        if (bgId == null) {
            throw new BadRequestException(
                    "Business Group id cannot be empty");
        }
        businessGroupRepository.findById(bgId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Business Group not found with id: " + bgId));
    }

    private void validateType(Long typeId) {
        if (typeId == null) {
            throw new BadRequestException("Type cannot be empty");
        }
        HrLookupType type = hrLookupTypeRepository.findById(typeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Lookup Type not found with id: " + typeId));
        if (!Integer.valueOf(1).equals(type.getStatus())) {
            throw new BadRequestException(
                    "Selected Lookup Type is not active");
        }
    }

    private void validateValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new BadRequestException("Value cannot be empty");
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

    private void validateSortOrder(BigDecimal sortOrder) {
        if (sortOrder != null
                && sortOrder.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(
                    "Sort order must be greater than 0");
        }
    }
}
