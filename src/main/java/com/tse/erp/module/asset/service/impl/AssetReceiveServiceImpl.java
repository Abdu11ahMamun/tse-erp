package com.tse.erp.module.asset.service.impl;

import com.tse.erp.common.StatusUtil;
import com.tse.erp.common.ValidationUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.asset.dto.AssetReceiveDto;
import com.tse.erp.module.asset.dto.ReceiveSpecDto;
import com.tse.erp.module.asset.entity.AssetItem;
import com.tse.erp.module.asset.entity.AssetLocation;
import com.tse.erp.module.asset.entity.AssetReceive;
import com.tse.erp.module.asset.entity.AssetReceiveSpec;
import com.tse.erp.module.asset.entity.Custodian;
import com.tse.erp.module.asset.entity.CustodianDtl;
import com.tse.erp.module.asset.entity.DepriCategory;
import com.tse.erp.module.asset.repository.AssetItemRepository;
import com.tse.erp.module.asset.repository.AssetLocationRepository;
import com.tse.erp.module.asset.repository.AssetReceiveRepository;
import com.tse.erp.module.asset.repository.AssetReceiveSpecRepository;
import com.tse.erp.module.asset.repository.AssetUomRepository;
import com.tse.erp.module.asset.repository.CustodianDtlRepository;
import com.tse.erp.module.asset.repository.CustodianRepository;
import com.tse.erp.module.asset.repository.DepriCategoryRepository;
import com.tse.erp.module.asset.service.AssetReceiveService;
import com.tse.erp.module.common.validation.CommonLookupValidator;
import com.tse.erp.module.hr.entity.Department;
import com.tse.erp.module.hr.repository.DepartmentGroupRepository;
import com.tse.erp.module.hr.repository.DepartmentRepository;
import com.tse.erp.module.inventory.entity.Supplier;
import com.tse.erp.module.inventory.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AssetReceiveServiceImpl implements AssetReceiveService {

    private final AssetItemRepository assetItemRepository;
    private final DepriCategoryRepository depriCategoryRepository;
    private final AssetUomRepository assetUomRepository;
    private final SupplierRepository supplierRepository;
    private final CustodianRepository custodianRepository;
    private final CustodianDtlRepository custodianDtlRepository;
    private final AssetLocationRepository assetLocationRepository;
    private final DepartmentRepository departmentRepository;
    private final DepartmentGroupRepository departmentGroupRepository;
    private final CommonLookupValidator commonLookupValidator;
    private final AssetReceiveRepository assetReceiveRepository;
    private final AssetReceiveSpecRepository assetReceiveSpecRepository;
    private final BusinessGroupRepository businessGroupRepository;
    private final BusinessUnitRepository businessUnitRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AssetReceiveDto> getAllAssetReceives(Long buId) {
        List<AssetReceive> receives = buId == null
                ? assetReceiveRepository.findAllByOrderByIdDesc()
                : assetReceiveRepository.findByBuIdOrderByIdDesc(buId);
        return receives.stream()
                .map(receive -> toDto(receive, true))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AssetReceiveDto getAssetReceiveById(Long id) {
        return toDto(findAssetReceive(id), true);
    }

    @Override
    @Transactional
    public AssetReceiveDto createAssetReceive(AssetReceiveDto dto) {
        AssetReceive target = new AssetReceive();
        validateAndApply(target, dto, null);
        AssetReceive saved = assetReceiveRepository.save(target);
        saved.setGrnNo("GRN-" + String.format("%06d", saved.getId()));
        assetReceiveRepository.save(saved);
        replaceSpecs(saved, dto.getSpecs());
        return toDto(saved, true);
    }

    @Override
    @Transactional
    public AssetReceiveDto updateAssetReceive(Long id, AssetReceiveDto dto) {
        AssetReceive target = findAssetReceive(id);
        validateAndApply(target, dto, id);
        AssetReceive saved = assetReceiveRepository.save(target);
        replaceSpecs(saved, dto.getSpecs());
        return toDto(saved, true);
    }

    @Override
    @Transactional
    public AssetReceiveDto changeStatus(Long id, Integer status) {
        AssetReceive target = findAssetReceive(id);
        target.setStatus(StatusUtil.validate(status));
        return toDto(assetReceiveRepository.save(target), true);
    }

    private AssetReceiveDto toDto(AssetReceive e, boolean withSpecs) {
        AssetReceiveDto d = new AssetReceiveDto();
        d.setId(e.getId());
        d.setGrnNo(e.getGrnNo());
        d.setBgId(e.getBgId());
        d.setBuId(e.getBuId());
        d.setGrnDate(e.getGrnDate());
        d.setPurchaseDate(e.getPurchaseDate());
        d.setPurchaseThrough(e.getPurchaseThrough());
        d.setReceivedById(e.getReceivedById());
        d.setSupplierId(e.getSupplierId());
        d.setItemNameId(e.getItemNameId());
        d.setAssetNo(e.getAssetNo());
        d.setReqNo(e.getReqNo());
        d.setPoNo(e.getPoNo());
        d.setQty(e.getQty());
        d.setUomId(e.getUomId());
        d.setUnitPrice(e.getUnitPrice());
        d.setGrossAmount(e.getGrossAmount());
        d.setDiscount(e.getDiscount());
        d.setVatTax(e.getVatTax());
        d.setOtherCharges(e.getOtherCharges());
        d.setCapitalizedCost(e.getCapitalizedCost());
        d.setCurrencyId(e.getCurrencyId());
        d.setWgApplicable(e.getWgApplicable());
        d.setWgPeriod(e.getWgPeriod());
        d.setWgTypeId(e.getWgTypeId());
        d.setWgStartDate(e.getWgStartDate());
        d.setWgEndDate(e.getWgEndDate());
        d.setWgOtherInfo(e.getWgOtherInfo());
        d.setCustodianId(e.getCustodianId());
        d.setSubCustodianId(e.getSubCustodianId());
        d.setLocationId(e.getLocationId());
        d.setBuilding(e.getBuilding());
        d.setFloor(e.getFloor());
        d.setRoom(e.getRoom());
        d.setRac(e.getRac());
        d.setBin(e.getBin());
        d.setDepriCategoryId(e.getDepriCategoryId());
        d.setDepriMethodId(e.getDepriMethodId());
        d.setAssetTypeId(e.getAssetTypeId());
        d.setDepriLife(e.getDepriLife());
        d.setDepriPercentage(e.getDepriPercentage());
        d.setAssetOwnerId(e.getAssetOwnerId());
        d.setRemarks(e.getRemarks());
        d.setStatus(e.getStatus());
        if (withSpecs) {
            d.setSpecs(assetReceiveSpecRepository
                    .findByAssetReceiveIdAndStatusOrderByIdAsc(e.getId(), 1)
                    .stream()
                    .map(this::toSpecDto)
                    .toList());
        }
        return d;
    }

    private ReceiveSpecDto toSpecDto(AssetReceiveSpec entity) {
        ReceiveSpecDto dto = new ReceiveSpecDto();
        dto.setId(entity.getId());
        dto.setEquipmentName(entity.getEquipmentName());
        dto.setEquipmentValue(entity.getEquipmentValue());
        dto.setStatus(entity.getStatus());
        return dto;
    }

    private void validateAndApply(
            AssetReceive t, AssetReceiveDto d, Long selfId) {
        validateParents(d.getBgId(), d.getBuId());

        if (d.getGrnDate() == null) {
            throw new BadRequestException("GRN date cannot be empty");
        }
        if (d.getGrnDate().isAfter(LocalDate.now())) {
            throw new BadRequestException(
                    "GRN date cannot be a future date");
        }
        if (d.getPurchaseDate() != null) {
            if (d.getPurchaseDate().isAfter(LocalDate.now())) {
                throw new BadRequestException(
                        "Purchase date cannot be a future date");
            }
            if (d.getPurchaseDate().isAfter(d.getGrnDate())) {
                throw new BadRequestException(
                        "Purchase date cannot be after GRN date");
            }
        }
        String purchaseThrough = ValidationUtil.optionalText(
                d.getPurchaseThrough(), "Purchase through", 50);

        Supplier supplier = requireSupplier(d.getSupplierId(), d.getBuId());
        if (d.getReceivedById() == null) {
            throw new BadRequestException("Received by cannot be empty");
        }

        AssetItem item = requireAssetItem(d.getItemNameId(), d.getBuId());
        requireUom(d.getUomId(), d.getBuId());

        String reqNo = ValidationUtil.optionalText(d.getReqNo(), "Req no", 50);
        String poNo = ValidationUtil.optionalText(d.getPoNo(), "PO no", 50);
        String assetNo = ValidationUtil.optionalText(
                d.getAssetNo(), "Asset no", 50);
        if (assetNo != null && assetReceiveRepository
                .findByBuIdAndAssetNoIgnoreCase(d.getBuId(), assetNo)
                .stream()
                .anyMatch(x -> selfId == null || !x.getId().equals(selfId))) {
            throw new DuplicateResourceException(
                    "Asset no already exists: " + assetNo);
        }

        Integer qty = ValidationUtil.requiredInt(
                d.getQty(), "Qty", 1, 1_000_000);
        BigDecimal unitPrice = requiredNonNegative(
                d.getUnitPrice(), "Unit price");
        unitPrice = scale(unitPrice);
        BigDecimal discount = nonNegativeOrZero(d.getDiscount(), "Discount");
        BigDecimal vatTax = nonNegativeOrZero(d.getVatTax(), "VAT/Tax");
        BigDecimal otherCharges = nonNegativeOrZero(
                d.getOtherCharges(), "Other charges");
        discount = scale(discount);
        vatTax = scale(vatTax);
        otherCharges = scale(otherCharges);
        BigDecimal grossAmount = scale(unitPrice.multiply(
                BigDecimal.valueOf(qty)));
        if (discount.compareTo(grossAmount) > 0) {
            throw new BadRequestException(
                    "Discount cannot exceed Gross amount");
        }
        BigDecimal capitalizedCost = scale(grossAmount
                .subtract(discount).add(vatTax).add(otherCharges));
        if (capitalizedCost.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException(
                    "Capitalized cost cannot be negative");
        }

        commonLookupValidator.validate(
                d.getCurrencyId(), d.getBuId(), "Currency", "Currency");

        String wgApplicable = normalizeWarranty(d.getWgApplicable());
        Integer wgPeriod = d.getWgPeriod();
        Long wgTypeId = d.getWgTypeId();
        LocalDate wgStartDate = d.getWgStartDate();
        LocalDate wgEndDate = d.getWgEndDate();
        String wgOtherInfo = ValidationUtil.optionalText(
                d.getWgOtherInfo(), "Warranty/Guaranty other info", 500);
        if ("N/A".equals(wgApplicable)) {
            wgPeriod = null;
            wgTypeId = null;
            wgStartDate = null;
            wgEndDate = null;
            wgOtherInfo = null;
        } else {
            if (wgPeriod == null || wgPeriod < 1) {
                throw new BadRequestException(
                        "Warranty/Guaranty period must be greater than 0");
            }
            commonLookupValidator.validate(
                    wgTypeId, d.getBuId(), "Warranty Type", "Warranty type");
            if (wgStartDate == null) {
                throw new BadRequestException(
                        "Warranty/Guaranty start date cannot be empty");
            }
            if (wgEndDate == null) {
                throw new BadRequestException(
                        "Warranty/Guaranty end date cannot be empty");
            }
            if (wgEndDate.isBefore(wgStartDate)) {
                throw new BadRequestException(
                        "Warranty/Guaranty end date cannot be before start date");
            }
        }

        Custodian custodian = requireCustodian(
                d.getCustodianId(), d.getBuId());
        CustodianDtl subCustodian = requireSubCustodian(
                d.getSubCustodianId());
        if (!custodian.getId().equals(subCustodian.getCustodianId())) {
            throw new BadRequestException(
                    "Sub Custodian does not belong to selected Custodian");
        }

        AssetLocation location = requireLocation(
                d.getLocationId(), d.getBuId());
        if (!custodian.getId().equals(location.getCustodianId())
                || !subCustodian.getId().equals(location.getSubCustodianId())) {
            throw new BadRequestException(
                    "Location does not belong to selected Custodian/Sub Custodian");
        }
        String rac = ValidationUtil.optionalText(d.getRac(), "RAC", 50);
        String bin = ValidationUtil.optionalText(d.getBin(), "Bin", 50);

        Long categoryId = d.getDepriCategoryId() != null
                ? d.getDepriCategoryId() : item.getAssetDepriCatId();
        DepriCategory category = requireCategory(categoryId, d.getBuId());
        if (d.getDepriCategoryId() != null
                && !d.getDepriCategoryId().equals(item.getAssetDepriCatId())) {
            throw new BadRequestException(
                    "Asset item does not belong to selected Depreciation Category");
        }

        Long depriMethodId = d.getDepriMethodId() != null
                ? d.getDepriMethodId() : category.getDepriMethodId();
        Long assetTypeId = d.getAssetTypeId() != null
                ? d.getAssetTypeId() : category.getAssetTypeId();
        Integer depriLife = d.getDepriLife() != null
                ? d.getDepriLife() : category.getDepriLife();
        BigDecimal depriPercentage = d.getDepriPercentage() != null
                ? d.getDepriPercentage() : category.getDepriPercentage();
        Long assetOwnerId = d.getAssetOwnerId() != null
                ? d.getAssetOwnerId() : category.getAssetOwnerId();

        commonLookupValidator.validate(
                depriMethodId, d.getBuId(), null, "Depreciation method");
        commonLookupValidator.validate(
                assetTypeId, d.getBuId(), null, "Asset type");
        depriLife = ValidationUtil.requiredInt(
                depriLife, "Depreciation period", 1, 1200);
        if (depriPercentage == null) {
            throw new BadRequestException(
                    "Depreciation percentage cannot be empty");
        }
        depriPercentage = scale(depriPercentage);
        if (depriPercentage.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(
                    "Depreciation percentage must be greater than 0");
        }
        if (depriPercentage.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BadRequestException(
                    "Depreciation percentage cannot exceed 100");
        }

        validateOwner(assetOwnerId, d.getBgId(), d.getBuId());
        String remarks = ValidationUtil.optionalText(
                d.getRemarks(), "Remarks", 500);

        t.setBgId(d.getBgId());
        t.setBuId(d.getBuId());
        t.setGrnDate(d.getGrnDate());
        t.setPurchaseDate(d.getPurchaseDate());
        t.setPurchaseThrough(purchaseThrough);
        t.setReceivedById(d.getReceivedById());
        t.setSupplierId(supplier.getId());
        t.setItemNameId(item.getId());
        t.setAssetNo(assetNo);
        t.setReqNo(reqNo);
        t.setPoNo(poNo);
        t.setQty(qty);
        t.setUomId(d.getUomId());
        t.setUnitPrice(unitPrice);
        t.setGrossAmount(grossAmount);
        t.setDiscount(discount);
        t.setVatTax(vatTax);
        t.setOtherCharges(otherCharges);
        t.setCapitalizedCost(capitalizedCost);
        t.setCurrencyId(d.getCurrencyId());
        t.setWgApplicable(wgApplicable);
        t.setWgPeriod(wgPeriod);
        t.setWgTypeId(wgTypeId);
        t.setWgStartDate(wgStartDate);
        t.setWgEndDate(wgEndDate);
        t.setWgOtherInfo(wgOtherInfo);
        t.setCustodianId(custodian.getId());
        t.setSubCustodianId(subCustodian.getId());
        t.setLocationId(location.getId());
        t.setBuilding(location.getBuilding());
        t.setFloor(location.getFloor());
        t.setRoom(location.getRoom());
        t.setRac(rac);
        t.setBin(bin);
        t.setDepriCategoryId(categoryId);
        t.setDepriMethodId(depriMethodId);
        t.setAssetTypeId(assetTypeId);
        t.setDepriLife(depriLife);
        t.setDepriPercentage(depriPercentage);
        t.setAssetOwnerId(assetOwnerId);
        t.setRemarks(remarks);
        if (t.getStatus() == null) {
            t.setStatus(1);
        }
    }

    private void replaceSpecs(
            AssetReceive receive, List<ReceiveSpecDto> submittedSpecs) {
        List<ReceiveSpecDto> specs = submittedSpecs == null
                ? List.of() : submittedSpecs;
        List<AssetReceiveSpec> current = assetReceiveSpecRepository
                .findByAssetReceiveIdOrderByIdAsc(receive.getId());
        Map<Long, AssetReceiveSpec> existingById = new HashMap<>();
        for (AssetReceiveSpec existing : current) {
            existingById.put(existing.getId(), existing);
        }

        Set<Long> handledIds = new HashSet<>();
        List<AssetReceiveSpec> newSpecs = new ArrayList<>();
        for (ReceiveSpecDto dto : specs) {
            Integer status = StatusUtil.validate(
                    dto.getStatus() == null ? 1 : dto.getStatus());
            if (dto.getId() != null) {
                AssetReceiveSpec existing = existingById.get(dto.getId());
                if (existing == null) {
                    throw new BadRequestException(
                            "Receive spec id " + dto.getId()
                                    + " does not belong to this Asset Receive");
                }
                existing.setBgId(receive.getBgId());
                existing.setBuId(receive.getBuId());
                existing.setAssetReceiveId(receive.getId());
                existing.setEquipmentName(dto.getEquipmentName());
                existing.setEquipmentValue(dto.getEquipmentValue());
                existing.setStatus(status);
                handledIds.add(dto.getId());
            } else {
                AssetReceiveSpec spec = new AssetReceiveSpec();
                spec.setBgId(receive.getBgId());
                spec.setBuId(receive.getBuId());
                spec.setAssetReceiveId(receive.getId());
                spec.setEquipmentName(dto.getEquipmentName());
                spec.setEquipmentValue(dto.getEquipmentValue());
                spec.setStatus(status);
                newSpecs.add(spec);
            }
        }

        for (AssetReceiveSpec existing : current) {
            if (!handledIds.contains(existing.getId())) {
                existing.setStatus(0);
            }
        }
        assetReceiveSpecRepository.saveAll(current);
        assetReceiveSpecRepository.saveAll(newSpecs);
    }

    private AssetReceive findAssetReceive(Long id) {
        return assetReceiveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Asset Receive not found with id: " + id));
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

    private Supplier requireSupplier(Long id, Long buId) {
        if (id == null) {
            throw new BadRequestException("Supplier cannot be empty");
        }
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Supplier not found with id: " + id));
        requireActive(supplier.getStatus(), "Selected Supplier is not active");
        if (!buId.equals(supplier.getBuId())) {
            throw new BadRequestException(
                    "Supplier does not belong to selected Business Unit");
        }
        return supplier;
    }

    private AssetItem requireAssetItem(Long id, Long buId) {
        if (id == null) {
            throw new BadRequestException("Asset item cannot be empty");
        }
        AssetItem item = assetItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Asset Item not found with id: " + id));
        requireActive(item.getStatus(), "Selected Asset Item is not active");
        if (!buId.equals(item.getBuId())) {
            throw new BadRequestException(
                    "Asset item does not belong to selected Business Unit");
        }
        return item;
    }

    private void requireUom(Long id, Long buId) {
        if (id == null) {
            throw new BadRequestException("UOM cannot be empty");
        }
        var uom = assetUomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "UOM not found with id: " + id));
        requireActive(uom.getStatus(), "Selected UOM is not active");
        if (!buId.equals(uom.getBuId())) {
            throw new BadRequestException(
                    "UOM does not belong to selected Business Unit");
        }
    }

    private Custodian requireCustodian(Long id, Long buId) {
        if (id == null) {
            throw new BadRequestException("Custodian cannot be empty");
        }
        Custodian custodian = custodianRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Custodian not found with id: " + id));
        requireActive(custodian.getStatus(), "Selected Custodian is not active");
        if (!buId.equals(custodian.getBuId())) {
            throw new BadRequestException(
                    "Custodian does not belong to selected Business Unit");
        }
        return custodian;
    }

    private CustodianDtl requireSubCustodian(Long id) {
        if (id == null) {
            throw new BadRequestException("Sub custodian cannot be empty");
        }
        CustodianDtl subCustodian = custodianDtlRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sub Custodian not found with id: " + id));
        requireActive(subCustodian.getStatus(),
                "Selected Sub Custodian is not active");
        return subCustodian;
    }

    private AssetLocation requireLocation(Long id, Long buId) {
        if (id == null) {
            throw new BadRequestException("Location cannot be empty");
        }
        AssetLocation location = assetLocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Location not found with id: " + id));
        requireActive(location.getStatus(), "Selected Location is not active");
        if (!buId.equals(location.getBuId())) {
            throw new BadRequestException(
                    "Location does not belong to selected Business Unit");
        }
        return location;
    }

    private DepriCategory requireCategory(Long id, Long buId) {
        DepriCategory category = depriCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Depreciation Category not found with id: " + id));
        requireActive(category.getStatus(),
                "Selected Depreciation Category is not active");
        if (!buId.equals(category.getBuId())) {
            throw new BadRequestException(
                    "Depreciation Category does not belong to selected Business Unit");
        }
        return category;
    }

    private void validateOwner(Long id, Long bgId, Long buId) {
        if (id == null) {
            throw new BadRequestException("Asset owner cannot be empty");
        }
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with id: " + id));
        requireActive(department.getStatus(), "Selected Department is not active");
        if (!bgId.equals(department.getBgId())) {
            throw new BadRequestException(
                    "Asset owner does not belong to selected Business Group");
        }
        if (departmentGroupRepository.findByDeptIdAndBuId(id, buId).isEmpty()) {
            throw new BadRequestException(
                    "Asset owner is not mapped to selected Business Unit");
        }
    }

    private String normalizeWarranty(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new BadRequestException(
                    "Warranty/Guaranty applicability cannot be empty");
        }
        String trimmed = value.trim();
        if (trimmed.equalsIgnoreCase("Warranty")) {
            return "Warranty";
        }
        if (trimmed.equalsIgnoreCase("Guaranty")) {
            return "Guaranty";
        }
        if (trimmed.equalsIgnoreCase("N/A")) {
            return "N/A";
        }
        throw new BadRequestException(
                "Applicability must be Warranty, Guaranty or N/A");
    }

    private BigDecimal requiredNonNegative(BigDecimal value, String label) {
        if (value == null) {
            throw new BadRequestException(label + " cannot be empty");
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException(label + " cannot be negative");
        }
        return value;
    }

    private BigDecimal nonNegativeOrZero(BigDecimal value, String label) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException(label + " cannot be negative");
        }
        return value;
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private void requireActive(Integer status, String message) {
        if (!Integer.valueOf(1).equals(status)) {
            throw new BadRequestException(message);
        }
    }
}
