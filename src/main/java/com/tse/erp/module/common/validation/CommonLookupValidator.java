package com.tse.erp.module.common.validation;

import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.common.entity.LookupType;
import com.tse.erp.module.common.entity.LookupValue;
import com.tse.erp.module.common.repository.LookupTypeRepository;
import com.tse.erp.module.common.repository.LookupValueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CommonLookupValidator {

    private final LookupValueRepository lookupValueRepository;
    private final LookupTypeRepository lookupTypeRepository;

    public void validate(
            Long valueId, Long buId, String typeName, String label) {
        if (valueId == null) {
            throw new BadRequestException(label + " cannot be empty");
        }

        LookupValue value = lookupValueRepository.findById(valueId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        label + " not found with id: " + valueId));

        if (!Integer.valueOf(1).equals(value.getStatus())) {
            throw new BadRequestException(
                    "Selected " + label + " is not active");
        }
        if (!buId.equals(value.getBuId())) {
            throw new BadRequestException(
                    label + " does not belong to selected Business Unit");
        }

        if (typeName != null) {
            LookupType type = findType(buId, typeName);
            if (!type.getId().equals(value.getTypeId())) {
                throw new BadRequestException(
                        label + " is not a valid " + typeName + " value");
            }
        }
    }

    public Long findDefault(Long buId, String typeName, String valueName) {
        LookupType type = findType(buId, typeName);
        List<LookupValue> values = lookupValueRepository
                .findByTypeIdAndValueIgnoreCase(type.getId(), valueName);

        if (values.isEmpty()) {
            throw new BadRequestException(
                    "Default value '" + valueName
                            + "' not configured under " + typeName);
        }
        return values.get(0).getId();
    }

    private LookupType findType(Long buId, String typeName) {
        List<LookupType> types = lookupTypeRepository
                .findByBuIdAndTypeNameIgnoreCase(buId, typeName);
        if (types.isEmpty()) {
            throw new BadRequestException(
                    "Lookup Type '" + typeName
                            + "' is not configured for this Business Unit");
        }
        return types.get(0);
    }
}
