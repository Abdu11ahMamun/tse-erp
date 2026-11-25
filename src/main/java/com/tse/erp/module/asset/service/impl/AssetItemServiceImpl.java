package com.tse.erp.module.asset.service.impl;

import com.tse.erp.common.AppConstants;
import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.asset.entity.AssetGroup;
import com.tse.erp.module.asset.entity.AssetItem;
import com.tse.erp.module.asset.entity.AssetItemSet;
import com.tse.erp.module.asset.entity.AssetUom;
import com.tse.erp.module.asset.entity.DepriCategory;
import com.tse.erp.module.asset.repository.AssetGroupRepository;
import com.tse.erp.module.asset.repository.AssetItemRepository;
import com.tse.erp.module.asset.repository.AssetItemSetRepository;
import com.tse.erp.module.asset.repository.AssetItemSpecRepository;
import com.tse.erp.module.asset.repository.AssetUomRepository;
import com.tse.erp.module.asset.repository.DepriCategoryRepository;
import com.tse.erp.module.asset.service.AssetItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AssetItemServiceImpl implements AssetItemService {

    private final AssetItemRepository assetItemRepository;
    private final AssetItemSetRepository assetItemSetRepository;
    private final AssetItemSpecRepository assetItemSpecRepository;
    private final BusinessGroupRepository businessGroupRepository;
    private final BusinessUnitRepository businessUnitRepository;
    private final AssetGroupRepository assetGroupRepository;
    private final DepriCategoryRepository depriCategoryRepository;
    private final AssetUomRepository assetUomRepository;

    @Override
    public List<AssetItem> getAllAssetItems(Long buId) {
        List<AssetItem> items = (buId == null)
                ? assetItemRepository.findAllByOrderByIdDesc()
                : assetItemRepository.findByBuIdOrderByIdDesc(buId);
        for (AssetItem item : items) {
            item.setItemSets(assetItemSetRepository.findByItemIdOrderBySortOrderAscIdAsc(item.getId()));
        }
        return items;
    }

    @Override
    public AssetItem getAssetItemById(Long id) {
        AssetItem item = assetItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Asset Item not found with id: " + id));
        item.setItemSets(assetItemSetRepository.findByItemIdOrderBySortOrderAscIdAsc(id));
        return item;
    }

    @Override
    public List<AssetItemSet> getItemSetsByItemId(Long itemId) {
        return assetItemSetRepository.findByItemIdOrderBySortOrderAscIdAsc(itemId);
    }

    @Override
    @Transactional
    public AssetItem createAssetItem(AssetItem item) {
        validateParents(item.getBgId(), item.getBuId());
        validateItemName(item.getItemName());
        validateItemGroup(item.getItemGroupId(), item.getBuId());
        validateDepriCategory(item.getAssetDepriCatId(), item.getBuId());
        validateUom(item.getUomId(), item.getBuId());
        validateItemPieceOrSet(item.getItemPieceOrSet());
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

        List<AssetItemSet> incomingSets = item.getItemSets();

        item.setId(null);
        item.setItemName(name);
        item.setItemPieceOrSet(normalizePieceOrSet(item.getItemPieceOrSet()));
        AssetItem saved = assetItemRepository.save(item);

        if ("Set".equalsIgnoreCase(saved.getItemPieceOrSet()) && incomingSets != null && !incomingSets.isEmpty()) {
            List<AssetItemSet> toSave = new ArrayList<>();
            for (int i = 0; i < incomingSets.size(); i++) {
                AssetItemSet setRow = incomingSets.get(i);
                if (setRow.getItemName() == null || setRow.getItemName().trim().isEmpty()) {
                    continue;
                }
                setRow.setId(null);
                setRow.setBgId(saved.getBgId());
                setRow.setBuId(saved.getBuId());
                setRow.setItemId(saved.getId());
                setRow.setItemName(setRow.getItemName().trim());
                setRow.setSortOrder(setRow.getSortOrder() != null ? setRow.getSortOrder() : BigDecimal.valueOf(i + 1));
                if (setRow.getStatus() == null) {
                    setRow.setStatus(1);
                }
                setRow.setCreatedApp(AppConstants.APP_NAME);
                toSave.add(setRow);
            }
            if (!toSave.isEmpty()) {
                assetItemSetRepository.saveAll(toSave);
            }
            saved.setItemSets(toSave);
        } else {
            saved.setItemSets(Collections.emptyList());
        }

        return saved;
    }

    @Override
    @Transactional
    public AssetItem updateAssetItem(Long id, AssetItem item) {
        AssetItem existing = getAssetItemById(id);

        validateParents(item.getBgId(), item.getBuId());
        validateItemName(item.getItemName());
        validateItemGroup(item.getItemGroupId(), item.getBuId());
        validateDepriCategory(item.getAssetDepriCatId(), item.getBuId());
        validateUom(item.getUomId(), item.getBuId());
        validateItemPieceOrSet(item.getItemPieceOrSet());
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
        existing.setItemPieceOrSet(normalizePieceOrSet(item.getItemPieceOrSet()));
        if (item.getStatus() != null) {
            existing.setStatus(item.getStatus());
        }

        AssetItem saved = assetItemRepository.save(existing);

        // Synchronize AssetItemSet rows
        List<AssetItemSet> currentDbSets = assetItemSetRepository.findByItemIdOrderBySortOrderAscIdAsc(id);
        Map<Long, AssetItemSet> dbSetMap = new HashMap<>();
        for (AssetItemSet s : currentDbSets) {
            if (s.getId() != null) {
                dbSetMap.put(s.getId(), s);
            }
        }

        List<AssetItemSet> incomingSets = item.getItemSets();
        Set<Long> incomingIds = new HashSet<>();
        List<AssetItemSet> toSave = new ArrayList<>();

        if ("Set".equalsIgnoreCase(saved.getItemPieceOrSet()) && incomingSets != null) {
            for (int i = 0; i < incomingSets.size(); i++) {
                AssetItemSet row = incomingSets.get(i);
                if (row.getItemName() == null || row.getItemName().trim().isEmpty()) {
                    continue;
                }
                if (row.getId() != null && dbSetMap.containsKey(row.getId())) {
                    incomingIds.add(row.getId());
                    AssetItemSet existingSet = dbSetMap.get(row.getId());
                    existingSet.setBgId(saved.getBgId());
                    existingSet.setBuId(saved.getBuId());
                    existingSet.setItemId(saved.getId());
                    existingSet.setItemName(row.getItemName().trim());
                    existingSet.setSortOrder(row.getSortOrder() != null ? row.getSortOrder() : BigDecimal.valueOf(i + 1));
                    if (row.getStatus() != null) {
                        existingSet.setStatus(row.getStatus());
                    }
                    existingSet.setUpdatedApp(AppConstants.APP_NAME);
                    toSave.add(existingSet);
                } else {
                    AssetItemSet newSet = AssetItemSet.builder()
                            .bgId(saved.getBgId())
                            .buId(saved.getBuId())
                            .itemId(saved.getId())
                            .itemName(row.getItemName().trim())
                            .sortOrder(row.getSortOrder() != null ? row.getSortOrder() : BigDecimal.valueOf(i + 1))
                            .build();
                    newSet.setStatus(row.getStatus() != null ? row.getStatus() : 1);
                    newSet.setCreatedApp(AppConstants.APP_NAME);
                    toSave.add(newSet);
                }
            }
        }

        List<AssetItemSet> toDelete = new ArrayList<>();
        for (AssetItemSet dbSet : currentDbSets) {
            if (!incomingIds.contains(dbSet.getId())) {
                toDelete.add(dbSet);
            }
        }
        if (!toDelete.isEmpty()) {
            assetItemSetRepository.deleteAll(toDelete);
        }
        if (!toSave.isEmpty()) {
            assetItemSetRepository.saveAll(toSave);
        }

        saved.setItemSets(assetItemSetRepository.findByItemIdOrderBySortOrderAscIdAsc(id));
        return saved;
    }

    @Override
    @Transactional
    public void deleteAssetItem(Long id) {
        AssetItem existing = getAssetItemById(id);
        if (assetItemSpecRepository.existsByItemNameId(id)) {
            throw new BadRequestException(
                    "Cannot delete: Specifications are mapped to this Asset Item");
        }
        assetItemSetRepository.deleteByItemId(id);
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

    private void validateItemPieceOrSet(String itemPieceOrSet) {
        if (itemPieceOrSet != null && !itemPieceOrSet.trim().isEmpty()) {
            String val = itemPieceOrSet.trim();
            if (!val.equalsIgnoreCase("Piece") && !val.equalsIgnoreCase("Set")) {
                throw new BadRequestException("Item Piece or Set must be either 'Piece' or 'Set'");
            }
        }
    }

    private String normalizePieceOrSet(String itemPieceOrSet) {
        if (itemPieceOrSet == null || itemPieceOrSet.trim().isEmpty()) {
            return "Piece";
        }
        return itemPieceOrSet.trim().equalsIgnoreCase("Set") ? "Set" : "Piece";
    }
}
