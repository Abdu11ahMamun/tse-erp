package com.tse.erp.module.hr.service.impl;

import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.hr.entity.HrLookupType;
import com.tse.erp.module.hr.repository.HrLookupTypeRepository;
import com.tse.erp.module.hr.repository.HrLookupValueRepository;
import com.tse.erp.module.hr.service.HrLookupTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HrLookupTypeServiceImpl implements HrLookupTypeService {

    private final HrLookupTypeRepository hrLookupTypeRepository;
    private final HrLookupValueRepository hrLookupValueRepository;

    @Override
    public List<HrLookupType> getAllHrLookupTypes() {
        return hrLookupTypeRepository.findAllByOrderByIdDesc();
    }

    @Override
    public HrLookupType getHrLookupTypeById(Long id) {
        return hrLookupTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Lookup Type not found with id: " + id));
    }

    @Override
    public HrLookupType createHrLookupType(HrLookupType hrLookupType) {
        validateTypeName(hrLookupType.getTypeName());
        validateCode(hrLookupType.getCode());
        StatusUtil.validate(hrLookupType.getStatus());

        String typeName = hrLookupType.getTypeName().trim();
        String code = normalizeCode(hrLookupType.getCode());

        boolean typeNameExists = !hrLookupTypeRepository
                .findByTypeNameIgnoreCase(typeName)
                .isEmpty();
        if (typeNameExists) {
            throw new DuplicateResourceException(
                    "Lookup Type already exists: " + typeName);
        }

        if (code != null) {
            boolean codeExists = !hrLookupTypeRepository
                    .findByCodeIgnoreCase(code)
                    .isEmpty();
            if (codeExists) {
                throw new DuplicateResourceException(
                        "Code already exists: " + code);
            }
        }

        hrLookupType.setId(null);
        hrLookupType.setTypeName(typeName);
        hrLookupType.setCode(code);

        return hrLookupTypeRepository.save(hrLookupType);
    }

    @Override
    public HrLookupType updateHrLookupType(
            Long id, HrLookupType hrLookupType) {
        HrLookupType existing = getHrLookupTypeById(id);

        validateTypeName(hrLookupType.getTypeName());
        validateCode(hrLookupType.getCode());
        StatusUtil.validate(hrLookupType.getStatus());

        String typeName = hrLookupType.getTypeName().trim();
        String code = normalizeCode(hrLookupType.getCode());

        boolean typeNameDuplicate = hrLookupTypeRepository
                .findByTypeNameIgnoreCase(typeName)
                .stream()
                .anyMatch(x -> !x.getId().equals(id));
        if (typeNameDuplicate) {
            throw new DuplicateResourceException(
                    "Lookup Type already exists: " + typeName);
        }

        if (code != null) {
            boolean codeDuplicate = hrLookupTypeRepository
                    .findByCodeIgnoreCase(code)
                    .stream()
                    .anyMatch(x -> !x.getId().equals(id));
            if (codeDuplicate) {
                throw new DuplicateResourceException(
                        "Code already exists: " + code);
            }
        }

        existing.setTypeName(typeName);
        existing.setCode(code);
        if (hrLookupType.getStatus() != null) {
            existing.setStatus(hrLookupType.getStatus());
        }

        return hrLookupTypeRepository.save(existing);
    }

    @Override
    public void deleteHrLookupType(Long id) {
        HrLookupType existing = getHrLookupTypeById(id);

        if (hrLookupValueRepository.existsByTypeId(id)) {
            throw new BadRequestException(
                    "Cannot delete: values exist under this Lookup Type");
        }

        hrLookupTypeRepository.delete(existing);
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

    private void validateCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return;
        }
        if (code.trim().length() < 2) {
            throw new BadRequestException(
                    "Code must be at least 2 characters");
        }
        if (code.trim().length() > 10) {
            throw new BadRequestException(
                    "Code cannot exceed 10 characters");
        }
    }

    private String normalizeCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        return code.trim();
    }
}
