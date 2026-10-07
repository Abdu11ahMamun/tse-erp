package com.tse.erp.module.asset.service.impl;

import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.asset.entity.AssetUom;
import com.tse.erp.module.asset.repository.AssetUomRepository;
import com.tse.erp.module.asset.service.AssetUomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetUomServiceImpl implements AssetUomService {

    private final AssetUomRepository assetUomRepository;
    private final BusinessGroupRepository businessGroupRepository;
    private final BusinessUnitRepository businessUnitRepository;

    @Override
    public List<AssetUom> getAllAssetUoms(Long buId) {
        if (buId == null) {
            return assetUomRepository.findAllByOrderByIdDesc();
        }
        return assetUomRepository.findByBuIdOrderByIdDesc(buId);
    }

    @Override
    public AssetUom getAssetUomById(Long id) {
        return assetUomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Asset UOM not found with id: " + id));
    }

    @Override
    public AssetUom createAssetUom(AssetUom assetUom) {
        validateParents(assetUom.getBgId(), assetUom.getBuId());
        validateUnitName(assetUom.getUnitName());
        validateSortOrder(assetUom.getSortOrder());
        StatusUtil.validate(assetUom.getStatus());

        String name = assetUom.getUnitName().trim();
        boolean exists = !assetUomRepository
                .findByBuIdAndUnitNameIgnoreCase(
                        assetUom.getBuId(), name)
                .isEmpty();
        if (exists) {
            throw new DuplicateResourceException(
                    "Unit name already exists: " + name);
        }

        assetUom.setId(null);
        assetUom.setUnitName(name);
        return assetUomRepository.save(assetUom);
    }

    @Override
    public AssetUom updateAssetUom(Long id, AssetUom assetUom) {
        AssetUom existing = getAssetUomById(id);

        validateParents(assetUom.getBgId(), assetUom.getBuId());
        validateUnitName(assetUom.getUnitName());
        validateSortOrder(assetUom.getSortOrder());
        StatusUtil.validate(assetUom.getStatus());

        String name = assetUom.getUnitName().trim();
        boolean duplicateExists = assetUomRepository
                .findByBuIdAndUnitNameIgnoreCase(
                        assetUom.getBuId(), name)
                .stream()
                .anyMatch(x -> !x.getId().equals(id));
        if (duplicateExists) {
            throw new DuplicateResourceException(
                    "Unit name already exists: " + name);
        }

        existing.setBgId(assetUom.getBgId());
        existing.setBuId(assetUom.getBuId());
        existing.setUnitName(name);
        existing.setSortOrder(assetUom.getSortOrder());
        if (assetUom.getStatus() != null) {
            existing.setStatus(assetUom.getStatus());
        }

        return assetUomRepository.save(existing);
    }

    @Override
    public void deleteAssetUom(Long id) {
        AssetUom existing = getAssetUomById(id);
        assetUomRepository.delete(existing);
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

    private void validateUnitName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new BadRequestException("Unit name cannot be empty");
        }
        if (name.trim().length() < 2) {
            throw new BadRequestException(
                    "Unit name must be at least 2 characters");
        }
        if (name.trim().length() > 50) {
            throw new BadRequestException(
                    "Unit name cannot exceed 50 characters");
        }
    }

    private void validateSortOrder(Integer sortOrder) {
        if (sortOrder != null && sortOrder < 1) {
            throw new BadRequestException(
                    "Sort order must be greater than 0");
        }
    }
}
