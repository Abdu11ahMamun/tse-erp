package com.tse.erp.module.asset.service.impl;

import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.asset.entity.AssetLocation;
import com.tse.erp.module.asset.entity.Custodian;
import com.tse.erp.module.asset.entity.CustodianDtl;
import com.tse.erp.module.asset.repository.AssetLocationRepository;
import com.tse.erp.module.asset.repository.CustodianDtlRepository;
import com.tse.erp.module.asset.repository.CustodianRepository;
import com.tse.erp.module.asset.service.AssetLocationService;
import com.tse.erp.module.hr.entity.Department;
import com.tse.erp.module.hr.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetLocationServiceImpl implements AssetLocationService {

    private final AssetLocationRepository assetLocationRepository;
    private final BusinessGroupRepository businessGroupRepository;
    private final BusinessUnitRepository businessUnitRepository;
    private final CustodianRepository custodianRepository;
    private final CustodianDtlRepository custodianDtlRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    public List<AssetLocation> getAllAssetLocations(Long buId) {
        if (buId == null) {
            return assetLocationRepository.findAllByOrderByIdDesc();
        }
        return assetLocationRepository.findByBuIdOrderByIdDesc(buId);
    }

    @Override
    public AssetLocation getAssetLocationById(Long id) {
        return assetLocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Asset Location not found with id: " + id));
    }

    @Override
    public AssetLocation createAssetLocation(AssetLocation location) {
        validateParents(location.getBgId(), location.getBuId());
        validateCustodian(location.getCustodianId(), location.getBuId());
        validateSubCustodian(location.getSubCustodianId(),
                location.getCustodianId());
        validateText(location.getLocationName(), "Location name", 2, 100);
        validateLocationCode(location.getLocationCode());
        validateText(location.getAddress(), "Address", 5, 255);
        validateDepartment(location.getDeptId(), location.getBgId());
        validateText(location.getBuilding(), "Building", 1, 100);
        validateText(location.getFloor(), "Floor", 1, 20);
        validateText(location.getRoom(), "Room", 1, 50);
        StatusUtil.validate(location.getStatus());

        String name = location.getLocationName().trim();
        String code = location.getLocationCode().trim();
        String address = location.getAddress().trim();
        String building = location.getBuilding().trim();
        String floor = location.getFloor().trim();
        String room = location.getRoom().trim();

        boolean nameExists = !assetLocationRepository
                .findByCustodianIdAndSubCustodianIdAndLocationNameIgnoreCase(
                        location.getCustodianId(),
                        location.getSubCustodianId(), name)
                .isEmpty();
        if (nameExists) {
            throw new DuplicateResourceException(
                    "Location name already exists under this Sub Custodian: "
                            + name);
        }

        boolean codeExists = !assetLocationRepository
                .findByBuIdAndLocationCodeIgnoreCase(
                        location.getBuId(), code)
                .isEmpty();
        if (codeExists) {
            throw new DuplicateResourceException(
                    "Location code already exists: " + code);
        }

        location.setId(null);
        location.setLocationName(name);
        location.setLocationCode(code);
        location.setAddress(address);
        location.setBuilding(building);
        location.setFloor(floor);
        location.setRoom(room);
        return assetLocationRepository.save(location);
    }

    @Override
    public AssetLocation updateAssetLocation(
            Long id, AssetLocation location) {
        AssetLocation existing = getAssetLocationById(id);

        validateParents(location.getBgId(), location.getBuId());
        validateCustodian(location.getCustodianId(), location.getBuId());
        validateSubCustodian(location.getSubCustodianId(),
                location.getCustodianId());
        validateText(location.getLocationName(), "Location name", 2, 100);
        validateLocationCode(location.getLocationCode());
        validateText(location.getAddress(), "Address", 5, 255);
        validateDepartment(location.getDeptId(), location.getBgId());
        validateText(location.getBuilding(), "Building", 1, 100);
        validateText(location.getFloor(), "Floor", 1, 20);
        validateText(location.getRoom(), "Room", 1, 50);
        StatusUtil.validate(location.getStatus());

        String name = location.getLocationName().trim();
        String code = location.getLocationCode().trim();
        String address = location.getAddress().trim();
        String building = location.getBuilding().trim();
        String floor = location.getFloor().trim();
        String room = location.getRoom().trim();

        boolean nameDuplicate = assetLocationRepository
                .findByCustodianIdAndSubCustodianIdAndLocationNameIgnoreCase(
                        location.getCustodianId(),
                        location.getSubCustodianId(), name)
                .stream()
                .anyMatch(x -> !x.getId().equals(id));
        if (nameDuplicate) {
            throw new DuplicateResourceException(
                    "Location name already exists under this Sub Custodian: "
                            + name);
        }

        boolean codeDuplicate = assetLocationRepository
                .findByBuIdAndLocationCodeIgnoreCase(
                        location.getBuId(), code)
                .stream()
                .anyMatch(x -> !x.getId().equals(id));
        if (codeDuplicate) {
            throw new DuplicateResourceException(
                    "Location code already exists: " + code);
        }

        existing.setBgId(location.getBgId());
        existing.setBuId(location.getBuId());
        existing.setCustodianId(location.getCustodianId());
        existing.setSubCustodianId(location.getSubCustodianId());
        existing.setLocationName(name);
        existing.setLocationCode(code);
        existing.setAddress(address);
        existing.setDeptId(location.getDeptId());
        existing.setBuilding(building);
        existing.setFloor(floor);
        existing.setRoom(room);
        if (location.getStatus() != null) {
            existing.setStatus(location.getStatus());
        }

        return assetLocationRepository.save(existing);
    }

    @Override
    public void deleteAssetLocation(Long id) {
        AssetLocation existing = getAssetLocationById(id);
        assetLocationRepository.delete(existing);
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

    private void validateLocationCode(String code) {
        validateText(code, "Location code", 2, 30);
        if (!code.trim().matches("^[A-Za-z0-9_-]+$")) {
            throw new BadRequestException(
                    "Location code allows only letters, digits, hyphen and underscore");
        }
    }

    private void validateCustodian(Long custodianId, Long buId) {
        if (custodianId == null) {
            throw new BadRequestException("Custodian cannot be empty");
        }

        Custodian custodian = custodianRepository.findById(custodianId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Custodian not found with id: " + custodianId));

        if (!Integer.valueOf(1).equals(custodian.getStatus())) {
            throw new BadRequestException(
                    "Selected Custodian is not active");
        }
        if (!buId.equals(custodian.getBuId())) {
            throw new BadRequestException(
                    "Custodian does not belong to selected Business Unit");
        }
    }

    private void validateSubCustodian(Long subId, Long custodianId) {
        if (subId == null) {
            throw new BadRequestException("Sub custodian cannot be empty");
        }

        CustodianDtl subCustodian = custodianDtlRepository.findById(subId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sub Custodian not found with id: " + subId));

        if (!Integer.valueOf(1).equals(subCustodian.getStatus())) {
            throw new BadRequestException(
                    "Selected Sub Custodian is not active");
        }
        if (!custodianId.equals(subCustodian.getCustodianId())) {
            throw new BadRequestException(
                    "Sub Custodian does not belong to selected Custodian");
        }
    }

    private void validateDepartment(Long deptId, Long bgId) {
        if (deptId == null) {
            throw new BadRequestException("Department cannot be empty");
        }

        Department department = departmentRepository.findById(deptId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with id: " + deptId));

        if (!Integer.valueOf(1).equals(department.getStatus())) {
            throw new BadRequestException(
                    "Selected Department is not active");
        }
        if (!bgId.equals(department.getBgId())) {
            throw new BadRequestException(
                    "Department does not belong to selected Business Group");
        }
    }
}
