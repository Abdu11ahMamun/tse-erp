package com.tse.erp.module.common.service.impl;

import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.common.entity.LookupType;
import com.tse.erp.module.common.repository.LookupTypeRepository;
import com.tse.erp.module.common.repository.LookupValueRepository;
import com.tse.erp.module.common.service.LookupTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LookupTypeServiceImpl
        implements LookupTypeService {

    private final LookupTypeRepository lookupTypeRepository;
    private final LookupValueRepository lookupValueRepository;
    private final BusinessGroupRepository businessGroupRepository;
    private final BusinessUnitRepository businessUnitRepository;

    @Override
    public List<LookupType> getAllLookupTypes(Long buId) {
        if (buId == null) {
            return lookupTypeRepository.findAllByOrderByIdDesc();
        }
        return lookupTypeRepository.findByBuIdOrderByIdDesc(buId);
    }

    @Override
    public LookupType getLookupTypeById(Long id) {
        return lookupTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Lookup Type not found with id: " + id));
    }

    @Override
    public LookupType createLookupType(LookupType lookupType) {

        validateParents(lookupType.getBgId(), lookupType.getBuId());
        validateTypeName(lookupType.getTypeName());
        validateTypeCode(lookupType.getTypeCode());
        StatusUtil.validate(lookupType.getStatus());

        String name = lookupType.getTypeName().trim();
        String code = normalizeCode(lookupType.getTypeCode());

        // Duplicate check
        boolean nameExists = !lookupTypeRepository
                .findByBuIdAndTypeNameIgnoreCase(
                        lookupType.getBuId(), name)
                .isEmpty();
        if (nameExists) {
            throw new DuplicateResourceException(
                    "Lookup Type already exists: " + name);
        }

        if (code != null) {
            boolean codeExists = !lookupTypeRepository
                    .findByBuIdAndTypeCodeIgnoreCase(
                            lookupType.getBuId(), code)
                    .isEmpty();
            if (codeExists) {
                throw new DuplicateResourceException(
                        "Lookup Type code already exists: " + code);
            }
        }

        lookupType.setId(null);
        lookupType.setTypeName(name);
        lookupType.setTypeCode(code);

        // Status default 1 — BaseEntity @PrePersist handle korbe
        return lookupTypeRepository.save(lookupType);
    }

    @Override
    public LookupType updateLookupType(
            Long id, LookupType lookupType) {

        LookupType existing = getLookupTypeById(id);

        validateParents(lookupType.getBgId(), lookupType.getBuId());
        validateTypeName(lookupType.getTypeName());
        validateTypeCode(lookupType.getTypeCode());
        StatusUtil.validate(lookupType.getStatus());

        String name = lookupType.getTypeName().trim();
        String code = normalizeCode(lookupType.getTypeCode());

        // Duplicate check — nijer id bade
        boolean nameDuplicate = lookupTypeRepository
                .findByBuIdAndTypeNameIgnoreCase(
                        lookupType.getBuId(), name)
                .stream()
                .anyMatch(x -> !x.getId().equals(id));
        if (nameDuplicate) {
            throw new DuplicateResourceException(
                    "Lookup Type already exists: " + name);
        }

        if (code != null) {
            boolean codeDuplicate = lookupTypeRepository
                    .findByBuIdAndTypeCodeIgnoreCase(
                            lookupType.getBuId(), code)
                    .stream()
                    .anyMatch(x -> !x.getId().equals(id));
            if (codeDuplicate) {
                throw new DuplicateResourceException(
                        "Lookup Type code already exists: " + code);
            }
        }

        existing.setBgId(lookupType.getBgId());
        existing.setBuId(lookupType.getBuId());
        existing.setTypeName(name);
        existing.setTypeCode(code);
        if (lookupType.getStatus() != null) {
            existing.setStatus(lookupType.getStatus());
        }

        return lookupTypeRepository.save(existing);
    }

    @Override
    public void deleteLookupType(Long id) {
        LookupType existing = getLookupTypeById(id);

        if (lookupValueRepository.existsByTypeId(id)) {
            throw new BadRequestException(
                    "Cannot delete: values exist under this Lookup Type");
        }

        lookupTypeRepository.delete(existing);
    }

    // =========================================
    // VALIDATION HELPERS
    // =========================================
    private void validateParents(Long bgId, Long buId) {
        if (bgId == null) {
            throw new BadRequestException(
                    "Business Group id cannot be empty");
        }
        if (buId == null) {
            throw new BadRequestException(
                    "Business Unit id cannot be empty");
        }

        businessGroupRepository.findById(bgId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Business Group not found with id: " + bgId));

        BusinessUnit bu = businessUnitRepository.findById(buId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Business Unit not found with id: " + buId));

        if (!bgId.equals(bu.getBgId())) {
            throw new BadRequestException(
                    "Business Unit does not belong to selected Business Group");
        }
    }

    private void validateTypeName(String typeName) {
        if (typeName == null || typeName.trim().isEmpty()) {
            throw new BadRequestException(
                    "Type name cannot be empty");
        }
        if (typeName.trim().length() < 2) {
            throw new BadRequestException(
                    "Type name must be at least 2 characters");
        }
        if (typeName.trim().length() > 50) {
            throw new BadRequestException(
                    "Type name cannot exceed 50 characters");
        }
    }

    // Code optional — blank hole skip
    private void validateTypeCode(String typeCode) {
        if (typeCode == null || typeCode.trim().isEmpty()) {
            return;
        }
        if (typeCode.trim().length() < 2) {
            throw new BadRequestException(
                    "Code must be at least 2 characters");
        }
        if (typeCode.trim().length() > 10) {
            throw new BadRequestException(
                    "Code cannot exceed 10 characters");
        }
    }

    private String normalizeCode(String typeCode) {
        if (typeCode == null || typeCode.trim().isEmpty()) {
            return null;
        }
        return typeCode.trim();
    }
}