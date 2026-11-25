package com.tse.erp.module.asset.service.impl;

import com.tse.erp.common.StatusUtil;
import com.tse.erp.common.ValidationUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.accounting.repository.AfmSupplierInfoRepository;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.asset.dto.AssetBookRegisterDto;
import com.tse.erp.module.asset.entity.AssetBookRegister;
import com.tse.erp.module.asset.repository.AssetBookRegisterRepository;
import com.tse.erp.module.asset.repository.AssetItemRepository;
import com.tse.erp.module.asset.repository.DepriCategoryRepository;
import com.tse.erp.module.asset.service.AssetBookRegisterService;
import com.tse.erp.module.hr.repository.DepartmentRepository;
import com.tse.erp.module.hr.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetBookRegisterServiceImpl implements AssetBookRegisterService {

    private final AssetBookRegisterRepository repository;
    private final BusinessGroupRepository businessGroupRepository;
    private final BusinessUnitRepository businessUnitRepository;
    private final AssetItemRepository assetItemRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final AfmSupplierInfoRepository afmSupplierInfoRepository;
    private final DepriCategoryRepository depriCategoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AssetBookRegisterDto> getAllAssetBookRegisters(Long buId) {
        List<AssetBookRegister> list = (buId == null)
                ? repository.findAllByOrderByIdDesc()
                : repository.findByBuIdOrderByIdDesc(buId);
        return list.stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AssetBookRegisterDto getAssetBookRegisterById(Long id) {
        return toDto(findEntity(id));
    }

    @Override
    @Transactional
    public AssetBookRegisterDto createAssetBookRegister(AssetBookRegisterDto dto) {
        AssetBookRegister entity = new AssetBookRegister();
        validateAndApply(entity, dto, null);
        entity.setId(null);
        return toDto(repository.save(entity));
    }

    @Override
    @Transactional
    public AssetBookRegisterDto updateAssetBookRegister(Long id, AssetBookRegisterDto dto) {
        AssetBookRegister existing = findEntity(id);
        validateAndApply(existing, dto, id);
        return toDto(repository.save(existing));
    }

    @Override
    @Transactional
    public AssetBookRegisterDto changeStatus(Long id, Integer status) {
        AssetBookRegister entity = findEntity(id);
        entity.setStatus(StatusUtil.validate(status));
        return toDto(repository.save(entity));
    }

    @Override
    @Transactional
    public void deleteAssetBookRegister(Long id) {
        AssetBookRegister entity = findEntity(id);
        repository.delete(entity);
    }

    private AssetBookRegister findEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset Book Register not found with id: " + id));
    }

    private void validateAndApply(AssetBookRegister target, AssetBookRegisterDto d, Long selfId) {
        if (d == null) {
            throw new BadRequestException("Payload cannot be empty");
        }

        if (d.getBuId() == null) {
            throw new BadRequestException("Business Unit cannot be empty");
        }
        BusinessUnit bu = businessUnitRepository.findById(d.getBuId())
                .orElseThrow(() -> new ResourceNotFoundException("Business Unit not found with id: " + d.getBuId()));

        Long bgId = d.getBgId() != null ? d.getBgId() : bu.getBgId();
        if (bgId == null) {
            throw new BadRequestException("Business Group cannot be empty");
        }
        if (!businessGroupRepository.existsById(bgId)) {
            throw new ResourceNotFoundException("Business Group not found with id: " + bgId);
        }

        // asset_no uniqueness check per BU
        String assetNo = ValidationUtil.optionalText(d.getAssetNo(), "Asset no", 50);
        if (assetNo != null) {
            boolean duplicate = repository.findByBuIdAndAssetNoIgnoreCase(d.getBuId(), assetNo).stream()
                    .anyMatch(x -> selfId == null || !x.getId().equals(selfId));
            if (duplicate) {
                throw new DuplicateResourceException("Asset no already exists: " + assetNo);
            }
        }

        // FK validations if provided
        if (d.getAssetItemId() != null && !assetItemRepository.existsById(d.getAssetItemId())) {
            throw new ResourceNotFoundException("Asset Item not found with id: " + d.getAssetItemId());
        }
        if (d.getAssetMajorCategoryId() != null && !assetItemRepository.existsById(d.getAssetMajorCategoryId())) {
            throw new ResourceNotFoundException("Asset Major Category (Asset Item) not found with id: " + d.getAssetMajorCategoryId());
        }
        if (d.getAssetOwnerId() != null && !departmentRepository.existsById(d.getAssetOwnerId())) {
            throw new ResourceNotFoundException("Asset Owner (Department) not found with id: " + d.getAssetOwnerId());
        }
        if (d.getAllocationRefId() != null && !employeeRepository.existsById(d.getAllocationRefId())) {
            throw new ResourceNotFoundException("Allocation Employee not found with id: " + d.getAllocationRefId());
        }
        if (d.getSupplierId() != null && !afmSupplierInfoRepository.existsById(d.getSupplierId())) {
            throw new ResourceNotFoundException("Supplier not found with id: " + d.getSupplierId());
        }
        if (d.getWarProviderSupplierId() != null && !afmSupplierInfoRepository.existsById(d.getWarProviderSupplierId())) {
            throw new ResourceNotFoundException("Warranty Provider Supplier not found with id: " + d.getWarProviderSupplierId());
        }
        if (d.getGuarProviderSupplierId() != null && !afmSupplierInfoRepository.existsById(d.getGuarProviderSupplierId())) {
            throw new ResourceNotFoundException("Guaranty Provider Supplier not found with id: " + d.getGuarProviderSupplierId());
        }
        if (d.getDepriciationMethodId() != null && !depriCategoryRepository.existsById(d.getDepriciationMethodId())) {
            throw new ResourceNotFoundException("Depreciation Category not found with id: " + d.getDepriciationMethodId());
        }

        if (d.getStatus() != null) {
            target.setStatus(StatusUtil.validate(d.getStatus()));
        }

        target.setBgId(bgId);
        target.setBuId(d.getBuId());
        target.setAssetItemId(d.getAssetItemId());
        target.setAssetNo(assetNo);
        target.setAssetUnit(ValidationUtil.optionalText(d.getAssetUnit(), "Asset unit", 20));
        target.setAssetMajorCategoryId(d.getAssetMajorCategoryId());
        target.setAssetOwnerId(d.getAssetOwnerId());
        target.setAllocationType(ValidationUtil.optionalText(d.getAllocationType(), "Allocation type", 50));
        target.setAllocationRefId(d.getAllocationRefId());
        target.setUsageStatus(ValidationUtil.optionalText(d.getUsageStatus(), "Usage status", 50));
        target.setAssetCondition(ValidationUtil.optionalText(d.getAssetCondition(), "Asset condition", 50));
        target.setAssetStatus(ValidationUtil.optionalText(d.getAssetStatus(), "Asset status", 50));
        target.setAllocationDate(d.getAllocationDate());

        target.setPurchaseThrough(ValidationUtil.optionalText(d.getPurchaseThrough(), "Purchase through", 50));
        target.setPurchaseOrderNo(ValidationUtil.optionalText(d.getPurchaseOrderNo(), "Purchase order no", 50));
        target.setSupplierId(d.getSupplierId());
        target.setReceivedDate(d.getReceivedDate());

        target.setWarCoverage(ValidationUtil.optionalText(d.getWarCoverage(), "Warranty coverage", 50));
        target.setWarProviderType(ValidationUtil.optionalText(d.getWarProviderType(), "Warranty provider type", 50));
        target.setWarProviderSupplierId(d.getWarProviderSupplierId());
        target.setWarRefNo(ValidationUtil.optionalText(d.getWarRefNo(), "Warranty ref no", 50));
        target.setWarTerms(ValidationUtil.optionalText(d.getWarTerms(), "Warranty terms", 500));
        target.setWarMonths(d.getWarMonths());
        target.setWarStartDate(d.getWarStartDate());
        target.setWarEndDate(d.getWarEndDate());
        target.setWarStatus(ValidationUtil.optionalText(d.getWarStatus(), "Warranty status", 50));

        target.setGuarCoverage(ValidationUtil.optionalText(d.getGuarCoverage(), "Guaranty coverage", 50));
        target.setGuarProviderType(ValidationUtil.optionalText(d.getGuarProviderType(), "Guaranty provider type", 50));
        target.setGuarProviderSupplierId(d.getGuarProviderSupplierId());
        target.setGuarRefNo(ValidationUtil.optionalText(d.getGuarRefNo(), "Guaranty ref no", 50));
        target.setGuarTerms(ValidationUtil.optionalText(d.getGuarTerms(), "Guaranty terms", 500));
        target.setGuarMonths(d.getGuarMonths());
        target.setGuarStartDate(d.getGuarStartDate());
        target.setGuarEndDate(d.getGuarEndDate());
        target.setGuarStatus(ValidationUtil.optionalText(d.getGuarStatus(), "Guaranty status", 50));

        target.setAssetValue(scale(d.getAssetValue()));
        target.setSalvageValue(scale(d.getSalvageValue()));
        target.setDepriciationValue(scale(d.getDepriciationValue()));
        target.setDepriciationMethodId(d.getDepriciationMethodId());
        target.setDepriciationFrequency(ValidationUtil.optionalText(d.getDepriciationFrequency(), "Depreciation frequency", 50));
        target.setUsefulLife(d.getUsefulLife());
        target.setDepriciationStartDate(d.getDepriciationStartDate());
        target.setDepriciationEndDate(d.getDepriciationEndDate());

        target.setAccumulatedDepriciation(scale(d.getAccumulatedDepriciation()));
        target.setAssetBookValue(scale(d.getAssetBookValue()));
        target.setAssetApprovalStatus(ValidationUtil.optionalText(d.getAssetApprovalStatus(), "Asset approval status", 50));
    }

    private BigDecimal scale(BigDecimal val) {
        return val == null ? null : val.setScale(2, RoundingMode.HALF_UP);
    }

    private AssetBookRegisterDto toDto(AssetBookRegister e) {
        if (e == null) return null;
        return AssetBookRegisterDto.builder()
                .id(e.getId())
                .bgId(e.getBgId())
                .buId(e.getBuId())
                .assetItemId(e.getAssetItemId())
                .assetNo(e.getAssetNo())
                .assetUnit(e.getAssetUnit())
                .assetMajorCategoryId(e.getAssetMajorCategoryId())
                .assetOwnerId(e.getAssetOwnerId())
                .allocationType(e.getAllocationType())
                .allocationRefId(e.getAllocationRefId())
                .usageStatus(e.getUsageStatus())
                .assetCondition(e.getAssetCondition())
                .assetStatus(e.getAssetStatus())
                .allocationDate(e.getAllocationDate())
                .purchaseThrough(e.getPurchaseThrough())
                .purchaseOrderNo(e.getPurchaseOrderNo())
                .supplierId(e.getSupplierId())
                .receivedDate(e.getReceivedDate())
                .warCoverage(e.getWarCoverage())
                .warProviderType(e.getWarProviderType())
                .warProviderSupplierId(e.getWarProviderSupplierId())
                .warRefNo(e.getWarRefNo())
                .warTerms(e.getWarTerms())
                .warMonths(e.getWarMonths())
                .warStartDate(e.getWarStartDate())
                .warEndDate(e.getWarEndDate())
                .warStatus(e.getWarStatus())
                .guarCoverage(e.getGuarCoverage())
                .guarProviderType(e.getGuarProviderType())
                .guarProviderSupplierId(e.getGuarProviderSupplierId())
                .guarRefNo(e.getGuarRefNo())
                .guarTerms(e.getGuarTerms())
                .guarMonths(e.getGuarMonths())
                .guarStartDate(e.getGuarStartDate())
                .guarEndDate(e.getGuarEndDate())
                .guarStatus(e.getGuarStatus())
                .assetValue(e.getAssetValue())
                .salvageValue(e.getSalvageValue())
                .depriciationValue(e.getDepriciationValue())
                .depriciationMethodId(e.getDepriciationMethodId())
                .depriciationFrequency(e.getDepriciationFrequency())
                .usefulLife(e.getUsefulLife())
                .depriciationStartDate(e.getDepriciationStartDate())
                .depriciationEndDate(e.getDepriciationEndDate())
                .accumulatedDepriciation(e.getAccumulatedDepriciation())
                .assetBookValue(e.getAssetBookValue())
                .assetApprovalStatus(e.getAssetApprovalStatus())
                .createdUserId(e.getCreatedUserId())
                .createdEmpId(e.getCreatedEmpId())
                .createdDatetime(e.getCreatedDatetime())
                .updatedUserId(e.getUpdatedUserId())
                .updatedEmpId(e.getUpdatedEmpId())
                .updatedDatetime(e.getUpdatedDatetime())
                .createdApp(e.getCreatedApp())
                .updatedApp(e.getUpdatedApp())
                .status(e.getStatus())
                .build();
    }
}
