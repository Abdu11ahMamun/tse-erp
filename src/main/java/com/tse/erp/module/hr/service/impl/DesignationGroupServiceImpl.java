package com.tse.erp.module.hr.service.impl;

import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.hr.entity.Designation;
import com.tse.erp.module.hr.entity.DesignationGroup;
import com.tse.erp.module.hr.repository.DesignationGroupRepository;
import com.tse.erp.module.hr.repository.DesignationRepository;
import com.tse.erp.module.hr.service.DesignationGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DesignationGroupServiceImpl
        implements DesignationGroupService {

    private final DesignationGroupRepository designationGroupRepository;
    private final BusinessGroupRepository businessGroupRepository;
    private final BusinessUnitRepository businessUnitRepository;
    private final DesignationRepository designationRepository;

    @Override
    public List<DesignationGroup> getAllDesignationGroups(Long desigId) {
        if (desigId == null) {
            return designationGroupRepository.findAllByOrderByIdDesc();
        }
        return designationGroupRepository
                .findByDesigIdOrderByIdDesc(desigId);
    }

    @Override
    public DesignationGroup getDesignationGroupById(Long id) {
        return designationGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Designation Group not found with id: " + id));
    }

    @Override
    public DesignationGroup createDesignationGroup(
            DesignationGroup designationGroup) {
        validateParents(designationGroup.getBgId(),
                designationGroup.getBuId(),
                designationGroup.getDesigId());
        StatusUtil.validate(designationGroup.getStatus());

        boolean exists = !designationGroupRepository
                .findByDesigIdAndBuId(
                        designationGroup.getDesigId(),
                        designationGroup.getBuId())
                .isEmpty();
        if (exists) {
            throw new DuplicateResourceException(
                    "This Designation is already mapped to the selected Business Unit");
        }

        designationGroup.setId(null);
        return designationGroupRepository.save(designationGroup);
    }

    @Override
    public DesignationGroup updateDesignationGroup(
            Long id, DesignationGroup designationGroup) {
        DesignationGroup existing = getDesignationGroupById(id);

        validateParents(designationGroup.getBgId(),
                designationGroup.getBuId(),
                designationGroup.getDesigId());
        StatusUtil.validate(designationGroup.getStatus());

        boolean duplicateExists = designationGroupRepository
                .findByDesigIdAndBuId(
                        designationGroup.getDesigId(),
                        designationGroup.getBuId())
                .stream()
                .anyMatch(x -> !x.getId().equals(id));
        if (duplicateExists) {
            throw new DuplicateResourceException(
                    "This Designation is already mapped to the selected Business Unit");
        }

        existing.setBgId(designationGroup.getBgId());
        existing.setBuId(designationGroup.getBuId());
        existing.setDesigId(designationGroup.getDesigId());
        if (designationGroup.getStatus() != null) {
            existing.setStatus(designationGroup.getStatus());
        }

        return designationGroupRepository.save(existing);
    }

    @Override
    public void deleteDesignationGroup(Long id) {
        DesignationGroup existing = getDesignationGroupById(id);
        designationGroupRepository.delete(existing);
    }

    private void validateParents(Long bgId, Long buId, Long desigId) {
        if (bgId == null) {
            throw new BadRequestException(
                    "Business Group id cannot be empty");
        }
        if (buId == null) {
            throw new BadRequestException(
                    "Business Unit id cannot be empty");
        }
        if (desigId == null) {
            throw new BadRequestException(
                    "Designation cannot be empty");
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

        Designation designation = designationRepository.findById(desigId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Designation not found with id: " + desigId));

        if (!Integer.valueOf(1).equals(designation.getStatus())) {
            throw new BadRequestException(
                    "Selected Designation is not active");
        }
        if (!bgId.equals(designation.getBgId())) {
            throw new BadRequestException(
                    "Designation does not belong to selected Business Group");
        }
    }
}
