package com.tse.erp.module.hr.service.impl;

import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.hr.entity.Department;
import com.tse.erp.module.hr.entity.DeptDesigMap;
import com.tse.erp.module.hr.entity.Designation;
import com.tse.erp.module.hr.repository.DepartmentGroupRepository;
import com.tse.erp.module.hr.repository.DepartmentRepository;
import com.tse.erp.module.hr.repository.DeptDesigMapRepository;
import com.tse.erp.module.hr.repository.DesignationGroupRepository;
import com.tse.erp.module.hr.repository.DesignationRepository;
import com.tse.erp.module.hr.service.DeptDesigMapService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeptDesigMapServiceImpl implements DeptDesigMapService {

    private final DeptDesigMapRepository deptDesigMapRepository;
    private final BusinessGroupRepository businessGroupRepository;
    private final BusinessUnitRepository businessUnitRepository;
    private final DepartmentRepository departmentRepository;
    private final DepartmentGroupRepository departmentGroupRepository;
    private final DesignationRepository designationRepository;
    private final DesignationGroupRepository designationGroupRepository;

    @Override
    public List<DeptDesigMap> getAllMappings(Long buId) {
        if (buId == null) {
            return deptDesigMapRepository.findAllByOrderByIdDesc();
        }
        return deptDesigMapRepository.findByBuIdOrderByIdDesc(buId);
    }

    @Override
    public DeptDesigMap getMappingById(Long id) {
        return deptDesigMapRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Mapping not found with id: " + id));
    }

    @Override
    public DeptDesigMap createMapping(DeptDesigMap mapping) {
        validateAll(mapping);
        StatusUtil.validate(mapping.getStatus());

        boolean exists = !deptDesigMapRepository
                .findByBuIdAndDeptIdAndDesigId(
                        mapping.getBuId(),
                        mapping.getDeptId(),
                        mapping.getDesigId())
                .isEmpty();
        if (exists) {
            throw new DuplicateResourceException(
                    "This Department and Designation mapping already exists for the selected Business Unit");
        }

        mapping.setId(null);
        return deptDesigMapRepository.save(mapping);
    }

    @Override
    public DeptDesigMap updateMapping(Long id, DeptDesigMap mapping) {
        DeptDesigMap existing = getMappingById(id);

        validateAll(mapping);
        StatusUtil.validate(mapping.getStatus());

        boolean duplicateExists = deptDesigMapRepository
                .findByBuIdAndDeptIdAndDesigId(
                        mapping.getBuId(),
                        mapping.getDeptId(),
                        mapping.getDesigId())
                .stream()
                .anyMatch(x -> !x.getId().equals(id));
        if (duplicateExists) {
            throw new DuplicateResourceException(
                    "This Department and Designation mapping already exists for the selected Business Unit");
        }

        existing.setBgId(mapping.getBgId());
        existing.setBuId(mapping.getBuId());
        existing.setDeptId(mapping.getDeptId());
        existing.setDesigId(mapping.getDesigId());
        if (mapping.getStatus() != null) {
            existing.setStatus(mapping.getStatus());
        }

        return deptDesigMapRepository.save(existing);
    }

    @Override
    public void deleteMapping(Long id) {
        DeptDesigMap existing = getMappingById(id);
        deptDesigMapRepository.delete(existing);
    }

    private void validateAll(DeptDesigMap mapping) {
        if (mapping.getBgId() == null) {
            throw new BadRequestException(
                    "Business Group id cannot be empty");
        }
        if (mapping.getBuId() == null) {
            throw new BadRequestException(
                    "Business Unit id cannot be empty");
        }
        if (mapping.getDeptId() == null) {
            throw new BadRequestException(
                    "Department cannot be empty");
        }
        if (mapping.getDesigId() == null) {
            throw new BadRequestException(
                    "Designation cannot be empty");
        }

        businessGroupRepository.findById(mapping.getBgId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Business Group not found with id: "
                                + mapping.getBgId()));

        BusinessUnit bu = businessUnitRepository.findById(mapping.getBuId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Business Unit not found with id: "
                                + mapping.getBuId()));
        if (!mapping.getBgId().equals(bu.getBgId())) {
            throw new BadRequestException(
                    "Business Unit does not belong to selected Business Group");
        }

        Department department = departmentRepository
                .findById(mapping.getDeptId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with id: "
                                + mapping.getDeptId()));
        if (!Integer.valueOf(1).equals(department.getStatus())) {
            throw new BadRequestException(
                    "Selected Department is not active");
        }
        if (!mapping.getBgId().equals(department.getBgId())) {
            throw new BadRequestException(
                    "Department does not belong to selected Business Group");
        }
        if (departmentGroupRepository.findByDeptIdAndBuId(
                mapping.getDeptId(), mapping.getBuId()).isEmpty()) {
            throw new BadRequestException(
                    "Department is not mapped to selected Business Unit");
        }

        Designation designation = designationRepository
                .findById(mapping.getDesigId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Designation not found with id: "
                                + mapping.getDesigId()));
        if (!Integer.valueOf(1).equals(designation.getStatus())) {
            throw new BadRequestException(
                    "Selected Designation is not active");
        }
        if (!mapping.getBgId().equals(designation.getBgId())) {
            throw new BadRequestException(
                    "Designation does not belong to selected Business Group");
        }
        if (designationGroupRepository.findByDesigIdAndBuId(
                mapping.getDesigId(), mapping.getBuId()).isEmpty()) {
            throw new BadRequestException(
                    "Designation is not mapped to selected Business Unit");
        }
    }
}
