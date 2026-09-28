package com.tse.erp.module.admin.service.impl;

import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.admin.service.BusinessUnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BusinessUnitServiceImpl
        implements BusinessUnitService {

    private final BusinessUnitRepository businessUnitRepository;
    private final BusinessGroupRepository businessGroupRepository;

    @Override
    public List<BusinessUnit> getAllBusinessUnits() {
        return businessUnitRepository.findAllByOrderByIdDesc();
    }

    @Override
    public List<BusinessUnit> getBusinessUnitsByGroupId(Long bgId) {
        businessGroupRepository.findById(bgId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Business Group not found with id: " + bgId));
        return businessUnitRepository
                .findByBgIdOrderByIdDesc(bgId);
    }

    @Override
    public BusinessUnit getBusinessUnitById(Long id) {
        return businessUnitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Business Unit not found with id: " + id));
    }

    @Override
    public BusinessUnit createBusinessUnit(
            BusinessUnit businessUnit) {

        // BG exist check
        if (businessUnit.getBgId() == null) {
            throw new BadRequestException(
                    "Group id cannot be empty");
        }

        businessGroupRepository.findById(businessUnit.getBgId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Business Group not found with id: "
                                + businessUnit.getBgId()));

        // Unit name validation
        validateUnitName(businessUnit.getBusinessUnit());

        // Address validation
        if (businessUnit.getAddress() == null ||
                businessUnit.getAddress().trim().isEmpty()) {
            throw new BadRequestException(
                    "Address cannot be empty");
        }
        if (businessUnit.getAddress().trim().length() > 500) {
            throw new BadRequestException(
                    "Address cannot exceed 500 characters");
        }

        // Email validation
        validateEmail(businessUnit.getEmail());

        // Mobile validation
        validateMobile(businessUnit.getMobileNo());

        // Web address validation (optional)
        if (businessUnit.getWebAddress() != null &&
                !businessUnit.getWebAddress().trim().isEmpty()) {
            validateWebAddress(businessUnit.getWebAddress());
        }

        // Report header/footer length check
        if (businessUnit.getReportHeader() != null &&
                businessUnit.getReportHeader().length() > 500) {
            throw new BadRequestException(
                    "Report header cannot exceed 500 characters");
        }
        if (businessUnit.getReportFooter() != null &&
                businessUnit.getReportFooter().length() > 500) {
            throw new BadRequestException(
                    "Report footer cannot exceed 500 characters");
        }

        // Duplicate unit name check
        boolean exists = !businessUnitRepository
                .findByBusinessUnitIgnoreCase(
                        businessUnit.getBusinessUnit().trim())
                .isEmpty();
        if (exists) {
            throw new DuplicateResourceException(
                    "Business Unit already exists: "
                            + businessUnit.getBusinessUnit());
        }

        businessUnit.setBusinessUnit(
                businessUnit.getBusinessUnit().trim());
        businessUnit.setEmail(
                businessUnit.getEmail().trim());

        return businessUnitRepository.save(businessUnit);
    }

    @Override
    public BusinessUnit updateBusinessUnit(
            Long id, BusinessUnit businessUnit) {

        BusinessUnit existing = getBusinessUnitById(id);

        // BG exist check
        if (businessUnit.getBgId() == null) {
            throw new BadRequestException(
                    "Group id cannot be empty");
        }

        businessGroupRepository.findById(businessUnit.getBgId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Business Group not found with id: "
                                + businessUnit.getBgId()));

        // Unit name validation
        validateUnitName(businessUnit.getBusinessUnit());

        // Address validation
        if (businessUnit.getAddress() == null ||
                businessUnit.getAddress().trim().isEmpty()) {
            throw new BadRequestException(
                    "Address cannot be empty");
        }

        // Email validation
        validateEmail(businessUnit.getEmail());

        // Mobile validation
        validateMobile(businessUnit.getMobileNo());

        // Web address validation (optional)
        if (businessUnit.getWebAddress() != null &&
                !businessUnit.getWebAddress().trim().isEmpty()) {
            validateWebAddress(businessUnit.getWebAddress());
        }

        // Duplicate check — nijer id bade
        List<BusinessUnit> found = businessUnitRepository
                .findByBusinessUnitIgnoreCase(
                        businessUnit.getBusinessUnit().trim());

        boolean duplicateExists = found.stream()
                .anyMatch(bu -> !bu.getId().equals(id));

        if (duplicateExists) {
            throw new DuplicateResourceException(
                    "Business Unit already exists: "
                            + businessUnit.getBusinessUnit());
        }

        existing.setBgId(businessUnit.getBgId());
        existing.setBusinessUnit(
                businessUnit.getBusinessUnit().trim());
        existing.setBuLogo(businessUnit.getBuLogo());
        existing.setAddress(businessUnit.getAddress().trim());
        existing.setEmail(businessUnit.getEmail().trim());
        existing.setMobileNo(businessUnit.getMobileNo());
        existing.setWebAddress(businessUnit.getWebAddress());
        existing.setReportHeader(businessUnit.getReportHeader());
        existing.setReportFooter(businessUnit.getReportFooter());
        existing.setStatus(businessUnit.getStatus());

        return businessUnitRepository.save(existing);
    }

    @Override
    public void deleteBusinessUnit(Long id) {
        BusinessUnit existing = getBusinessUnitById(id);
        businessUnitRepository.delete(existing);
    }

    // =========================================
    // VALIDATION HELPERS
    // =========================================
    private void validateUnitName(String unitName) {
        if (unitName == null || unitName.trim().isEmpty()) {
            throw new BadRequestException(
                    "Unit name cannot be empty");
        }
        if (unitName.trim().length() < 2) {
            throw new BadRequestException(
                    "Unit name must be at least 2 characters");
        }
        if (unitName.trim().length() > 100) {
            throw new BadRequestException(
                    "Unit name cannot exceed 100 characters");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new BadRequestException(
                    "Email cannot be empty");
        }
        if (!email.trim().matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new BadRequestException(
                    "Invalid email format");
        }
        if (email.trim().length() > 150) {
            throw new BadRequestException(
                    "Email cannot exceed 150 characters");
        }
    }

    private void validateMobile(String mobile) {
        if (mobile == null || mobile.trim().isEmpty()) {
            throw new BadRequestException(
                    "Mobile number cannot be empty");
        }
        if (!mobile.matches("^01[0-9]{9}$")) {
            throw new BadRequestException(
                    "Invalid mobile number. " +
                            "Must be 11 digits starting with 01");
        }
    }

    private void validateWebAddress(String webAddress) {
        if (!webAddress.trim().startsWith("http://") &&
                !webAddress.trim().startsWith("https://")) {
            throw new BadRequestException(
                    "Web address must start with " +
                            "http:// or https://");
        }
    }
}