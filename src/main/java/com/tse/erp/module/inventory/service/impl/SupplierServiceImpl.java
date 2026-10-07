package com.tse.erp.module.inventory.service.impl;

import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.inventory.entity.Supplier;
import com.tse.erp.module.inventory.repository.SupplierRepository;
import com.tse.erp.module.inventory.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final BusinessGroupRepository businessGroupRepository;
    private final BusinessUnitRepository businessUnitRepository;

    @Override
    public List<Supplier> getAllSuppliers(Long buId) {
        if (buId == null) {
            return supplierRepository.findAllByOrderByIdDesc();
        }
        return supplierRepository.findByBuIdOrderByIdDesc(buId);
    }

    @Override
    public Supplier getSupplierById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Supplier not found with id: " + id));
    }

    @Override
    public Supplier createSupplier(Supplier supplier) {
        validateParents(supplier.getBgId(), supplier.getBuId());
        validateText(supplier.getSupplierName(), "Supplier name", 2, 150);
        validateCode(
                supplier.getSupplierCode(),
                "Supplier code",
                2,
                30,
                "^[A-Za-z0-9_-]+$",
                "Supplier code allows only letters, digits, hyphen and underscore");
        validateContactPerson(supplier.getContactPerson());
        String phone = normalizePhone(supplier.getPhone());
        String email = normalizeEmail(supplier.getEmail());
        validateText(supplier.getAddress(), "Address", 5, 255);
        validateCode(
                supplier.getTaxId(),
                "Tax ID",
                5,
                50,
                "^[A-Za-z0-9-]+$",
                "Tax ID allows only letters, digits and hyphen");
        validateCompanyType(supplier.getCompanyType());
        StatusUtil.validate(supplier.getStatus());

        String name = supplier.getSupplierName().trim();
        String code = supplier.getSupplierCode().trim();
        String contactPerson = supplier.getContactPerson().trim();
        String address = supplier.getAddress().trim();
        String taxId = supplier.getTaxId().trim();
        String companyType = supplier.getCompanyType() == null
                || supplier.getCompanyType().trim().isEmpty()
                ? null
                : supplier.getCompanyType().trim();

        if (!supplierRepository
                .findByBuIdAndSupplierNameIgnoreCase(
                        supplier.getBuId(), name)
                .isEmpty()) {
            throw new DuplicateResourceException(
                    "Supplier name already exists: " + name);
        }
        if (!supplierRepository
                .findByBuIdAndSupplierCodeIgnoreCase(
                        supplier.getBuId(), code)
                .isEmpty()) {
            throw new DuplicateResourceException(
                    "Supplier code already exists: " + code);
        }
        if (!supplierRepository
                .findByBuIdAndTaxIdIgnoreCase(
                        supplier.getBuId(), taxId)
                .isEmpty()) {
            throw new DuplicateResourceException(
                    "Tax ID already exists: " + taxId);
        }

        supplier.setId(null);
        supplier.setSupplierName(name);
        supplier.setSupplierCode(code);
        supplier.setContactPerson(contactPerson);
        supplier.setPhone(phone);
        supplier.setEmail(email);
        supplier.setAddress(address);
        supplier.setCompanyType(companyType);
        supplier.setTaxId(taxId);
        return supplierRepository.save(supplier);
    }

    @Override
    public Supplier updateSupplier(Long id, Supplier supplier) {
        Supplier existing = getSupplierById(id);

        validateParents(supplier.getBgId(), supplier.getBuId());
        validateText(supplier.getSupplierName(), "Supplier name", 2, 150);
        validateCode(
                supplier.getSupplierCode(),
                "Supplier code",
                2,
                30,
                "^[A-Za-z0-9_-]+$",
                "Supplier code allows only letters, digits, hyphen and underscore");
        validateContactPerson(supplier.getContactPerson());
        String phone = normalizePhone(supplier.getPhone());
        String email = normalizeEmail(supplier.getEmail());
        validateText(supplier.getAddress(), "Address", 5, 255);
        validateCode(
                supplier.getTaxId(),
                "Tax ID",
                5,
                50,
                "^[A-Za-z0-9-]+$",
                "Tax ID allows only letters, digits and hyphen");
        validateCompanyType(supplier.getCompanyType());
        StatusUtil.validate(supplier.getStatus());

        String name = supplier.getSupplierName().trim();
        String code = supplier.getSupplierCode().trim();
        String contactPerson = supplier.getContactPerson().trim();
        String address = supplier.getAddress().trim();
        String taxId = supplier.getTaxId().trim();
        String companyType = supplier.getCompanyType() == null
                || supplier.getCompanyType().trim().isEmpty()
                ? null
                : supplier.getCompanyType().trim();

        if (supplierRepository
                .findByBuIdAndSupplierNameIgnoreCase(
                        supplier.getBuId(), name)
                .stream()
                .anyMatch(x -> !x.getId().equals(id))) {
            throw new DuplicateResourceException(
                    "Supplier name already exists: " + name);
        }
        if (supplierRepository
                .findByBuIdAndSupplierCodeIgnoreCase(
                        supplier.getBuId(), code)
                .stream()
                .anyMatch(x -> !x.getId().equals(id))) {
            throw new DuplicateResourceException(
                    "Supplier code already exists: " + code);
        }
        if (supplierRepository
                .findByBuIdAndTaxIdIgnoreCase(
                        supplier.getBuId(), taxId)
                .stream()
                .anyMatch(x -> !x.getId().equals(id))) {
            throw new DuplicateResourceException(
                    "Tax ID already exists: " + taxId);
        }

        existing.setBgId(supplier.getBgId());
        existing.setBuId(supplier.getBuId());
        existing.setSupplierName(name);
        existing.setSupplierCode(code);
        existing.setContactPerson(contactPerson);
        existing.setPhone(phone);
        existing.setEmail(email);
        existing.setAddress(address);
        existing.setCompanyType(companyType);
        existing.setTaxId(taxId);
        if (supplier.getStatus() != null) {
            existing.setStatus(supplier.getStatus());
        }

        return supplierRepository.save(existing);
    }

    @Override
    public void deleteSupplier(Long id) {
        Supplier existing = getSupplierById(id);
        supplierRepository.delete(existing);
    }

    private void validateParents(Long bgId, Long buId) {
        if (bgId == null) {
            throw new BadRequestException(
                    "Business Group id cannot be empty");
        }
        if (buId == null) {
            throw new BadRequestException(
                    "Business Unit id cannot be empty");
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
    }

    private void validateText(
            String value, String label, int min, int max) {
        if (value == null || value.trim().isEmpty()) {
            throw new BadRequestException(label + " cannot be empty");
        }
        if (value.trim().length() < min) {
            throw new BadRequestException(
                    label + " must be at least " + min + " characters");
        }
        if (value.trim().length() > max) {
            throw new BadRequestException(
                    label + " cannot exceed " + max + " characters");
        }
    }

    private void validateCode(
            String value,
            String label,
            int min,
            int max,
            String regex,
            String formatMessage) {
        validateText(value, label, min, max);
        if (!value.trim().matches(regex)) {
            throw new BadRequestException(formatMessage);
        }
    }

    private void validateContactPerson(String value) {
        validateText(value, "Contact person", 2, 100);
        if (!value.trim().matches("^[\\p{L} .'-]+$")) {
            throw new BadRequestException(
                    "Contact person contains invalid characters");
        }
    }

    private String normalizePhone(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new BadRequestException("Phone cannot be empty");
        }
        String phone = value.trim().replaceAll("[\\s-]", "");
        if (!phone.matches("^\\+?[0-9]{7,15}$")) {
            throw new BadRequestException("Invalid phone number");
        }
        return phone;
    }

    private String normalizeEmail(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new BadRequestException("Email cannot be empty");
        }
        String email = value.trim().toLowerCase();
        if (email.length() > 150) {
            throw new BadRequestException(
                    "Email cannot exceed 150 characters");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new BadRequestException("Invalid email format");
        }
        return email;
    }

    private void validateCompanyType(String value) {
        if (value == null || value.trim().isEmpty()) {
            return;
        }
        if (value.trim().length() > 50) {
            throw new BadRequestException(
                    "Company type cannot exceed 50 characters");
        }
    }
}
