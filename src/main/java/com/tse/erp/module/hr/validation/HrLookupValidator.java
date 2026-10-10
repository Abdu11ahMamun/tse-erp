package com.tse.erp.module.hr.validation;

import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.hr.entity.HrLookupType;
import com.tse.erp.module.hr.entity.HrLookupValue;
import com.tse.erp.module.hr.repository.HrLookupTypeRepository;
import com.tse.erp.module.hr.repository.HrLookupValueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class HrLookupValidator {

    private final HrLookupValueRepository hrLookupValueRepository;
    private final HrLookupTypeRepository hrLookupTypeRepository;

    public void validate(
            Long valueId, Long bgId, String typeName, String label) {
        if (valueId == null) {
            throw new BadRequestException(label + " cannot be empty");
        }

        HrLookupValue value = hrLookupValueRepository.findById(valueId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        label + " not found with id: " + valueId));

        if (!Integer.valueOf(1).equals(value.getStatus())) {
            throw new BadRequestException(
                    "Selected " + label + " is not active");
        }
        if (!bgId.equals(value.getBgId())) {
            throw new BadRequestException(
                    label + " does not belong to selected Business Group");
        }

        List<HrLookupType> types = hrLookupTypeRepository
                .findByTypeNameIgnoreCase(typeName);
        if (types.isEmpty()) {
            throw new BadRequestException(
                    "Lookup Type '" + typeName + "' is not configured");
        }
        if (!types.get(0).getId().equals(value.getTypeId())) {
            throw new BadRequestException(
                    label + " is not a valid " + typeName + " value");
        }
    }
}
