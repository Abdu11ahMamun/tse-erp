package com.tse.erp.module.common.service.impl;

import com.tse.erp.common.AppConstants;
import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.BusinessGroup;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.common.dto.LookupTypeRequestDto;
import com.tse.erp.module.common.dto.LookupTypeResponseDto;
import com.tse.erp.module.common.dto.LookupValueRowDto;
import com.tse.erp.module.common.entity.LookupType;
import com.tse.erp.module.common.entity.LookupValue;
import com.tse.erp.module.common.repository.LookupTypeRepository;
import com.tse.erp.module.common.repository.LookupValueRepository;
import com.tse.erp.module.common.service.LookupTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class LookupTypeServiceImpl implements LookupTypeService {

    private final LookupTypeRepository lookupTypeRepository;
    private final LookupValueRepository lookupValueRepository;
    private final BusinessGroupRepository businessGroupRepository;
    private final BusinessUnitRepository businessUnitRepository;

    @Override
    public List<LookupTypeResponseDto> getAllLookupTypes(Long buId) {
        List<LookupType> types = (buId == null)
                ? lookupTypeRepository.findAllByOrderByIdDesc()
                : lookupTypeRepository.findByBuIdOrderByIdDesc(buId);

        return types.stream().map(this::toResponse).toList();
    }

    @Override
    public LookupTypeResponseDto getLookupTypeById(Long id) {
        return toResponse(findLookupTypeEntity(id));
    }

    @Override
    public List<LookupValueRowDto> getLookupValues(Long typeId) {
        findLookupTypeEntity(typeId);
        List<LookupValue> values = lookupValueRepository.findByTypeIdOrderBySortOrderAscIdAsc(typeId);
        return values.stream().map(this::toValueDto).toList();
    }

    @Override
    @Transactional
    public LookupTypeResponseDto createLookupType(LookupTypeRequestDto request) {
        validateParents(request.getBgId(), request.getBuId());
        validateTypeName(request.getTypeName());
        validateTypeCode(request.getTypeCode());
        StatusUtil.validate(request.getStatus());

        String name = request.getTypeName().trim();
        String code = normalizeCode(request.getTypeCode());

        // Duplicate check
        boolean nameExists = !lookupTypeRepository
                .findByBuIdAndTypeNameIgnoreCase(request.getBuId(), name)
                .isEmpty();
        if (nameExists) {
            throw new DuplicateResourceException("Lookup Type already exists: " + name);
        }

        if (code != null) {
            boolean codeExists = !lookupTypeRepository
                    .findByBuIdAndTypeCodeIgnoreCase(request.getBuId(), code)
                    .isEmpty();
            if (codeExists) {
                throw new DuplicateResourceException("Lookup Type code already exists: " + code);
            }
        }

        validateDetailRows(request.getLookupValues(), null);

        LookupType lookupType = LookupType.builder()
                .bgId(request.getBgId())
                .buId(request.getBuId())
                .typeName(name)
                .typeCode(code)
                .build();

        lookupType.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        lookupType.setCreatedApp(AppConstants.APP_NAME);

        LookupType savedType = lookupTypeRepository.save(lookupType);

        if (request.getLookupValues() != null && !request.getLookupValues().isEmpty()) {
            List<LookupValue> values = new ArrayList<>();
            for (int i = 0; i < request.getLookupValues().size(); i++) {
                LookupValueRowDto dto = request.getLookupValues().get(i);
                LookupValue entity = toValueEntity(dto, savedType.getId(), savedType.getBgId(), savedType.getBuId(), savedType.getTypeCode(), i + 1);
                entity.setCreatedApp(AppConstants.APP_NAME);
                values.add(entity);
            }
            lookupValueRepository.saveAll(values);
        }

        return toResponse(savedType);
    }

    @Override
    @Transactional
    public LookupTypeResponseDto updateLookupType(Long id, LookupTypeRequestDto request) {
        LookupType existing = findLookupTypeEntity(id);

        validateParents(request.getBgId(), request.getBuId());
        validateTypeName(request.getTypeName());
        validateTypeCode(request.getTypeCode());
        StatusUtil.validate(request.getStatus());

        String name = request.getTypeName().trim();
        String code = normalizeCode(request.getTypeCode());

        // Duplicate check
        boolean nameDuplicate = lookupTypeRepository
                .findByBuIdAndTypeNameIgnoreCase(request.getBuId(), name)
                .stream()
                .anyMatch(x -> !x.getId().equals(id));
        if (nameDuplicate) {
            throw new DuplicateResourceException("Lookup Type already exists: " + name);
        }

        if (code != null) {
            boolean codeDuplicate = lookupTypeRepository
                    .findByBuIdAndTypeCodeIgnoreCase(request.getBuId(), code)
                    .stream()
                    .anyMatch(x -> !x.getId().equals(id));
            if (codeDuplicate) {
                throw new DuplicateResourceException("Lookup Type code already exists: " + code);
            }
        }

        validateDetailRows(request.getLookupValues(), id);

        existing.setBgId(request.getBgId());
        existing.setBuId(request.getBuId());
        existing.setTypeName(name);
        existing.setTypeCode(code);
        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }
        existing.setUpdatedApp(AppConstants.APP_NAME);

        LookupType savedType = lookupTypeRepository.save(existing);

        // Synchronize Lookup Values: update existing, insert new, delete removed
        List<LookupValue> currentDbValues = lookupValueRepository.findByTypeIdOrderBySortOrderAscIdAsc(id);
        Map<Long, LookupValue> dbValueMap = new HashMap<>();
        for (LookupValue v : currentDbValues) {
            if (v.getId() != null) {
                dbValueMap.put(v.getId(), v);
            }
        }

        Set<Long> incomingIds = new HashSet<>();
        List<LookupValue> toSave = new ArrayList<>();

        if (request.getLookupValues() != null) {
            for (int i = 0; i < request.getLookupValues().size(); i++) {
                LookupValueRowDto dto = request.getLookupValues().get(i);
                if (dto.getId() != null && dbValueMap.containsKey(dto.getId())) {
                    incomingIds.add(dto.getId());
                    LookupValue existingVal = dbValueMap.get(dto.getId());
                    existingVal.setBgId(savedType.getBgId());
                    existingVal.setBuId(savedType.getBuId());
                    existingVal.setTypeCode(savedType.getTypeCode());
                    existingVal.setValue(dto.getValue() != null ? dto.getValue().trim() : null);
                    existingVal.setValueCode(dto.getValueCode() != null && !dto.getValueCode().trim().isEmpty()
                            ? dto.getValueCode().trim().toUpperCase(Locale.ROOT)
                            : null);
                    existingVal.setNote(dto.getNote() != null && !dto.getNote().trim().isEmpty()
                            ? dto.getNote().trim()
                            : null);
                    existingVal.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : BigDecimal.valueOf(i + 1));
                    if (dto.getStatus() != null) {
                        existingVal.setStatus(dto.getStatus());
                    }
                    existingVal.setUpdatedApp(AppConstants.APP_NAME);
                    toSave.add(existingVal);
                } else {
                    LookupValue newVal = toValueEntity(dto, savedType.getId(), savedType.getBgId(), savedType.getBuId(), savedType.getTypeCode(), i + 1);
                    newVal.setId(null);
                    newVal.setCreatedApp(AppConstants.APP_NAME);
                    toSave.add(newVal);
                }
            }
        }

        List<LookupValue> toDelete = new ArrayList<>();
        for (LookupValue v : currentDbValues) {
            if (!incomingIds.contains(v.getId())) {
                toDelete.add(v);
            }
        }

        if (!toDelete.isEmpty()) {
            lookupValueRepository.deleteAll(toDelete);
        }

        if (!toSave.isEmpty()) {
            lookupValueRepository.saveAll(toSave);
        }

        return toResponse(savedType);
    }

    @Override
    @Transactional
    public void deleteLookupType(Long id) {
        findLookupTypeEntity(id);
        List<LookupValue> values = lookupValueRepository.findByTypeIdOrderBySortOrderAscIdAsc(id);
        if (!values.isEmpty()) {
            lookupValueRepository.deleteAll(values);
        }
        lookupTypeRepository.deleteById(id);
    }

    // Backward compatible overloads
    @Override
    public LookupType createLookupType(LookupType lookupType) {
        LookupTypeRequestDto dto = LookupTypeRequestDto.builder()
                .bgId(lookupType.getBgId())
                .buId(lookupType.getBuId())
                .typeName(lookupType.getTypeName())
                .typeCode(lookupType.getTypeCode())
                .status(lookupType.getStatus())
                .build();
        LookupTypeResponseDto resp = createLookupType(dto);
        lookupType.setId(resp.getId());
        return lookupType;
    }

    @Override
    public LookupType updateLookupType(Long id, LookupType lookupType) {
        LookupTypeRequestDto dto = LookupTypeRequestDto.builder()
                .bgId(lookupType.getBgId())
                .buId(lookupType.getBuId())
                .typeName(lookupType.getTypeName())
                .typeCode(lookupType.getTypeCode())
                .status(lookupType.getStatus())
                .build();
        LookupTypeResponseDto resp = updateLookupType(id, dto);
        lookupType.setId(resp.getId());
        return lookupType;
    }

    private LookupType findLookupTypeEntity(Long id) {
        return lookupTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lookup Type not found with id: " + id));
    }

    private LookupTypeResponseDto toResponse(LookupType type) {
        List<LookupValue> values = lookupValueRepository.findByTypeIdOrderBySortOrderAscIdAsc(type.getId());
        List<LookupValueRowDto> detailDtos = values.stream().map(this::toValueDto).toList();

        String groupName = null;
        if (type.getBgId() != null) {
            groupName = businessGroupRepository.findById(type.getBgId())
                    .map(BusinessGroup::getGroupName)
                    .orElse(null);
        }

        String buName = null;
        if (type.getBuId() != null) {
            buName = businessUnitRepository.findById(type.getBuId())
                    .map(BusinessUnit::getBusinessUnit)
                    .orElse(null);
        }

        return LookupTypeResponseDto.builder()
                .id(type.getId())
                .bgId(type.getBgId())
                .buId(type.getBuId())
                .typeName(type.getTypeName())
                .typeCode(type.getTypeCode())
                .status(type.getStatus())
                .createdUserId(type.getCreatedUserId())
                .createdEmpId(type.getCreatedEmpId())
                .createdDatetime(type.getCreatedDatetime())
                .updatedUserId(type.getUpdatedUserId())
                .updatedEmpId(type.getUpdatedEmpId())
                .updatedDatetime(type.getUpdatedDatetime())
                .createdApp(type.getCreatedApp())
                .updatedApp(type.getUpdatedApp())
                .groupName(groupName)
                .businessUnitName(buName)
                .lookupValues(detailDtos)
                .build();
    }

    private LookupValueRowDto toValueDto(LookupValue val) {
        return LookupValueRowDto.builder()
                .id(val.getId())
                .bgId(val.getBgId())
                .buId(val.getBuId())
                .typeId(val.getTypeId())
                .typeCode(val.getTypeCode())
                .value(val.getValue())
                .valueCode(val.getValueCode())
                .note(val.getNote())
                .sortOrder(val.getSortOrder())
                .status(val.getStatus())
                .inUse(false)
                .build();
    }

    private LookupValue toValueEntity(LookupValueRowDto dto, Long typeId, Long bgId, Long buId, String typeCode, int defaultOrder) {
        String val = dto.getValue() != null ? dto.getValue().trim() : null;
        String valCode = dto.getValueCode() != null && !dto.getValueCode().trim().isEmpty()
                ? dto.getValueCode().trim().toUpperCase(Locale.ROOT)
                : null;
        String note = dto.getNote() != null && !dto.getNote().trim().isEmpty()
                ? dto.getNote().trim()
                : null;

        BigDecimal sortOrder = dto.getSortOrder() != null
                ? dto.getSortOrder()
                : BigDecimal.valueOf(defaultOrder);

        LookupValue entity = LookupValue.builder()
                .id(dto.getId())
                .bgId(bgId)
                .buId(buId)
                .typeId(typeId)
                .typeCode(typeCode)
                .value(val)
                .valueCode(valCode)
                .note(note)
                .sortOrder(sortOrder)
                .build();

        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        return entity;
    }

    private void validateDetailRows(List<LookupValueRowDto> rows, Long existingTypeId) {
        if (rows == null || rows.isEmpty()) {
            return;
        }

        Set<String> seenValues = new HashSet<>();
        Set<String> seenCodes = new HashSet<>();

        for (int i = 0; i < rows.size(); i++) {
            LookupValueRowDto row = rows.get(i);
            int rowNum = i + 1;

            if (row.getValue() == null || row.getValue().trim().isEmpty()) {
                throw new BadRequestException("Row " + rowNum + ": Lookup Value cannot be empty");
            }
            String val = row.getValue().trim();
            if (val.length() > 150) {
                throw new BadRequestException("Row " + rowNum + ": Lookup Value cannot exceed 150 characters");
            }
            String valLower = val.toLowerCase(Locale.ROOT);
            if (seenValues.contains(valLower)) {
                throw new BadRequestException("Row " + rowNum + ": Duplicate Lookup Value in table: " + val);
            }
            seenValues.add(valLower);

            if (row.getValueCode() != null && !row.getValueCode().trim().isEmpty()) {
                String code = row.getValueCode().trim();
                if (code.length() > 50) {
                    throw new BadRequestException("Row " + rowNum + ": Lookup Value Code cannot exceed 50 characters");
                }
                String codeLower = code.toLowerCase(Locale.ROOT);
                if (seenCodes.contains(codeLower)) {
                    throw new BadRequestException("Row " + rowNum + ": Duplicate Lookup Value Code in table: " + code);
                }
                seenCodes.add(codeLower);
            }

            if (row.getNote() != null && !row.getNote().trim().isEmpty()) {
                if (row.getNote().trim().length() > 200) {
                    throw new BadRequestException("Row " + rowNum + ": Note cannot exceed 200 characters");
                }
            }

            if (row.getSortOrder() != null && row.getSortOrder().compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException("Row " + rowNum + ": Sort Order cannot be negative");
            }

            StatusUtil.validate(row.getStatus());
        }
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

        if (!bgId.equals(bu.getBgId())) {
            throw new BadRequestException("Business Unit does not belong to selected Business Group");
        }
    }

    private void validateTypeName(String typeName) {
        if (typeName == null || typeName.trim().isEmpty()) {
            throw new BadRequestException("Type name cannot be empty");
        }
        if (typeName.trim().length() < 2) {
            throw new BadRequestException("Type name must be at least 2 characters");
        }
        if (typeName.trim().length() > 150) {
            throw new BadRequestException("Type name cannot exceed 150 characters");
        }
    }

    private void validateTypeCode(String typeCode) {
        if (typeCode == null || typeCode.trim().isEmpty()) {
            return;
        }
        if (typeCode.trim().length() < 2) {
            throw new BadRequestException("Code must be at least 2 characters");
        }
        if (typeCode.trim().length() > 50) {
            throw new BadRequestException("Code cannot exceed 50 characters");
        }
    }

    private String normalizeCode(String typeCode) {
        if (typeCode == null || typeCode.trim().isEmpty()) {
            return null;
        }
        return typeCode.trim().toUpperCase(Locale.ROOT);
    }
}
