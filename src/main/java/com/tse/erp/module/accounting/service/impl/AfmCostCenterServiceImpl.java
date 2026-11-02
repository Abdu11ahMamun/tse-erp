package com.tse.erp.module.accounting.service.impl;

import com.tse.erp.common.AppConstants;
import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.accounting.dto.AfmCostCenterRequestDto;
import com.tse.erp.module.accounting.dto.AfmCostCenterResponseDto;
import com.tse.erp.module.accounting.entity.AfmCostCenter;
import com.tse.erp.module.accounting.repository.AfmCostCenterRepository;
import com.tse.erp.module.accounting.service.AfmCostCenterService;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.hr.entity.Department;
import com.tse.erp.module.hr.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AfmCostCenterServiceImpl implements AfmCostCenterService {

    private final AfmCostCenterRepository costCenterRepository;
    private final BusinessGroupRepository businessGroupRepository;
    private final BusinessUnitRepository businessUnitRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    public List<AfmCostCenterResponseDto> getAllCostCenters(
            Long bgId,
            Long buId,
            Long projectId,
            Long departmentId,
            Integer status) {

        List<AfmCostCenter> costCenters;

        if (bgId != null && buId != null && status != null) {
            costCenters = costCenterRepository.findByBgIdAndBuIdAndStatusOrderByIdDesc(bgId, buId, status);
        } else if (bgId != null && buId != null) {
            costCenters = costCenterRepository.findByBgIdAndBuIdOrderByIdDesc(bgId, buId);
        } else if (buId != null && status != null) {
            costCenters = costCenterRepository.findByBuIdAndStatusOrderByIdDesc(buId, status);
        } else if (buId != null) {
            costCenters = costCenterRepository.findByBuIdOrderByIdDesc(buId);
        } else if (bgId != null) {
            costCenters = costCenterRepository.findByBgIdOrderByIdDesc(bgId);
        } else {
            costCenters = costCenterRepository.findAllByOrderByIdDesc();
        }

        if (status != null && (bgId != null && buId == null || bgId == null && buId == null)) {
            costCenters = costCenters.stream().filter(c -> status.equals(c.getStatus())).toList();
        }

        if (projectId != null) {
            costCenters = costCenters.stream().filter(c -> projectId.equals(c.getProjectId())).toList();
        }

        if (departmentId != null) {
            costCenters = costCenters.stream().filter(c -> departmentId.equals(c.getDepartmentId())).toList();
        }

        return toResponseList(costCenters);
    }

    @Override
    public AfmCostCenterResponseDto getCostCenterById(Long id) {
        return toResponse(findCostCenter(id));
    }

    @Override
    @Transactional
    public AfmCostCenterResponseDto createCostCenter(AfmCostCenterRequestDto request) {
        validateParents(request.getBgId(), request.getBuId());
        validateDepartment(request.getBgId(), request.getDepartmentId());
        validateCostCenterName(request.getCostCenter());
        validateShortName(request.getShortName());
        StatusUtil.validate(request.getStatus());

        String costCenterName = request.getCostCenter().trim();
        String shortName = request.getShortName() != null && !request.getShortName().trim().isEmpty()
                ? request.getShortName().trim()
                : null;

        boolean exists = costCenterRepository.existsByBuIdAndCostCenterIgnoreCase(request.getBuId(), costCenterName);
        if (exists) {
            throw new DuplicateResourceException("Cost Center already exists for this Business Unit: " + costCenterName);
        }

        AfmCostCenter entity = AfmCostCenter.builder()
                .bgId(request.getBgId())
                .buId(request.getBuId())
                .projectId(request.getProjectId())
                .departmentId(request.getDepartmentId())
                .costCenter(costCenterName)
                .shortName(shortName)
                .build();

        entity.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        entity.setCreatedApp(AppConstants.APP_NAME);

        AfmCostCenter saved = costCenterRepository.save(entity);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public AfmCostCenterResponseDto updateCostCenter(Long id, AfmCostCenterRequestDto request) {
        AfmCostCenter existing = findCostCenter(id);

        validateParents(request.getBgId(), request.getBuId());
        validateDepartment(request.getBgId(), request.getDepartmentId());
        validateCostCenterName(request.getCostCenter());
        validateShortName(request.getShortName());
        StatusUtil.validate(request.getStatus());

        String costCenterName = request.getCostCenter().trim();
        String shortName = request.getShortName() != null && !request.getShortName().trim().isEmpty()
                ? request.getShortName().trim()
                : null;

        boolean duplicateExists = costCenterRepository
                .findByBuIdAndCostCenterIgnoreCase(request.getBuId(), costCenterName)
                .stream()
                .anyMatch(c -> !c.getId().equals(id));
        if (duplicateExists) {
            throw new DuplicateResourceException("Cost Center already exists for this Business Unit: " + costCenterName);
        }

        existing.setBgId(request.getBgId());
        existing.setBuId(request.getBuId());
        existing.setProjectId(request.getProjectId());
        existing.setDepartmentId(request.getDepartmentId());
        existing.setCostCenter(costCenterName);
        existing.setShortName(shortName);

        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }
        existing.setUpdatedApp(AppConstants.APP_NAME);

        AfmCostCenter saved = costCenterRepository.save(existing);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteCostCenter(Long id) {
        AfmCostCenter existing = findCostCenter(id);
        costCenterRepository.delete(existing);
    }

    // =========================================
    // HELPER & VALIDATION METHODS
    // =========================================

    private AfmCostCenter findCostCenter(Long id) {
        return costCenterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cost Center not found with id: " + id));
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

    private void validateDepartment(Long bgId, Long departmentId) {
        if (departmentId == null) {
            return;
        }

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + departmentId));

        if (!Integer.valueOf(1).equals(department.getStatus())) {
            throw new BadRequestException("Selected Department is not active");
        }

        if (bgId != null && !bgId.equals(department.getBgId())) {
            throw new BadRequestException("Selected Department does not belong to the selected Business Group");
        }
    }

    private void validateCostCenterName(String costCenter) {
        if (costCenter == null || costCenter.trim().isEmpty()) {
            throw new BadRequestException("Cost Center name cannot be empty");
        }
        if (costCenter.trim().length() < 2) {
            throw new BadRequestException("Cost Center name must be at least 2 characters");
        }
        if (costCenter.trim().length() > 150) {
            throw new BadRequestException("Cost Center name cannot exceed 150 characters");
        }
    }

    private void validateShortName(String shortName) {
        if (shortName != null && shortName.trim().length() > 50) {
            throw new BadRequestException("Short name cannot exceed 50 characters");
        }
    }

    private AfmCostCenterResponseDto toResponse(AfmCostCenter entity) {
        Department department = entity.getDepartmentId() != null
                ? departmentRepository.findById(entity.getDepartmentId()).orElse(null)
                : null;

        return buildResponseDto(entity, department);
    }

    private List<AfmCostCenterResponseDto> toResponseList(List<AfmCostCenter> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> departmentIds = entities.stream()
                .map(AfmCostCenter::getDepartmentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, Department> departmentMap = departmentIds.isEmpty()
                ? Collections.emptyMap()
                : departmentRepository.findAllById(departmentIds)
                        .stream()
                        .collect(Collectors.toMap(Department::getId, Function.identity(), (a, b) -> a));

        return entities.stream()
                .map(entity -> buildResponseDto(entity, departmentMap.get(entity.getDepartmentId())))
                .toList();
    }

    private AfmCostCenterResponseDto buildResponseDto(AfmCostCenter entity, Department department) {
        return AfmCostCenterResponseDto.builder()
                .id(entity.getId())
                .bgId(entity.getBgId())
                .buId(entity.getBuId())
                .projectId(entity.getProjectId())
                .departmentId(entity.getDepartmentId())
                .departmentName(department != null ? department.getDepartmentName() : null)
                .departmentShortName(department != null ? department.getShortName() : null)
                .costCenter(entity.getCostCenter())
                .shortName(entity.getShortName())
                .status(entity.getStatus())
                .createdUserId(entity.getCreatedUserId())
                .createdEmpId(entity.getCreatedEmpId())
                .createdDatetime(entity.getCreatedDatetime())
                .updatedUserId(entity.getUpdatedUserId())
                .updatedEmpId(entity.getUpdatedEmpId())
                .updatedDatetime(entity.getUpdatedDatetime())
                .createdApp(entity.getCreatedApp())
                .updatedApp(entity.getUpdatedApp())
                .build();
    }
}

