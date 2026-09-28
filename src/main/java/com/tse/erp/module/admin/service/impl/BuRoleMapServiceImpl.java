package com.tse.erp.module.admin.service.impl;

import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.dto.AssignedRoleDto;
import com.tse.erp.module.admin.dto.BuRoleMapResponseDto;
import com.tse.erp.module.admin.entity.BuRoleMap;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.repository.BuRoleMapRepository;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.admin.repository.RoleRepository;
import com.tse.erp.module.admin.service.BuRoleMapService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BuRoleMapServiceImpl implements BuRoleMapService {

    private final BuRoleMapRepository buRoleMapRepository;
    private final BusinessUnitRepository businessUnitRepository;
    private final RoleRepository roleRepository;

    @Override
    public BuRoleMapResponseDto getRolesByBusinessUnit(Long buId) {

        BusinessUnit bu = businessUnitRepository.findById(buId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Business Unit not found with id: " + buId));

        List<BuRoleMap> mappings =
                buRoleMapRepository.findByBuIdOrderByIdDesc(buId);

        List<AssignedRoleDto> assignedRoles = mappings.stream()
                .map(m -> {
                    String roleName = roleRepository
                            .findById(m.getRoleId())
                            .map(r -> r.getRoleName())
                            .orElse("Unknown");
                    return AssignedRoleDto.builder()
                            .id(m.getId())
                            .roleId(m.getRoleId())
                            .roleName(roleName)
                            .build();
                })
                .collect(Collectors.toList());

        return BuRoleMapResponseDto.builder()
                .buId(bu.getId())
                .businessUnitName(bu.getBusinessUnit())
                .assignedRoles(assignedRoles)
                .build();
    }

    @Override
    @Transactional
    public BuRoleMapResponseDto assignRole(Long buId, Long roleId) {

        // BU exist check
        businessUnitRepository.findById(buId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Business Unit not found with id: " + buId));

        // Role exist check
        roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role not found with id: " + roleId));

        // Duplicate check
        boolean exists = buRoleMapRepository
                .findByRoleIdAndBuId(roleId, buId)
                .isPresent();

        if (exists) {
            throw new DuplicateResourceException(
                    "This role is already assigned to this business unit");
        }

        BuRoleMap mapping = BuRoleMap.builder()
                .roleId(roleId)
                .buId(buId)
                .build();

        buRoleMapRepository.save(mapping);

        return getRolesByBusinessUnit(buId);
    }

    @Override
    @Transactional
    public BuRoleMapResponseDto removeRole(Long buId, Long mappingId) {

        BuRoleMap mapping = buRoleMapRepository.findById(mappingId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Mapping not found with id: " + mappingId));

        if (!mapping.getBuId().equals(buId)) {
            throw new BadRequestException(
                    "Mapping does not belong to this business unit");
        }

        buRoleMapRepository.delete(mapping);

        return getRolesByBusinessUnit(buId);
    }
}