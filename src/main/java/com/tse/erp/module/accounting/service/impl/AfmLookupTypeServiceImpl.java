package com.tse.erp.module.accounting.service.impl;

import com.tse.erp.common.AppConstants;
import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.accounting.dto.AfmLookupTypeRequestDto;
import com.tse.erp.module.accounting.dto.AfmLookupTypeResponseDto;
import com.tse.erp.module.accounting.dto.AfmLookupValueDto;
import com.tse.erp.module.accounting.entity.AfmLookupType;
import com.tse.erp.module.accounting.entity.AfmLookupValue;
import com.tse.erp.module.accounting.repository.AfmLookupTypeRepository;
import com.tse.erp.module.accounting.repository.AfmLookupValueRepository;
import com.tse.erp.module.accounting.repository.AfmPTypePThroughMapRepository;
import com.tse.erp.module.accounting.repository.AfmVTypePTypeMapRepository;
import com.tse.erp.module.accounting.service.AfmLookupTypeService;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AfmLookupTypeServiceImpl implements AfmLookupTypeService {

    private final AfmLookupTypeRepository lookupTypeRepository;
    private final AfmLookupValueRepository lookupValueRepository;
    private final AfmPTypePThroughMapRepository pTypePThroughMapRepository;
    private final AfmVTypePTypeMapRepository vTypePTypeMapRepository;
    private final BusinessGroupRepository businessGroupRepository;
    private final BusinessUnitRepository businessUnitRepository;

    @Override
    public List<AfmLookupTypeResponseDto> getAllLookupTypes(Long bgId, Long buId, Integer status) {
        List<AfmLookupType> types;

        if (bgId != null && buId != null && status != null) {
            types = lookupTypeRepository.findByBgIdAndBuIdAndStatusOrderByIdDesc(bgId, buId, status);
        } else if (bgId != null && buId != null) {
            types = lookupTypeRepository.findByBgIdAndBuIdOrderByIdDesc(bgId, buId);
        } else if (buId != null && status != null) {
            types = lookupTypeRepository.findByBuIdAndStatusOrderByIdDesc(buId, status);
        } else if (buId != null) {
            types = lookupTypeRepository.findByBuIdOrderByIdDesc(buId);
        } else if (bgId != null) {
            types = lookupTypeRepository.findByBgIdOrderByIdDesc(bgId);
        } else {
            types = lookupTypeRepository.findAllByOrderByIdDesc();
        }

        if (status != null && (bgId != null && buId == null || bgId == null && buId == null)) {
            types = types.stream().filter(t -> status.equals(t.getStatus())).toList();
        }

        return types.stream().map(this::toResponse).toList();
    }

    @Override
    public AfmLookupTypeResponseDto getLookupTypeById(Long id) {
        return toResponse(findLookupType(id));
    }

    @Override
    public AfmLookupTypeResponseDto getLookupTypeByCode(Long buId, String typeCode) {
        return getLookupTypeByCode(typeCode, buId);
    }

    @Override
    public AfmLookupTypeResponseDto getLookupTypeByCode(String typeCode, Long buId) {
        if (typeCode == null || typeCode.trim().isEmpty()) {
            throw new BadRequestException("Type code cannot be empty");
        }
        String trimmed = typeCode.trim();
        AfmLookupType type;
        if (buId != null) {
            type = lookupTypeRepository.findFirstByBuIdAndTypeCodeIgnoreCase(buId, trimmed)
                    .orElseGet(() -> lookupTypeRepository.findByBuIdAndTypeNameIgnoreCase(buId, trimmed).stream().findFirst()
                            .orElseThrow(() -> new ResourceNotFoundException("Lookup Type not found with code/name: " + trimmed + " for Business Unit: " + buId)));
        } else {
            type = lookupTypeRepository.findFirstByTypeCodeIgnoreCase(trimmed)
                    .orElseThrow(() -> new ResourceNotFoundException("Lookup Type not found with code: " + trimmed));
        }
        return toResponse(type);
    }

    @Override
    public List<AfmLookupValueDto> getLookupValues(Long typeId) {
        findLookupType(typeId);
        List<AfmLookupValue> values = lookupValueRepository.findByTypeNameIdOrderBySortOrderAscIdAsc(typeId);
        Set<Long> referencedIds = findReferencedLookupValueIds(values);
        return values.stream()
                .map(v -> toValueDto(v, referencedIds.contains(v.getId())))
                .toList();
    }

    @Override
    public List<AfmLookupValueDto> getLookupValuesByCode(Long buId, String typeCode, Integer status) {
        return getLookupValuesByCode(typeCode, buId, status);
    }

    @Override
    public List<AfmLookupValueDto> getLookupValuesByCode(String typeCode) {
        return getLookupValuesByCode(typeCode, null, null);
    }

    @Override
    public List<AfmLookupValueDto> getLookupValuesByCode(String typeCode, Long buId, Integer status) {
        if (typeCode == null || typeCode.trim().isEmpty()) {
            throw new BadRequestException("Type code cannot be empty");
        }
        List<AfmLookupValue> values = lookupValueRepository.findValuesByTypeCode(
                typeCode.trim(),
                buId,
                status
        );
        Set<Long> referencedIds = findReferencedLookupValueIds(values);
        return values.stream()
                .map(v -> toValueDto(v, referencedIds.contains(v.getId())))
                .toList();
    }

    @Override
    @Transactional
    public AfmLookupTypeResponseDto createLookupType(AfmLookupTypeRequestDto request) {
        validateParents(request.getBgId(), request.getBuId());
        validateTypeName(request.getTypeName());
        validateTypeCode(request.getTypeCode());
        StatusUtil.validate(request.getStatus());

        String typeName = request.getTypeName().trim();
        String typeCode = request.getTypeCode() != null && !request.getTypeCode().trim().isEmpty()
                ? request.getTypeCode().trim().toUpperCase(Locale.ROOT)
                : null;

        boolean existsName = lookupTypeRepository.existsByBuIdAndTypeNameIgnoreCase(request.getBuId(), typeName);
        if (existsName) {
            throw new DuplicateResourceException("Lookup Type name already exists for this Business Unit: " + typeName);
        }

        if (typeCode != null) {
            boolean existsCode = lookupTypeRepository.existsByBuIdAndTypeCodeIgnoreCase(request.getBuId(), typeCode);
            if (existsCode) {
                throw new DuplicateResourceException("Lookup Type code already exists for this Business Unit: " + typeCode);
            }
        }

        validateDetailRows(request.getLookupValues(), null);

        AfmLookupType lookupType = AfmLookupType.builder()
                .bgId(request.getBgId())
                .buId(request.getBuId())
                .typeName(typeName)
                .typeCode(typeCode)
                .build();

        lookupType.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        lookupType.setCreatedApp(AppConstants.APP_NAME);

        AfmLookupType savedType = lookupTypeRepository.save(lookupType);

        if (request.getLookupValues() != null && !request.getLookupValues().isEmpty()) {
            List<AfmLookupValue> values = new ArrayList<>();
            for (int i = 0; i < request.getLookupValues().size(); i++) {
                AfmLookupValueDto dto = request.getLookupValues().get(i);
                AfmLookupValue entity = toValueEntity(dto, savedType.getId(), savedType.getBgId(), savedType.getBuId(), savedType.getTypeCode(), i + 1);
                entity.setCreatedApp(AppConstants.APP_NAME);
                values.add(entity);
            }
            lookupValueRepository.saveAll(values);
        }

        return toResponse(savedType);
    }

    @Override
    @Transactional
    public AfmLookupTypeResponseDto updateLookupType(Long id, AfmLookupTypeRequestDto request) {
        AfmLookupType existing = findLookupType(id);

        validateParents(request.getBgId(), request.getBuId());
        validateTypeName(request.getTypeName());
        validateTypeCode(request.getTypeCode());
        StatusUtil.validate(request.getStatus());

        String typeName = request.getTypeName().trim();
        String typeCode = request.getTypeCode() != null && !request.getTypeCode().trim().isEmpty()
                ? request.getTypeCode().trim().toUpperCase(Locale.ROOT)
                : null;

        List<AfmLookupType> nameMatches = lookupTypeRepository.findByBuIdAndTypeNameIgnoreCase(request.getBuId(), typeName);
        boolean duplicateName = nameMatches.stream().anyMatch(t -> !t.getId().equals(id));
        if (duplicateName) {
            throw new DuplicateResourceException("Lookup Type name already exists for this Business Unit: " + typeName);
        }

        if (typeCode != null) {
            List<AfmLookupType> codeMatches = lookupTypeRepository.findByBuIdAndTypeCodeIgnoreCase(request.getBuId(), typeCode);
            boolean duplicateCode = codeMatches.stream().anyMatch(t -> !t.getId().equals(id));
            if (duplicateCode) {
                throw new DuplicateResourceException("Lookup Type code already exists for this Business Unit: " + typeCode);
            }
        }

        validateDetailRows(request.getLookupValues(), id);

        existing.setBgId(request.getBgId());
        existing.setBuId(request.getBuId());
        existing.setTypeName(typeName);
        existing.setTypeCode(typeCode);
        existing.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        existing.setUpdatedApp(AppConstants.APP_NAME);

        AfmLookupType savedType = lookupTypeRepository.save(existing);

        // Synchronize Lookup Values: update existing, insert new, delete removed
        List<AfmLookupValue> currentDbValues = lookupValueRepository.findByTypeNameIdOrderBySortOrderAscIdAsc(id);
        Map<Long, AfmLookupValue> dbValueMap = new HashMap<>();
        for (AfmLookupValue v : currentDbValues) {
            if (v.getId() != null) {
                dbValueMap.put(v.getId(), v);
            }
        }

        Set<Long> incomingIds = new HashSet<>();
        List<AfmLookupValue> toSave = new ArrayList<>();

        if (request.getLookupValues() != null) {
            for (int i = 0; i < request.getLookupValues().size(); i++) {
                AfmLookupValueDto dto = request.getLookupValues().get(i);
                if (dto.getId() != null && dbValueMap.containsKey(dto.getId())) {
                    incomingIds.add(dto.getId());
                    AfmLookupValue existingVal = dbValueMap.get(dto.getId());
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
                    // New lookup value row
                    AfmLookupValue newVal = toValueEntity(dto, savedType.getId(), savedType.getBgId(), savedType.getBuId(), savedType.getTypeCode(), i + 1);
                    newVal.setId(null);
                    newVal.setCreatedApp(AppConstants.APP_NAME);
                    toSave.add(newVal);
                }
            }
        }

        // Delete only values that were removed from the form by the user
        List<AfmLookupValue> toDelete = new ArrayList<>();
        for (AfmLookupValue v : currentDbValues) {
            if (!incomingIds.contains(v.getId())) {
                ensureLookupValueNotReferenced(v);
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
        findLookupType(id);
        List<AfmLookupValue> values = lookupValueRepository.findByTypeNameIdOrderBySortOrderAscIdAsc(id);
        if (!values.isEmpty()) {
            for (AfmLookupValue v : values) {
                ensureLookupValueNotReferenced(v);
            }
            lookupValueRepository.deleteAll(values);
        }
        lookupTypeRepository.deleteById(id);
    }

    @Override
    @Transactional
    public AfmLookupValueDto addLookupValue(Long typeId, AfmLookupValueDto detailDto) {
        AfmLookupType lookupType = findLookupType(typeId);
        validateDetailRow(detailDto);

        String value = detailDto.getValue().trim();
        List<AfmLookupValue> existingValues = lookupValueRepository.findByTypeNameIdAndValueIgnoreCase(typeId, value);
        if (!existingValues.isEmpty()) {
            throw new DuplicateResourceException("Lookup value already exists for this Lookup Type: " + value);
        }

        if (detailDto.getValueCode() != null && !detailDto.getValueCode().trim().isEmpty()) {
            String valCode = detailDto.getValueCode().trim();
            List<AfmLookupValue> existingCodes = lookupValueRepository.findByTypeNameIdAndValueCodeIgnoreCase(typeId, valCode);
            if (!existingCodes.isEmpty()) {
                throw new DuplicateResourceException("Lookup value code already exists for this Lookup Type: " + valCode);
            }
        }

        int nextOrder = lookupValueRepository.findByTypeNameIdOrderBySortOrderAscIdAsc(typeId).size() + 1;
        AfmLookupValue entity = toValueEntity(detailDto, typeId, lookupType.getBgId(), lookupType.getBuId(), lookupType.getTypeCode(), nextOrder);
        entity.setCreatedApp(AppConstants.APP_NAME);

        AfmLookupValue saved = lookupValueRepository.save(entity);
        return toValueDto(saved);
    }

    @Override
    @Transactional
    public AfmLookupValueDto updateLookupValue(Long typeId, Long valueId, AfmLookupValueDto detailDto) {
        AfmLookupType lookupType = findLookupType(typeId);
        AfmLookupValue existing = lookupValueRepository.findById(valueId)
                .orElseThrow(() -> new ResourceNotFoundException("Lookup value not found with id: " + valueId));

        if (!typeId.equals(existing.getTypeNameId())) {
            throw new BadRequestException("Detail row with id " + valueId + " does not belong to Lookup Type " + typeId);
        }

        validateDetailRow(detailDto);

        String value = detailDto.getValue().trim();
        List<AfmLookupValue> matches = lookupValueRepository.findByTypeNameIdAndValueIgnoreCase(typeId, value);
        boolean duplicate = matches.stream().anyMatch(m -> !m.getId().equals(valueId));
        if (duplicate) {
            throw new DuplicateResourceException("Lookup value already exists for this Lookup Type: " + value);
        }

        if (detailDto.getValueCode() != null && !detailDto.getValueCode().trim().isEmpty()) {
            String valCode = detailDto.getValueCode().trim();
            List<AfmLookupValue> codeMatches = lookupValueRepository.findByTypeNameIdAndValueCodeIgnoreCase(typeId, valCode);
            boolean duplicateCode = codeMatches.stream().anyMatch(m -> !m.getId().equals(valueId));
            if (duplicateCode) {
                throw new DuplicateResourceException("Lookup value code already exists for this Lookup Type: " + valCode);
            }
        }

        existing.setBgId(lookupType.getBgId());
        existing.setBuId(lookupType.getBuId());
        existing.setTypeCode(lookupType.getTypeCode());
        existing.setValue(value);
        existing.setValueCode(detailDto.getValueCode() != null && !detailDto.getValueCode().trim().isEmpty() ? detailDto.getValueCode().trim() : null);
        existing.setNote(detailDto.getNote() != null && !detailDto.getNote().trim().isEmpty() ? detailDto.getNote().trim() : null);
        if (detailDto.getSortOrder() != null) {
            existing.setSortOrder(detailDto.getSortOrder());
        }
        if (detailDto.getStatus() != null) {
            existing.setStatus(detailDto.getStatus());
        }
        existing.setUpdatedApp(AppConstants.APP_NAME);

        AfmLookupValue saved = lookupValueRepository.save(existing);
        return toValueDto(saved);
    }

    @Override
    @Transactional
    public void deleteLookupValue(Long typeId, Long valueId) {
        findLookupType(typeId);
        AfmLookupValue existing = lookupValueRepository.findById(valueId)
                .orElseThrow(() -> new ResourceNotFoundException("Lookup value not found with id: " + valueId));

        if (!typeId.equals(existing.getTypeNameId())) {
            throw new BadRequestException("Detail row with id " + valueId + " does not belong to Lookup Type " + typeId);
        }

        ensureLookupValueNotReferenced(existing);

        lookupValueRepository.delete(existing);
    }

    // =========================================
    // HELPER & VALIDATION METHODS
    // =========================================

    private void ensureLookupValueNotReferenced(AfmLookupValue lookupValue) {
        if (lookupValue == null || lookupValue.getId() == null) {
            return;
        }
        Long valueId = lookupValue.getId();
        String label = lookupValue.getValue() != null ? lookupValue.getValue() : String.valueOf(valueId);

        if (pTypePThroughMapRepository.existsByPaymentTypeIdOrPaymentThroughId(valueId, valueId)) {
            throw new BadRequestException(
                    "Cannot delete Lookup Value '" + label + "' because it is already used in Payment Type to Payment Through Mapping."
            );
        }

        if (vTypePTypeMapRepository.existsByVoucherTypeIdOrPaymentTypeId(valueId, valueId)) {
            throw new BadRequestException(
                    "Cannot delete Lookup Value '" + label + "' because it is already used in Voucher Type to Payment Type Mapping."
            );
        }
    }

    private Set<Long> findReferencedLookupValueIds(List<AfmLookupValue> values) {
        if (values == null || values.isEmpty()) {
            return Collections.emptySet();
        }
        List<Long> ids = values.stream()
                .map(AfmLookupValue::getId)
                .filter(Objects::nonNull)
                .toList();
        if (ids.isEmpty()) {
            return Collections.emptySet();
        }
        Set<Long> referenced = new HashSet<>();
        referenced.addAll(pTypePThroughMapRepository.findReferencedPaymentTypeIds(ids));
        referenced.addAll(pTypePThroughMapRepository.findReferencedPaymentThroughIds(ids));
        referenced.addAll(vTypePTypeMapRepository.findReferencedVoucherTypeIds(ids));
        referenced.addAll(vTypePTypeMapRepository.findReferencedPaymentTypeIds(ids));
        return referenced;
    }

    private boolean isLookupValueReferenced(Long valueId) {
        if (valueId == null) {
            return false;
        }
        return pTypePThroughMapRepository.existsByPaymentTypeIdOrPaymentThroughId(valueId, valueId)
                || vTypePTypeMapRepository.existsByVoucherTypeIdOrPaymentTypeId(valueId, valueId);
    }

    private AfmLookupType findLookupType(Long id) {
        return lookupTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lookup Type not found with id: " + id));
    }

    private AfmLookupTypeResponseDto toResponse(AfmLookupType type) {
        List<AfmLookupValue> valueEntities = lookupValueRepository
                .findByTypeNameIdOrderBySortOrderAscIdAsc(type.getId());
        Set<Long> referencedIds = findReferencedLookupValueIds(valueEntities);
        List<AfmLookupValueDto> details = valueEntities
                .stream()
                .map(v -> toValueDto(v, referencedIds.contains(v.getId())))
                .toList();

        return AfmLookupTypeResponseDto.builder()
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
                .lookupValues(details)
                .build();
    }

    private AfmLookupValueDto toValueDto(AfmLookupValue detail) {
        return toValueDto(detail, isLookupValueReferenced(detail.getId()));
    }

    private AfmLookupValueDto toValueDto(AfmLookupValue detail, boolean inUse) {
        return AfmLookupValueDto.builder()
                .id(detail.getId())
                .bgId(detail.getBgId())
                .buId(detail.getBuId())
                .typeNameId(detail.getTypeNameId())
                .typeCode(detail.getTypeCode())
                .value(detail.getValue())
                .valueCode(detail.getValueCode())
                .note(detail.getNote())
                .sortOrder(detail.getSortOrder())
                .status(detail.getStatus())
                .inUse(inUse)
                .build();
    }

    private AfmLookupValue toValueEntity(AfmLookupValueDto dto, Long typeId, Long bgId, Long buId, String parentTypeCode, int defaultSortOrder) {
        String typeCode = dto.getTypeCode() != null && !dto.getTypeCode().trim().isEmpty()
                ? dto.getTypeCode().trim()
                : parentTypeCode;

        return AfmLookupValue.builder()
                .id(dto.getId())
                .bgId(bgId)
                .buId(buId)
                .typeNameId(typeId)
                .typeCode(typeCode)
                .value(dto.getValue() != null ? dto.getValue().trim() : null)
                .valueCode(dto.getValueCode() != null && !dto.getValueCode().trim().isEmpty() ? dto.getValueCode().trim() : null)
                .note(dto.getNote() != null && !dto.getNote().trim().isEmpty() ? dto.getNote().trim() : null)
                .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : BigDecimal.valueOf(defaultSortOrder))
                .build();
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

    private void validateTypeName(String typeName) {
        if (typeName == null || typeName.trim().isEmpty()) {
            throw new BadRequestException("Lookup Type name cannot be empty");
        }
        if (typeName.trim().length() < 2) {
            throw new BadRequestException("Lookup Type name must be at least 2 characters");
        }
        if (typeName.trim().length() > 150) {
            throw new BadRequestException("Lookup Type name cannot exceed 150 characters");
        }
    }

    private void validateTypeCode(String typeCode) {
        if (typeCode != null && typeCode.trim().length() > 50) {
            throw new BadRequestException("Type code cannot exceed 50 characters");
        }
    }

    private void validateDetailRows(List<AfmLookupValueDto> details, Long existingTypeId) {
        if (details == null || details.isEmpty()) {
            return;
        }

        Set<String> seenValues = new HashSet<>();
        Set<String> seenValueCodes = new HashSet<>();

        for (AfmLookupValueDto row : details) {
            validateDetailRow(row);

            String val = row.getValue().trim().toUpperCase(Locale.ROOT);
            if (!seenValues.add(val)) {
                throw new DuplicateResourceException("Duplicate lookup value in form: " + row.getValue());
            }

            if (row.getValueCode() != null && !row.getValueCode().trim().isEmpty()) {
                String valCode = row.getValueCode().trim().toUpperCase(Locale.ROOT);
                if (!seenValueCodes.add(valCode)) {
                    throw new DuplicateResourceException("Duplicate lookup value code in form: " + row.getValueCode());
                }
            }
        }
    }

    private void validateDetailRow(AfmLookupValueDto row) {
        if (row.getValue() == null || row.getValue().trim().isEmpty()) {
            throw new BadRequestException("Lookup value cannot be empty");
        }
        if (row.getValue().trim().length() > 150) {
            throw new BadRequestException("Lookup value cannot exceed 150 characters");
        }
        if (row.getValueCode() != null && row.getValueCode().trim().length() > 50) {
            throw new BadRequestException("Lookup value code cannot exceed 50 characters");
        }
        if (row.getNote() != null && row.getNote().trim().length() > 200) {
            throw new BadRequestException("Note cannot exceed 200 characters");
        }
        if (row.getTypeCode() != null && row.getTypeCode().trim().length() > 50) {
            throw new BadRequestException("Type code cannot exceed 50 characters");
        }
        if (row.getSortOrder() != null && row.getSortOrder().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Sort order cannot be negative");
        }
        StatusUtil.validate(row.getStatus());
    }
}
