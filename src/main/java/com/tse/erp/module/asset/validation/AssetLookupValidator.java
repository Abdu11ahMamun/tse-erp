package com.tse.erp.module.asset.validation;

import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.asset.entity.AssetLookupType;
import com.tse.erp.module.asset.entity.AssetLookupValue;
import com.tse.erp.module.asset.repository.AssetLookupTypeRepository;
import com.tse.erp.module.asset.repository.AssetLookupValueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AssetLookupValidator {

    private final AssetLookupValueRepository lookupValueRepository;
    private final AssetLookupTypeRepository lookupTypeRepository;

    public void validate(
            Long valueId, Long buId, String typeName, String label) {
        if (valueId == null) {
            throw new BadRequestException(label + " cannot be empty");
        }

        AssetLookupValue value = lookupValueRepository.findById(valueId)
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
            AssetLookupType type = findType(buId, typeName);
            if (!type.getId().equals(value.getTypeId())) {
                throw new BadRequestException(
                        label + " is not a valid " + typeName + " value");
            }
        }
    }

    public Long findDefault(Long buId, String typeName, String valueName) {
        AssetLookupType type = findType(buId, typeName);
        List<AssetLookupValue> values = lookupValueRepository
                .findByTypeIdAndValueIgnoreCase(type.getId(), valueName);

        if (values.isEmpty()) {
            throw new BadRequestException(
                    "Default value '" + valueName
                            + "' not configured under " + typeName);
        }
        return values.get(0).getId();
    }

    private AssetLookupType findType(Long buId, String typeName) {
        List<AssetLookupType> types = lookupTypeRepository
                .findByBuIdAndTypeNameIgnoreCase(buId, typeName);
        if (types.isEmpty()) {
            throw new BadRequestException(
                    "Lookup Type '" + typeName
                            + "' is not configured for this Business Unit");
        }
        return types.get(0);
    }
}
