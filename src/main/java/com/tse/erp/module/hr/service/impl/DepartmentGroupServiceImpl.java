package com.tse.erp.module.hr.service.impl;

import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.hr.entity.Department;
import com.tse.erp.module.hr.entity.DepartmentGroup;
import com.tse.erp.module.hr.repository.DepartmentGroupRepository;
import com.tse.erp.module.hr.repository.DepartmentRepository;
import com.tse.erp.module.hr.service.DepartmentGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentGroupServiceImpl
        implements DepartmentGroupService {

    private final DepartmentGroupRepository departmentGroupRepository;
    private final BusinessGroupRepository businessGroupRepository;
    private final BusinessUnitRepository businessUnitRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    public List<DepartmentGroup> getAllDepartmentGroups(Long deptId) {
        if (deptId == null) {
            return departmentGroupRepository.findAllByOrderByIdDesc();
        }
        return departmentGroupRepository.findByDeptIdOrderByIdDesc(deptId);
    }

    @Override
    public DepartmentGroup getDepartmentGroupById(Long id) {
        return departmentGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department Group not found with id: " + id));
    }

    @Override
    public DepartmentGroup createDepartmentGroup(
            DepartmentGroup departmentGroup) {
        validateParents(departmentGroup.getBgId(),
                departmentGroup.getBuId(),
                departmentGroup.getDeptId());
        StatusUtil.validate(departmentGroup.getStatus());

        boolean exists = !departmentGroupRepository
                .findByDeptIdAndBuId(
                        departmentGroup.getDeptId(),
                        departmentGroup.getBuId())
                .isEmpty();
        if (exists) {
            throw new DuplicateResourceException(
                    "This Department is already mapped to the selected Business Unit");
        }

        departmentGroup.setId(null);
        return departmentGroupRepository.save(departmentGroup);
    }

    @Override
    public DepartmentGroup updateDepartmentGroup(
            Long id, DepartmentGroup departmentGroup) {
        DepartmentGroup existing = getDepartmentGroupById(id);

        validateParents(departmentGroup.getBgId(),
                departmentGroup.getBuId(),
                departmentGroup.getDeptId());
        StatusUtil.validate(departmentGroup.getStatus());

        boolean duplicateExists = departmentGroupRepository
                .findByDeptIdAndBuId(
                        departmentGroup.getDeptId(),
                        departmentGroup.getBuId())
                .stream()
                .anyMatch(x -> !x.getId().equals(id));
        if (duplicateExists) {
            throw new DuplicateResourceException(
                    "This Department is already mapped to the selected Business Unit");
        }

        existing.setBgId(departmentGroup.getBgId());
        existing.setBuId(departmentGroup.getBuId());
        existing.setDeptId(departmentGroup.getDeptId());
        if (departmentGroup.getStatus() != null) {
            existing.setStatus(departmentGroup.getStatus());
        }

        return departmentGroupRepository.save(existing);
    }

    @Override
    public void deleteDepartmentGroup(Long id) {
        DepartmentGroup existing = getDepartmentGroupById(id);
        departmentGroupRepository.delete(existing);
    }

    private void validateParents(Long bgId, Long buId, Long deptId) {
        if (bgId == null) {
            throw new BadRequestException(
                    "Business Group id cannot be empty");
        }
        if (buId == null) {
            throw new BadRequestException(
                    "Business Unit id cannot be empty");
        }
        if (deptId == null) {
            throw new BadRequestException(
                    "Department cannot be empty");
        }

        businessGroupRepository.findById(bgId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Business Group not found with id: " + bgId));

        BusinessUnit bu = businessUnitRepository.findById(buId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Business Unit not found with id: " + buId));

        if (!bgId.equals(bu.getBgId())) {
            throw new BadRequestException(
                    "Business Unit does not belong to selected Business Group");
        }

        Department dept = departmentRepository.findById(deptId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with id: " + deptId));

        if (!Integer.valueOf(1).equals(dept.getStatus())) {
            throw new BadRequestException("Selected Department is not active");
        }
        if (!bgId.equals(dept.getBgId())) {
            throw new BadRequestException(
                    "Department does not belong to selected Business Group");
        }
    }
}
