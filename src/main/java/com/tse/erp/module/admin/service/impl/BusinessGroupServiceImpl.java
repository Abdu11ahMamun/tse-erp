package com.tse.erp.module.admin.service.impl;

import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.BusinessGroup;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.admin.service.BusinessGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BusinessGroupServiceImpl
        implements BusinessGroupService {

    private final BusinessGroupRepository businessGroupRepository;

    @Override
    public List<BusinessGroup> getAllBusinessGroups() {
        return businessGroupRepository.findAllByOrderByIdDesc();
    }

    @Override
    public BusinessGroup getBusinessGroupById(Long id) {
        return businessGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Business Group not found with id: " + id));
    }

    @Override
    public BusinessGroup createBusinessGroup(
            BusinessGroup businessGroup) {

        // Validation
        if (businessGroup.getGroupName() == null ||
                businessGroup.getGroupName().trim().isEmpty()) {
            throw new BadRequestException(
                    "Group name cannot be empty");
        }

        if (businessGroup.getGroupName().trim().length() < 2) {
            throw new BadRequestException(
                    "Group name must be at least 2 characters");
        }

        if (businessGroup.getGroupName().trim().length() > 100) {
            throw new BadRequestException(
                    "Group name cannot exceed 100 characters");
        }

        // Duplicate check
        boolean exists = !businessGroupRepository
                .findByGroupNameIgnoreCase(
                        businessGroup.getGroupName().trim())
                .isEmpty();

        if (exists) {
            throw new DuplicateResourceException(
                    "Business Group already exists: "
                            + businessGroup.getGroupName());
        }

        businessGroup.setGroupName(
                businessGroup.getGroupName().trim());

        // Status default 1 — BaseEntity @PrePersist handle korbe
        return businessGroupRepository.save(businessGroup);
    }

    @Override
    public BusinessGroup updateBusinessGroup(
            Long id, BusinessGroup businessGroup) {

        BusinessGroup existing = getBusinessGroupById(id);

        // Validation
        if (businessGroup.getGroupName() == null ||
                businessGroup.getGroupName().trim().isEmpty()) {
            throw new BadRequestException(
                    "Group name cannot be empty");
        }

        if (businessGroup.getGroupName().trim().length() < 2) {
            throw new BadRequestException(
                    "Group name must be at least 2 characters");
        }

        if (businessGroup.getGroupName().trim().length() > 100) {
            throw new BadRequestException(
                    "Group name cannot exceed 100 characters");
        }

        // Duplicate check — nijer id bade
        List<BusinessGroup> found = businessGroupRepository
                .findByGroupNameIgnoreCase(
                        businessGroup.getGroupName().trim());

        boolean duplicateExists = found.stream()
                .anyMatch(bg -> !bg.getId().equals(id));

        if (duplicateExists) {
            throw new DuplicateResourceException(
                    "Business Group already exists: "
                            + businessGroup.getGroupName());
        }

        existing.setGroupName(businessGroup.getGroupName().trim());
        existing.setBgLogo(businessGroup.getBgLogo());
        existing.setStatus(businessGroup.getStatus());

        return businessGroupRepository.save(existing);
    }

    @Override
    public void deleteBusinessGroup(Long id) {
        BusinessGroup existing = getBusinessGroupById(id);
        businessGroupRepository.delete(existing);
    }
}