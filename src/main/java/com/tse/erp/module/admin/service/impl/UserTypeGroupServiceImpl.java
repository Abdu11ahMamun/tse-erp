package com.tse.erp.module.admin.service.impl;

import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.dto.UserTypeGroupDto;
import com.tse.erp.module.admin.dto.UserTypeGroupRowDto;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.entity.UserType;
import com.tse.erp.module.admin.entity.UserTypeGroup;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.admin.repository.UserTypeGroupRepository;
import com.tse.erp.module.admin.repository.UserTypeRepository;
import com.tse.erp.module.admin.service.UserTypeGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserTypeGroupServiceImpl implements UserTypeGroupService {

    private final UserTypeGroupRepository userTypeGroupRepository;
    private final UserTypeRepository userTypeRepository;
    private final BusinessGroupRepository businessGroupRepository;
    private final BusinessUnitRepository businessUnitRepository;

    @Override
    public UserTypeGroupDto getByUserType(Long userTypeId) {
        UserType userType = userTypeRepository.findById(userTypeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User Type not found with id: " + userTypeId));

        List<UserTypeGroupRowDto> rows = userTypeGroupRepository
                .findByUserTypeIdOrderByIdAsc(userTypeId)
                .stream()
                .map(this::toRowDto)
                .collect(Collectors.toList());

        UserTypeGroupDto dto = new UserTypeGroupDto();
        dto.setUserTypeId(userType.getId());
        dto.setBgId(userType.getBgId());
        dto.setBusinessUnits(rows);
        return dto;
    }

    @Override
    @Transactional
    public UserTypeGroupDto save(Long userTypeId, UserTypeGroupDto dto) {
        UserType userType = userTypeRepository.findById(userTypeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User Type not found with id: " + userTypeId));

        if (!Integer.valueOf(1).equals(userType.getStatus())) {
            throw new BadRequestException("Selected User Type is not active");
        }

        Long bgId = dto.getBgId();
        if (bgId == null) {
            throw new BadRequestException("Business Group id cannot be empty");
        }

        businessGroupRepository.findById(bgId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Business Group not found with id: " + bgId));

        if (userType.getBgId() != null && !userType.getBgId().equals(bgId)) {
            throw new BadRequestException(
                    "User Type does not belong to selected Business Group");
        }

        List<UserTypeGroupRowDto> requestedRows = dto.getBusinessUnits() == null
                ? List.of()
                : dto.getBusinessUnits();
        Set<Long> requestedBuIds = new HashSet<>();

        for (UserTypeGroupRowDto row : requestedRows) {
            Long buId = row.getBuId();
            if (buId == null) {
                throw new BadRequestException("Business Unit cannot be empty");
            }

            StatusUtil.validate(row.getStatus());

            BusinessUnit businessUnit = businessUnitRepository.findById(buId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Business Unit not found with id: " + buId));

            if (!bgId.equals(businessUnit.getBgId())) {
                throw new BadRequestException(
                        "Business Unit does not belong to selected Business Group");
            }

            Integer status = row.getStatus();
            if ((status == null || status == 1)
                    && !Integer.valueOf(1).equals(businessUnit.getStatus())) {
                throw new BadRequestException(
                        "Selected Business Unit is not active");
            }

            if (!requestedBuIds.add(buId)) {
                throw new DuplicateResourceException(
                        "Duplicate Business Unit in request: " + buId);
            }
        }

        List<UserTypeGroup> currentRows = userTypeGroupRepository
                .findByUserTypeIdOrderByIdAsc(userTypeId);
        Map<Long, UserTypeGroup> rowsById = currentRows.stream()
                .collect(Collectors.toMap(UserTypeGroup::getId, row -> row));
        Set<Long> requestedRowIds = new HashSet<>();
        List<UserTypeGroup> rowsToSave = new ArrayList<>(currentRows);

        for (UserTypeGroupRowDto row : requestedRows) {
            UserTypeGroup entity;
            if (row.getId() != null) {
                entity = rowsById.get(row.getId());
                if (entity == null) {
                    throw new BadRequestException(
                            "Row id " + row.getId()
                                    + " does not belong to this User Type");
                }
                requestedRowIds.add(row.getId());
                entity.setBuId(row.getBuId());
                if (row.getStatus() != null) {
                    entity.setStatus(row.getStatus());
                }
            } else {
                entity = new UserTypeGroup();
                entity.setBgId(bgId);
                entity.setUserTypeId(userTypeId);
                entity.setBuId(row.getBuId());
                if (row.getStatus() != null) {
                    entity.setStatus(row.getStatus());
                }
                rowsToSave.add(entity);
            }
        }

        for (UserTypeGroup currentRow : currentRows) {
            if (!requestedRowIds.contains(currentRow.getId())) {
                currentRow.setStatus(0);
            }
        }

        Set<Long> finalBuIds = new HashSet<>();
        for (UserTypeGroup row : rowsToSave) {
            if (!finalBuIds.add(row.getBuId())) {
                throw new DuplicateResourceException(
                        "Business Unit already mapped to this User Type: "
                                + row.getBuId());
            }
        }

        userTypeGroupRepository.saveAll(rowsToSave);
        return getByUserType(userTypeId);
    }

    private UserTypeGroupRowDto toRowDto(UserTypeGroup entity) {
        UserTypeGroupRowDto row = new UserTypeGroupRowDto();
        row.setId(entity.getId());
        row.setBuId(entity.getBuId());
        row.setStatus(entity.getStatus());
        return row;
    }
}
