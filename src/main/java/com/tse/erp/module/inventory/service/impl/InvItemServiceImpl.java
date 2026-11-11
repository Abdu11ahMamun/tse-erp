package com.tse.erp.module.inventory.service.impl;

import com.tse.erp.common.AppConstants;
import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.BusinessGroup;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.entity.User;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.admin.repository.UserRepository;
import com.tse.erp.module.asset.entity.AssetUom;
import com.tse.erp.module.asset.repository.AssetUomRepository;
import com.tse.erp.module.hr.entity.Employee;
import com.tse.erp.module.hr.repository.EmployeeRepository;
import com.tse.erp.module.inventory.dto.InvItemAttributeDtlsDto;
import com.tse.erp.module.inventory.dto.InvItemRequestDto;
import com.tse.erp.module.inventory.dto.InvItemResponseDto;
import com.tse.erp.module.inventory.entity.*;
import com.tse.erp.module.inventory.repository.*;
import com.tse.erp.module.inventory.service.InvItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class InvItemServiceImpl implements InvItemService {

    private final InvItemRepository itemRepository;
    private final InvItemAttributeDtlsRepository itemAttributeDtlsRepository;
    private final InvItemCategoryRepository itemCategoryRepository;
    private final InvItemBrandRepository itemBrandRepository;
    private final AssetUomRepository assetUomRepository;
    private final InvItemStoreRepository itemStoreRepository;
    private final InvItemAttributeRepository itemAttributeRepository;
    private final BusinessGroupRepository businessGroupRepository;
    private final BusinessUnitRepository businessUnitRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public List<InvItemResponseDto> getAllItems(
            Long bgId, Long buId, Long categoryId, Long brandId, Long uomId, String storeId, String itemName, String itemCode, Integer status) {
        return itemRepository.searchItems(bgId, buId, categoryId, brandId, uomId, storeId, itemName, itemCode, status)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public InvItemResponseDto getItemById(Long id) {
        return toResponse(findItem(id));
    }

    @Override
    public String generateNextItemCode(Long buId) {
        if (buId == null) {
            throw new BadRequestException("Business Unit ID is required to generate next Item Code");
        }
        long count = itemRepository.countByBuId(buId);
        long next = count + 1;
        String code = String.format("ITEM-%05d", next);
        while (itemRepository.existsByBuIdAndItemCodeIgnoreCase(buId, code)) {
            next++;
            code = String.format("ITEM-%05d", next);
        }
        return code;
    }

    @Override
    @Transactional
    public InvItemResponseDto createItem(InvItemRequestDto request) {
        validateParents(request.getBgId(), request.getBuId());

        if (request.getItemName() == null || request.getItemName().trim().isEmpty()) {
            throw new BadRequestException("Item Name is required");
        }
        String itemName = request.getItemName().trim();
        if (itemName.length() > 150) {
            throw new BadRequestException("Item Name cannot exceed 150 characters");
        }

        if (itemRepository.existsByBuIdAndItemNameIgnoreCase(request.getBuId(), itemName)) {
            throw new DuplicateResourceException("Item Name '" + itemName + "' already exists for this Business Unit");
        }

        String itemCode = request.getItemCode();
        if (itemCode == null || itemCode.trim().isEmpty()) {
            itemCode = generateNextItemCode(request.getBuId());
        } else {
            itemCode = itemCode.trim();
            if (itemCode.length() > 50) {
                throw new BadRequestException("Item Code cannot exceed 50 characters");
            }
            if (itemRepository.existsByBuIdAndItemCodeIgnoreCase(request.getBuId(), itemCode)) {
                throw new DuplicateResourceException("Item Code '" + itemCode + "' already exists for this Business Unit");
            }
        }

        validateCategory(request.getCategoryId(), request.getBuId());
        validateBrand(request.getBrandId(), request.getBuId());
        validateUom(request.getUomId(), request.getBuId());
        String normalizedStoreId = normalizeStoreIds(request.getStoreId());
        validateStore(normalizedStoreId, request.getBuId());
        StatusUtil.validate(request.getStatus());

        Integer isWarranty = (request.getIsWarrantyApplicable() != null && request.getIsWarrantyApplicable() == 1) ? 1 : 0;
        Integer warrantyDay = isWarranty == 1 ? request.getWarrantyDay() : null;
        String warrantyCriteria = request.getWarrantyCriteria() != null ? request.getWarrantyCriteria().trim() : null;

        validateDetailRows(request.getAttributeDetails(), request.getBuId());

        InvItem item = InvItem.builder()
                .bgId(request.getBgId())
                .buId(request.getBuId())
                .itemName(itemName)
                .itemCode(itemCode)
                .categoryId(request.getCategoryId())
                .brandId(request.getBrandId())
                .uomId(request.getUomId())
                .storeId(normalizedStoreId)
                .itemImage(request.getItemImage())
                .isWarrantyApplicable(isWarranty)
                .warrantyDay(warrantyDay)
                .warrantyCriteria(warrantyCriteria)
                .build();

        item.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        item.setCreatedApp(AppConstants.APP_NAME);

        InvItem savedItem = itemRepository.save(item);

        if (request.getAttributeDetails() != null && !request.getAttributeDetails().isEmpty()) {
            List<InvItemAttributeDtls> detailsToSave = request.getAttributeDetails().stream()
                    .map(dto -> {
                        InvItemAttributeDtls d = toDetailEntity(dto, savedItem.getId(), savedItem.getBgId(), savedItem.getBuId());
                        d.setCreatedApp(AppConstants.APP_NAME);
                        return d;
                    })
                    .toList();
            itemAttributeDtlsRepository.saveAll(detailsToSave);
        }

        return toResponse(savedItem);
    }

    @Override
    @Transactional
    public InvItemResponseDto updateItem(Long id, InvItemRequestDto request) {
        InvItem existing = findItem(id);

        validateParents(request.getBgId(), request.getBuId());

        if (request.getItemName() == null || request.getItemName().trim().isEmpty()) {
            throw new BadRequestException("Item Name is required");
        }
        String itemName = request.getItemName().trim();
        if (itemName.length() > 150) {
            throw new BadRequestException("Item Name cannot exceed 150 characters");
        }

        itemRepository.findByBuIdAndItemNameIgnoreCase(request.getBuId(), itemName)
                .ifPresent(other -> {
                    if (!other.getId().equals(id)) {
                        throw new DuplicateResourceException("Item Name '" + itemName + "' already exists for this Business Unit");
                    }
                });

        if (request.getItemCode() == null || request.getItemCode().trim().isEmpty()) {
            throw new BadRequestException("Item Code cannot be empty");
        }
        String itemCode = request.getItemCode().trim();
        if (itemCode.length() > 50) {
            throw new BadRequestException("Item Code cannot exceed 50 characters");
        }

        itemRepository.findByBuIdAndItemCodeIgnoreCase(request.getBuId(), itemCode)
                .ifPresent(other -> {
                    if (!other.getId().equals(id)) {
                        throw new DuplicateResourceException("Item Code '" + itemCode + "' already exists for this Business Unit");
                    }
                });

        validateCategory(request.getCategoryId(), request.getBuId());
        validateBrand(request.getBrandId(), request.getBuId());
        validateUom(request.getUomId(), request.getBuId());
        String normalizedStoreId = normalizeStoreIds(request.getStoreId());
        validateStore(normalizedStoreId, request.getBuId());
        StatusUtil.validate(request.getStatus());

        Integer isWarranty = (request.getIsWarrantyApplicable() != null && request.getIsWarrantyApplicable() == 1) ? 1 : 0;
        Integer warrantyDay = isWarranty == 1 ? request.getWarrantyDay() : null;
        String warrantyCriteria = request.getWarrantyCriteria() != null ? request.getWarrantyCriteria().trim() : null;

        validateDetailRows(request.getAttributeDetails(), request.getBuId());

        existing.setBgId(request.getBgId());
        existing.setBuId(request.getBuId());
        existing.setItemName(itemName);
        existing.setItemCode(itemCode);
        existing.setCategoryId(request.getCategoryId());
        existing.setBrandId(request.getBrandId());
        existing.setUomId(request.getUomId());
        existing.setStoreId(normalizedStoreId);
        if (request.getItemImage() != null) {
            existing.setItemImage(request.getItemImage());
        }
        existing.setIsWarrantyApplicable(isWarranty);
        existing.setWarrantyDay(warrantyDay);
        existing.setWarrantyCriteria(warrantyCriteria);
        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }
        existing.setUpdatedApp(AppConstants.APP_NAME);

        InvItem savedItem = itemRepository.save(existing);

        // Synchronize Detail Rows
        List<InvItemAttributeDtls> currentDetails = itemAttributeDtlsRepository.findByItemIdOrderByIdAsc(id);
        Map<Long, InvItemAttributeDtls> existingMap = new HashMap<>();
        currentDetails.forEach(d -> existingMap.put(d.getId(), d));

        Set<Long> processedIds = new HashSet<>();
        List<InvItemAttributeDtls> toSave = new ArrayList<>();
        List<InvItemAttributeDtlsDto> incomingRows = request.getAttributeDetails() != null ? request.getAttributeDetails() : Collections.emptyList();

        for (InvItemAttributeDtlsDto dto : incomingRows) {
            if (dto.getId() != null) {
                InvItemAttributeDtls current = existingMap.get(dto.getId());
                if (current == null) {
                    throw new BadRequestException("Detail row with id " + dto.getId() + " does not belong to Item " + id);
                }
                current.setBgId(savedItem.getBgId());
                current.setBuId(savedItem.getBuId());
                current.setAttributeId(dto.getAttributeId());
                current.setPrice(dto.getPrice());
                current.setOpeningStock(dto.getOpeningStock());
                if (dto.getStatus() != null) {
                    current.setStatus(dto.getStatus());
                }
                current.setUpdatedApp(AppConstants.APP_NAME);
                toSave.add(current);
                processedIds.add(dto.getId());
            } else {
                InvItemAttributeDtls newDetail = toDetailEntity(dto, id, savedItem.getBgId(), savedItem.getBuId());
                newDetail.setCreatedApp(AppConstants.APP_NAME);
                toSave.add(newDetail);
            }
        }

        List<InvItemAttributeDtls> toDelete = currentDetails.stream()
                .filter(d -> !processedIds.contains(d.getId()))
                .toList();

        if (!toDelete.isEmpty()) {
            itemAttributeDtlsRepository.deleteAll(toDelete);
        }

        if (!toSave.isEmpty()) {
            itemAttributeDtlsRepository.saveAll(toSave);
        }

        return toResponse(savedItem);
    }

    @Override
    @Transactional
    public void deleteItem(Long id) {
        InvItem existing = findItem(id);
        List<InvItemAttributeDtls> details = itemAttributeDtlsRepository.findByItemIdOrderByIdAsc(id);
        if (!details.isEmpty()) {
            itemAttributeDtlsRepository.deleteAll(details);
        }
        itemRepository.delete(existing);
    }

    @Override
    public List<InvItemAttributeDtlsDto> getItemDetails(Long itemId) {
        findItem(itemId);
        return itemAttributeDtlsRepository.findByItemIdOrderByIdAsc(itemId)
                .stream()
                .map(this::toDetailDto)
                .toList();
    }

    @Override
    @Transactional
    public InvItemAttributeDtlsDto addItemDetail(Long itemId, InvItemAttributeDtlsDto detailDto) {
        InvItem item = findItem(itemId);
        validateDetailRow(detailDto, item.getBuId());

        InvItemAttributeDtls detail = toDetailEntity(detailDto, itemId, item.getBgId(), item.getBuId());
        detail.setCreatedApp(AppConstants.APP_NAME);
        InvItemAttributeDtls saved = itemAttributeDtlsRepository.save(detail);

        return toDetailDto(saved);
    }

    @Override
    @Transactional
    public InvItemAttributeDtlsDto updateItemDetail(Long itemId, Long detailId, InvItemAttributeDtlsDto detailDto) {
        InvItem item = findItem(itemId);
        InvItemAttributeDtls existing = itemAttributeDtlsRepository.findById(detailId)
                .orElseThrow(() -> new ResourceNotFoundException("Item detail not found with id: " + detailId));

        if (!itemId.equals(existing.getItemId())) {
            throw new BadRequestException("Detail row with id " + detailId + " does not belong to Item " + itemId);
        }

        validateDetailRow(detailDto, item.getBuId());

        existing.setBgId(item.getBgId());
        existing.setBuId(item.getBuId());
        existing.setAttributeId(detailDto.getAttributeId());
        existing.setPrice(detailDto.getPrice());
        existing.setOpeningStock(detailDto.getOpeningStock());
        if (detailDto.getStatus() != null) {
            existing.setStatus(detailDto.getStatus());
        }
        existing.setUpdatedApp(AppConstants.APP_NAME);

        return toDetailDto(itemAttributeDtlsRepository.save(existing));
    }

    @Override
    @Transactional
    public void deleteItemDetail(Long itemId, Long detailId) {
        InvItem item = findItem(itemId);
        InvItemAttributeDtls existing = itemAttributeDtlsRepository.findById(detailId)
                .orElseThrow(() -> new ResourceNotFoundException("Item detail not found with id: " + detailId));

        if (!itemId.equals(existing.getItemId())) {
            throw new BadRequestException("Detail row with id " + detailId + " does not belong to Item " + itemId);
        }

        itemAttributeDtlsRepository.delete(existing);
    }

    private InvItem findItem(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + id));
    }

    private void validateCategory(Long categoryId, Long buId) {
        if (categoryId == null) return;
        InvItemCategory cat = itemCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Item Category not found with id: " + categoryId));
        if (buId != null && !buId.equals(cat.getBuId())) {
            throw new BadRequestException("Selected Item Category does not belong to the same Business Unit");
        }
    }

    private void validateBrand(Long brandId, Long buId) {
        if (brandId == null) return;
        InvItemBrand brand = itemBrandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("Item Brand not found with id: " + brandId));
        if (buId != null && !buId.equals(brand.getBuId())) {
            throw new BadRequestException("Selected Item Brand does not belong to the same Business Unit");
        }
    }

    private void validateUom(Long uomId, Long buId) {
        if (uomId == null) return;
        AssetUom uom = assetUomRepository.findById(uomId)
                .orElseThrow(() -> new ResourceNotFoundException("Unit of Measure not found with id: " + uomId));
        if (buId != null && !buId.equals(uom.getBuId())) {
            throw new BadRequestException("Selected Unit of Measure does not belong to the same Business Unit");
        }
    }

    private String normalizeStoreIds(String storeId) {
        if (storeId == null || storeId.trim().isEmpty()) {
            return null;
        }
        List<String> list = Arrays.stream(storeId.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();
        return list.isEmpty() ? null : String.join(",", list);
    }

    private void validateStore(String storeId, Long buId) {
        if (storeId == null || storeId.trim().isEmpty()) return;
        String[] parts = storeId.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) continue;
            Long sId;
            try {
                sId = Long.parseLong(trimmed);
            } catch (NumberFormatException e) {
                throw new BadRequestException("Invalid Store ID: " + trimmed);
            }
            InvItemStore store = itemStoreRepository.findById(sId)
                    .orElseThrow(() -> new ResourceNotFoundException("Item Store not found with id: " + sId));
            if (buId != null && !buId.equals(store.getBuId())) {
                throw new BadRequestException("Selected Item Store '" + store.getStoreName() + "' does not belong to the same Business Unit");
            }
        }
    }

    private void validateDetailRows(List<InvItemAttributeDtlsDto> details, Long buId) {
        if (details == null || details.isEmpty()) return;
        Set<Long> seenAttributeIds = new HashSet<>();
        for (int i = 0; i < details.size(); i++) {
            InvItemAttributeDtlsDto d = details.get(i);
            validateDetailRow(d, buId);
            if (d.getAttributeId() != null) {
                if (!seenAttributeIds.add(d.getAttributeId())) {
                    throw new BadRequestException("Duplicate Attribute in row " + (i + 1) + ". Each attribute can only be added once.");
                }
            }
        }
    }

    private void validateDetailRow(InvItemAttributeDtlsDto d, Long buId) {
        if (d == null) return;
        if (d.getAttributeId() == null) {
            throw new BadRequestException("Attribute is required in detail row");
        }
        InvItemAttribute attr = itemAttributeRepository.findById(d.getAttributeId())
                .orElseThrow(() -> new ResourceNotFoundException("Item Attribute not found with id: " + d.getAttributeId()));
        if (buId != null && !buId.equals(attr.getBuId())) {
            throw new BadRequestException("Selected Item Attribute does not belong to the same Business Unit");
        }
        if (d.getPrice() != null && d.getPrice().signum() < 0) {
            throw new BadRequestException("Price cannot be negative");
        }
        if (d.getOpeningStock() != null && d.getOpeningStock().signum() < 0) {
            throw new BadRequestException("Opening Stock cannot be negative");
        }
    }

    private InvItemAttributeDtls toDetailEntity(InvItemAttributeDtlsDto dto, Long itemId, Long bgId, Long buId) {
        InvItemAttributeDtls entity = InvItemAttributeDtls.builder()
                .bgId(bgId)
                .buId(buId)
                .itemId(itemId)
                .attributeId(dto.getAttributeId())
                .price(dto.getPrice())
                .openingStock(dto.getOpeningStock())
                .build();
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        return entity;
    }

    private InvItemAttributeDtlsDto toDetailDto(InvItemAttributeDtls entity) {
        String attributeName = null;
        String attributeCode = null;
        if (entity.getAttributeId() != null) {
            Optional<InvItemAttribute> attrOpt = itemAttributeRepository.findById(entity.getAttributeId());
            if (attrOpt.isPresent()) {
                attributeName = attrOpt.get().getAttributeName();
                attributeCode = attrOpt.get().getAttributeCode();
            }
        }

        String createdUserName = entity.getCreatedUserId() != null
                ? userRepository.findById(entity.getCreatedUserId()).map(User::getUserName).orElse(null)
                : null;
        String createdEmpName = null;
        if (entity.getCreatedEmpId() != null) {
            Optional<Employee> empOpt = employeeRepository.findById(entity.getCreatedEmpId());
            if (empOpt.isPresent()) {
                Employee emp = empOpt.get();
                createdEmpName = (emp.getEmployeeCode() != null ? emp.getEmployeeCode() : "") + ": " + (emp.getEmployeeName() != null ? emp.getEmployeeName() : "");
            }
        }

        String updatedUserName = entity.getUpdatedUserId() != null
                ? userRepository.findById(entity.getUpdatedUserId()).map(User::getUserName).orElse(null)
                : null;
        String updatedEmpName = null;
        if (entity.getUpdatedEmpId() != null) {
            Optional<Employee> empOpt = employeeRepository.findById(entity.getUpdatedEmpId());
            if (empOpt.isPresent()) {
                Employee emp = empOpt.get();
                updatedEmpName = (emp.getEmployeeCode() != null ? emp.getEmployeeCode() : "") + ": " + (emp.getEmployeeName() != null ? emp.getEmployeeName() : "");
            }
        }

        return InvItemAttributeDtlsDto.builder()
                .id(entity.getId())
                .bgId(entity.getBgId())
                .buId(entity.getBuId())
                .itemId(entity.getItemId())
                .attributeId(entity.getAttributeId())
                .attributeName(attributeName)
                .attributeCode(attributeCode)
                .price(entity.getPrice())
                .openingStock(entity.getOpeningStock())
                .status(entity.getStatus())
                .createdUserId(entity.getCreatedUserId())
                .createdUserName(createdUserName)
                .createdEmpId(entity.getCreatedEmpId())
                .createdEmpName(createdEmpName)
                .createdDatetime(entity.getCreatedDatetime())
                .createdApp(entity.getCreatedApp())
                .updatedUserId(entity.getUpdatedUserId())
                .updatedUserName(updatedUserName)
                .updatedEmpId(entity.getUpdatedEmpId())
                .updatedEmpName(updatedEmpName)
                .updatedDatetime(entity.getUpdatedDatetime())
                .updatedApp(entity.getUpdatedApp())
                .build();
    }

    private InvItemResponseDto toResponse(InvItem entity) {
        String groupName = businessGroupRepository.findById(entity.getBgId())
                .map(BusinessGroup::getGroupName)
                .orElse(null);

        String businessUnitName = businessUnitRepository.findById(entity.getBuId())
                .map(BusinessUnit::getBusinessUnit)
                .orElse(null);

        String categoryName = null;
        String categoryCode = null;
        if (entity.getCategoryId() != null) {
            Optional<InvItemCategory> catOpt = itemCategoryRepository.findById(entity.getCategoryId());
            if (catOpt.isPresent()) {
                categoryName = catOpt.get().getCategoryName();
                categoryCode = catOpt.get().getCategoryCode();
            }
        }

        String brandName = null;
        String brandCode = null;
        if (entity.getBrandId() != null) {
            Optional<InvItemBrand> brandOpt = itemBrandRepository.findById(entity.getBrandId());
            if (brandOpt.isPresent()) {
                brandName = brandOpt.get().getBrandName();
                brandCode = brandOpt.get().getBrandCode();
            }
        }

        String uomName = null;
        if (entity.getUomId() != null) {
            uomName = assetUomRepository.findById(entity.getUomId())
                    .map(AssetUom::getUnitName)
                    .orElse(null);
        }

        List<Long> storeIds = new ArrayList<>();
        List<String> storeNames = new ArrayList<>();
        List<String> storeCodes = new ArrayList<>();

        if (entity.getStoreId() != null && !entity.getStoreId().trim().isEmpty()) {
            String[] parts = entity.getStoreId().split(",");
            for (String part : parts) {
                String trimmed = part.trim();
                if (trimmed.isEmpty()) continue;
                try {
                    Long sId = Long.parseLong(trimmed);
                    storeIds.add(sId);
                    itemStoreRepository.findById(sId).ifPresent(s -> {
                        if (s.getStoreName() != null && !s.getStoreName().isEmpty()) {
                            storeNames.add(s.getStoreName());
                        }
                        if (s.getStoreCode() != null && !s.getStoreCode().isEmpty()) {
                            storeCodes.add(s.getStoreCode());
                        }
                    });
                } catch (NumberFormatException ignored) {}
            }
        }
        String storeName = String.join(", ", storeNames);
        String storeCode = String.join(", ", storeCodes);

        String createdUserName = entity.getCreatedUserId() != null
                ? userRepository.findById(entity.getCreatedUserId()).map(User::getUserName).orElse(null)
                : null;
        String createdEmpName = null;
        if (entity.getCreatedEmpId() != null) {
            Optional<Employee> empOpt = employeeRepository.findById(entity.getCreatedEmpId());
            if (empOpt.isPresent()) {
                Employee emp = empOpt.get();
                createdEmpName = (emp.getEmployeeCode() != null ? emp.getEmployeeCode() : "") + ": " + (emp.getEmployeeName() != null ? emp.getEmployeeName() : "");
            }
        }

        String updatedUserName = entity.getUpdatedUserId() != null
                ? userRepository.findById(entity.getUpdatedUserId()).map(User::getUserName).orElse(null)
                : null;
        String updatedEmpName = null;
        if (entity.getUpdatedEmpId() != null) {
            Optional<Employee> empOpt = employeeRepository.findById(entity.getUpdatedEmpId());
            if (empOpt.isPresent()) {
                Employee emp = empOpt.get();
                updatedEmpName = (emp.getEmployeeCode() != null ? emp.getEmployeeCode() : "") + ": " + (emp.getEmployeeName() != null ? emp.getEmployeeName() : "");
            }
        }

        List<InvItemAttributeDtlsDto> attributeDetails = itemAttributeDtlsRepository.findByItemIdOrderByIdAsc(entity.getId())
                .stream()
                .map(this::toDetailDto)
                .toList();

        return InvItemResponseDto.builder()
                .id(entity.getId())
                .bgId(entity.getBgId())
                .groupName(groupName)
                .buId(entity.getBuId())
                .businessUnitName(businessUnitName)
                .itemName(entity.getItemName())
                .itemCode(entity.getItemCode())
                .categoryId(entity.getCategoryId())
                .categoryName(categoryName)
                .categoryCode(categoryCode)
                .brandId(entity.getBrandId())
                .brandName(brandName)
                .brandCode(brandCode)
                .uomId(entity.getUomId())
                .uomName(uomName)
                .storeId(entity.getStoreId())
                .storeIds(storeIds)
                .storeNames(storeNames)
                .storeName(storeName.isEmpty() ? null : storeName)
                .storeCode(storeCode.isEmpty() ? null : storeCode)
                .itemImage(entity.getItemImage())
                .isWarrantyApplicable(entity.getIsWarrantyApplicable())
                .warrantyDay(entity.getWarrantyDay())
                .warrantyCriteria(entity.getWarrantyCriteria())
                .status(entity.getStatus())
                .createdUserId(entity.getCreatedUserId())
                .createdUserName(createdUserName)
                .createdEmpId(entity.getCreatedEmpId())
                .createdEmpName(createdEmpName)
                .createdDatetime(entity.getCreatedDatetime())
                .createdApp(entity.getCreatedApp())
                .updatedUserId(entity.getUpdatedUserId())
                .updatedUserName(updatedUserName)
                .updatedEmpId(entity.getUpdatedEmpId())
                .updatedEmpName(updatedEmpName)
                .updatedDatetime(entity.getUpdatedDatetime())
                .updatedApp(entity.getUpdatedApp())
                .attributeDetails(attributeDetails)
                .build();
    }

    private void validateParents(Long bgId, Long buId) {
        if (bgId == null) {
            throw new BadRequestException("Business Group id cannot be empty");
        }
        if (buId == null) {
            throw new BadRequestException("Business Unit id cannot be empty");
        }

        businessGroupRepository.findById(bgId)
                .orElseThrow(() -> new ResourceNotFoundException("Business Group not found with id: " + bgId));

        BusinessUnit bu = businessUnitRepository.findById(buId)
                .orElseThrow(() -> new ResourceNotFoundException("Business Unit not found with id: " + buId));

        if (!Integer.valueOf(1).equals(bu.getStatus())) {
            throw new BadRequestException("Selected Business Unit is not active");
        }

        if (!bgId.equals(bu.getBgId())) {
            throw new BadRequestException("Business Unit does not belong to selected Business Group");
        }
    }
}

