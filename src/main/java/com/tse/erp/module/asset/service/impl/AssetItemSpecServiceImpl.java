package com.tse.erp.module.asset.service.impl;

import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.asset.entity.AssetItem;
import com.tse.erp.module.asset.entity.AssetItemSpec;
import com.tse.erp.module.asset.repository.AssetItemRepository;
import com.tse.erp.module.asset.repository.AssetItemSpecRepository;
import com.tse.erp.module.asset.service.AssetItemSpecService;
import com.tse.erp.module.common.entity.LookupType;
import com.tse.erp.module.common.entity.LookupValue;
import com.tse.erp.module.common.repository.LookupTypeRepository;
import com.tse.erp.module.common.repository.LookupValueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetItemSpecServiceImpl
        implements AssetItemSpecService {

    private final AssetItemSpecRepository assetItemSpecRepository;
    private final BusinessGroupRepository businessGroupRepository;
    private final BusinessUnitRepository businessUnitRepository;
    private final AssetItemRepository assetItemRepository;
    private final LookupTypeRepository lookupTypeRepository;
    private final LookupValueRepository lookupValueRepository;

    @Override
    public List<AssetItemSpec> getAllAssetItemSpecs(
            Long buId, Long itemNameId) {
        if (itemNameId != null) {
            return assetItemSpecRepository
                    .findByItemNameIdOrderBySortOrderAscIdAsc(itemNameId);
        }
        if (buId != null) {
            return assetItemSpecRepository.findByBuIdOrderByIdDesc(buId);
        }
        return assetItemSpecRepository.findAllByOrderByIdDesc();
    }

    @Override
    public AssetItemSpec getAssetItemSpecById(Long id) {
        return assetItemSpecRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Asset Item Specification not found with id: " + id));
    }

    @Override
    public AssetItemSpec createAssetItemSpec(AssetItemSpec spec) {
        validateParents(spec.getBgId(), spec.getBuId());
        validateItem(spec.getItemNameId(), spec.getBuId());
        validateSpecName(spec.getSpecName(), spec.getBuId());
        validateSortOrder(spec.getSortOrder());
        StatusUtil.validate(spec.getStatus());

        String specName = spec.getSpecName().trim();
        boolean exists = !assetItemSpecRepository
                .findByItemNameIdAndSpecNameIgnoreCase(
                        spec.getItemNameId(), specName)
                .isEmpty();
        if (exists) {
            throw new DuplicateResourceException(
                    "Specification already mapped to this Asset Item: "
                            + specName);
        }

        spec.setId(null);
        spec.setSpecName(specName);
        return assetItemSpecRepository.save(spec);
    }

    @Override
    public AssetItemSpec updateAssetItemSpec(
            Long id, AssetItemSpec spec) {
        AssetItemSpec existing = getAssetItemSpecById(id);

        validateParents(spec.getBgId(), spec.getBuId());
        validateItem(spec.getItemNameId(), spec.getBuId());
        validateSpecName(spec.getSpecName(), spec.getBuId());
        validateSortOrder(spec.getSortOrder());
        StatusUtil.validate(spec.getStatus());

        String specName = spec.getSpecName().trim();
        boolean duplicateExists = assetItemSpecRepository
                .findByItemNameIdAndSpecNameIgnoreCase(
                        spec.getItemNameId(), specName)
                .stream()
                .anyMatch(x -> !x.getId().equals(id));
        if (duplicateExists) {
            throw new DuplicateResourceException(
                    "Specification already mapped to this Asset Item: "
                            + specName);
        }

        existing.setBgId(spec.getBgId());
        existing.setBuId(spec.getBuId());
        existing.setItemNameId(spec.getItemNameId());
        existing.setSpecName(specName);
        existing.setSortOrder(spec.getSortOrder());
        if (spec.getStatus() != null) {
            existing.setStatus(spec.getStatus());
        }

        return assetItemSpecRepository.save(existing);
    }

    @Override
    public void deleteAssetItemSpec(Long id) {
        AssetItemSpec existing = getAssetItemSpecById(id);
        assetItemSpecRepository.delete(existing);
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

    private void validateItem(Long itemId, Long buId) {
        if (itemId == null) {
            throw new BadRequestException("Asset item cannot be empty");
        }

        AssetItem item = assetItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Asset Item not found with id: " + itemId));

        if (!Integer.valueOf(1).equals(item.getStatus())) {
            throw new BadRequestException(
                    "Selected Asset Item is not active");
        }
        if (!buId.equals(item.getBuId())) {
            throw new BadRequestException(
                    "Asset Item does not belong to selected Business Unit");
        }
    }

    private void validateSpecName(String specName, Long buId) {
        if (specName == null || specName.trim().isEmpty()) {
            throw new BadRequestException(
                    "Specification cannot be empty");
        }

        String value = specName.trim();
        List<LookupType> types = lookupTypeRepository
                .findByBuIdAndTypeNameIgnoreCase(
                        buId, "Asset Specification");
        if (types.isEmpty()) {
            throw new BadRequestException(
                    "Lookup Type 'Asset Specification' is not configured for this Business Unit");
        }

        List<LookupValue> values = lookupValueRepository
                .findByTypeIdAndValueIgnoreCase(types.get(0).getId(), value);
        if (values.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Specification not found: " + value);
        }
        if (!Integer.valueOf(1).equals(values.get(0).getStatus())) {
            throw new BadRequestException(
                    "Selected Specification is not active");
        }
    }

    private void validateSortOrder(Integer sortOrder) {
        if (sortOrder != null && sortOrder < 1) {
            throw new BadRequestException(
                    "Sort order must be greater than 0");
        }
    }
}
