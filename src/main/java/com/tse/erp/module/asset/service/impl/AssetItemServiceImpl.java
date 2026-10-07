package com.tse.erp.module.asset.service.impl;

import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.asset.entity.AssetGroup;
import com.tse.erp.module.asset.entity.AssetItem;
import com.tse.erp.module.asset.entity.AssetUom;
import com.tse.erp.module.asset.entity.DepriCategory;
import com.tse.erp.module.asset.repository.AssetGroupRepository;
import com.tse.erp.module.asset.repository.AssetItemRepository;
import com.tse.erp.module.asset.repository.AssetItemSpecRepository;
import com.tse.erp.module.asset.repository.AssetUomRepository;
import com.tse.erp.module.asset.repository.DepriCategoryRepository;
import com.tse.erp.module.asset.service.AssetItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetItemServiceImpl implements AssetItemService {

    private final AssetItemRepository assetItemRepository;
    private final AssetItemSpecRepository assetItemSpecRepository;
    private final BusinessGroupRepository businessGroupRepository;
    private final BusinessUnitRepository businessUnitRepository;
    private final AssetGroupRepository assetGroupRepository;
    private final DepriCategoryRepository depriCategoryRepository;
    private final AssetUomRepository assetUomRepository;

    @Override
    public List<AssetItem> getAllAssetItems(Long buId) {
        if (buId == null) {
            return assetItemRepository.findAllByOrderByIdDesc();
        }
        return assetItemRepository.findByBuIdOrderByIdDesc(buId);
    }

    @Override
    public AssetItem getAssetItemById(Long id) {
        return assetItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Asset Item not found with id: " + id));
    }

    @Override
    public AssetItem createAssetItem(AssetItem item) {
        validateParents(item.getBgId(), item.getBuId());
        validateItemName(item.getItemName());
        validateItemGroup(item.getItemGroupId(), item.getBuId());
        validateDepriCategory(item.getAssetDepriCatId(), item.getBuId());
        validateUom(item.getUomId(), item.getBuId());
        StatusUtil.validate(item.getStatus());

        String name = item.getItemName().trim();
        boolean exists = !assetItemRepository
                .findByItemGroupIdAndItemNameIgnoreCase(
                        item.getItemGroupId(), name)
                .isEmpty();
        if (exists) {
            throw new DuplicateResourceException(
                    "Item name already exists in this Asset Group: " + name);
        }

        item.setId(null);
        item.setItemName(name);
        return assetItemRepository.save(item);
    }

    @Override
    public AssetItem updateAssetItem(Long id, AssetItem item) {
        AssetItem existing = getAssetItemById(id);

        validateParents(item.getBgId(), item.getBuId());
        validateItemName(item.getItemName());
        validateItemGroup(item.getItemGroupId(), item.getBuId());
        validateDepriCategory(item.getAssetDepriCatId(), item.getBuId());
        validateUom(item.getUomId(), item.getBuId());
        StatusUtil.validate(item.getStatus());

        String name = item.getItemName().trim();
        boolean duplicateExists = assetItemRepository
                .findByItemGroupIdAndItemNameIgnoreCase(
                        item.getItemGroupId(), name)
                .stream()
                .anyMatch(x -> !x.getId().equals(id));
        if (duplicateExists) {
            throw new DuplicateResourceException(
                    "Item name already exists in this Asset Group: " + name);
        }

        existing.setBgId(item.getBgId());
        existing.setBuId(item.getBuId());
        existing.setItemName(name);
        existing.setItemGroupId(item.getItemGroupId());
        existing.setAssetDepriCatId(item.getAssetDepriCatId());
        existing.setUomId(item.getUomId());
        if (item.getStatus() != null) {
            existing.setStatus(item.getStatus());
        }

        return assetItemRepository.save(existing);
    }

    @Override
    public void deleteAssetItem(Long id) {
        AssetItem existing = getAssetItemById(id);
        if (assetItemSpecRepository.existsByItemNameId(id)) {
            throw new BadRequestException(
                    "Cannot delete: Specifications are mapped to this Asset Item");
        }
        assetItemRepository.delete(existing);
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

    private void validateItemName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new BadRequestException("Item name cannot be empty");
        }
        if (name.trim().length() < 2) {
            throw new BadRequestException(
                    "Item name must be at least 2 characters");
        }
        if (name.trim().length() > 100) {
            throw new BadRequestException(
                    "Item name cannot exceed 100 characters");
        }
    }

    private void validateItemGroup(Long groupId, Long buId) {
        if (groupId == null) {
            throw new BadRequestException("Item group cannot be empty");
        }

        AssetGroup group = assetGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Asset Group not found with id: " + groupId));

        if (!Integer.valueOf(1).equals(group.getStatus())) {
            throw new BadRequestException(
                    "Selected Asset Group is not active");
        }
        if (!buId.equals(group.getBuId())) {
            throw new BadRequestException(
                    "Asset Group does not belong to selected Business Unit");
        }
    }

    private void validateDepriCategory(Long categoryId, Long buId) {
        if (categoryId == null) {
            throw new BadRequestException(
                    "Depreciation category cannot be empty");
        }

        DepriCategory category = depriCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Depreciation Category not found with id: "
                                + categoryId));

        if (!Integer.valueOf(1).equals(category.getStatus())) {
            throw new BadRequestException(
                    "Selected Depreciation Category is not active");
        }
        if (!buId.equals(category.getBuId())) {
            throw new BadRequestException(
                    "Depreciation Category does not belong to selected Business Unit");
        }
    }

    private void validateUom(Long uomId, Long buId) {
        if (uomId == null) {
            throw new BadRequestException("UOM cannot be empty");
        }

        AssetUom uom = assetUomRepository.findById(uomId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "UOM not found with id: " + uomId));

        if (!Integer.valueOf(1).equals(uom.getStatus())) {
            throw new BadRequestException("Selected UOM is not active");
        }
        if (!buId.equals(uom.getBuId())) {
            throw new BadRequestException(
                    "UOM does not belong to selected Business Unit");
        }
    }
}
