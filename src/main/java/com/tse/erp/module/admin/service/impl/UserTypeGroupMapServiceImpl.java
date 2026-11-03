package com.tse.erp.module.admin.service.impl;

import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.entity.UserType;
import com.tse.erp.module.admin.entity.UserTypeGroup;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.admin.repository.UserTypeGroupRepository;
import com.tse.erp.module.admin.repository.UserTypeRepository;
import com.tse.erp.module.admin.service.UserTypeGroupMapService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserTypeGroupMapServiceImpl
        implements UserTypeGroupMapService {

    private final UserTypeGroupRepository userTypeGroupRepository;
    private final UserTypeRepository userTypeRepository;
    private final BusinessGroupRepository businessGroupRepository;
    private final BusinessUnitRepository businessUnitRepository;

    @Override
    public List<UserTypeGroup> getAll(Long userTypeId, Long buId) {
        List<UserTypeGroup> list;
        if (userTypeId != null && buId != null) {
            list = userTypeGroupRepository
                    .findByUserTypeIdAndBuId(userTypeId, buId);
        } else if (userTypeId != null) {
            list = userTypeGroupRepository
                    .findByUserTypeIdOrderByIdAsc(userTypeId);
        } else if (buId != null) {
            list = userTypeGroupRepository.findByBuIdOrderByIdDesc(buId);
        } else {
            list = userTypeGroupRepository.findAllByOrderByIdDesc();
        }
        attachCodes(list);
        return list;
    }

    @Override
    public UserTypeGroup getById(Long id) {
        UserTypeGroup row = userTypeGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User Type Group not found with id: " + id));
        attachCodes(List.of(row));
        return row;
    }

    @Override
    public UserTypeGroup create(UserTypeGroup row) {
        validate(row);
        StatusUtil.validate(row.getStatus());
        if (!userTypeGroupRepository.findByUserTypeIdAndBuId(
                row.getUserTypeId(), row.getBuId()).isEmpty()) {
            throw new DuplicateResourceException(
                    "This User Type is already mapped to the selected Business Unit");
        }
        row.setId(null);
        UserTypeGroup saved = userTypeGroupRepository.save(row);
        attachCodes(List.of(saved));
        return saved;
    }

    @Override
    public UserTypeGroup update(Long id, UserTypeGroup row) {
        UserTypeGroup existing = getById(id);
        validate(row);
        StatusUtil.validate(row.getStatus());
        boolean duplicateExists = userTypeGroupRepository
                .findByUserTypeIdAndBuId(
                        row.getUserTypeId(), row.getBuId())
                .stream()
                .anyMatch(x -> !x.getId().equals(id));
        if (duplicateExists) {
            throw new DuplicateResourceException(
                    "This User Type is already mapped to the selected Business Unit");
        }

        existing.setBgId(row.getBgId());
        existing.setUserTypeId(row.getUserTypeId());
        existing.setBuId(row.getBuId());
        if (row.getStatus() != null) {
            existing.setStatus(row.getStatus());
        }
        UserTypeGroup saved = userTypeGroupRepository.save(existing);
        attachCodes(List.of(saved));
        return saved;
    }

    @Override
    public void delete(Long id) {
        UserTypeGroup existing = getById(id);
        // TODO: block when Users of this User Type use this Business Unit.
        userTypeGroupRepository.delete(existing);
    }

    private void validate(UserTypeGroup row) {
        if (row.getBgId() == null) {
            throw new BadRequestException(
                    "Business Group id cannot be empty");
        }
        if (row.getUserTypeId() == null) {
            throw new BadRequestException("User Type cannot be empty");
        }
        if (row.getBuId() == null) {
            throw new BadRequestException(
                    "Business Unit id cannot be empty");
        }

        businessGroupRepository.findById(row.getBgId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Business Group not found with id: " + row.getBgId()));

        UserType userType = userTypeRepository.findById(row.getUserTypeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User Type not found with id: "
                                + row.getUserTypeId()));
        if (!Integer.valueOf(1).equals(userType.getStatus())) {
            throw new BadRequestException(
                    "Selected User Type is not active");
        }
        if (userType.getBgId() != null
                && !row.getBgId().equals(userType.getBgId())) {
            throw new BadRequestException(
                    "User Type does not belong to selected Business Group");
        }

        BusinessUnit businessUnit = businessUnitRepository
                .findById(row.getBuId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Business Unit not found with id: " + row.getBuId()));
        if (!row.getBgId().equals(businessUnit.getBgId())) {
            throw new BadRequestException(
                    "Business Unit does not belong to selected Business Group");
        }
        if (!Integer.valueOf(1).equals(businessUnit.getStatus())) {
            throw new BadRequestException(
                    "Selected Business Unit is not active");
        }
    }

    private void attachCodes(List<UserTypeGroup> rows) {
        java.util.Map<Long, String> codes = new java.util.HashMap<>();
        for (UserTypeGroup r : rows) {
            codes.computeIfAbsent(r.getUserTypeId(), id ->
                    userTypeRepository.findById(id)
                            .map(UserType::getUserTypeCode).orElse(null));
        }
        rows.forEach(r -> r.setUserTypeCode(codes.get(r.getUserTypeId())));
    }
}
