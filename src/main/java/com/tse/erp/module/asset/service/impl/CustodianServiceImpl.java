package com.tse.erp.module.asset.service.impl;

import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.asset.dto.CustodianRequestDto;
import com.tse.erp.module.asset.dto.CustodianResponseDto;
import com.tse.erp.module.asset.dto.SubCustodianDto;
import com.tse.erp.module.asset.entity.Custodian;
import com.tse.erp.module.asset.entity.CustodianDtl;
import com.tse.erp.module.asset.repository.AssetLocationRepository;
import com.tse.erp.module.asset.repository.CustodianDtlRepository;
import com.tse.erp.module.asset.repository.CustodianRepository;
import com.tse.erp.module.asset.service.CustodianService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CustodianServiceImpl implements CustodianService {

    private final CustodianRepository custodianRepository;
    private final CustodianDtlRepository custodianDtlRepository;
    private final AssetLocationRepository assetLocationRepository;
    private final BusinessGroupRepository businessGroupRepository;
    private final BusinessUnitRepository businessUnitRepository;

    @Override
    public List<CustodianResponseDto> getAllCustodians(Long buId) {
        List<Custodian> custodians = buId == null
                ? custodianRepository.findAllByOrderByIdDesc()
                : custodianRepository.findByBuIdOrderByIdDesc(buId);
        return custodians.stream().map(this::toResponse).toList();
    }

    @Override
    public CustodianResponseDto getCustodianById(Long id) {
        return toResponse(findCustodian(id));
    }

    @Override
    @Transactional
    public CustodianResponseDto createCustodian(
            CustodianRequestDto request) {
        validateParents(request.getBgId(), request.getBuId());
        validateName(request.getCustodianName(), "Custodian name");
        StatusUtil.validate(request.getStatus());

        String name = request.getCustodianName().trim();
        validateSubRows(request.getSubCustodians(), name);

        boolean exists = !custodianRepository
                .findByBuIdAndCustodianNameIgnoreCase(
                        request.getBuId(), name)
                .isEmpty();
        if (exists) {
            throw new DuplicateResourceException(
                    "Custodian already exists: " + name);
        }

        Custodian custodian = new Custodian();
        custodian.setBgId(request.getBgId());
        custodian.setBuId(request.getBuId());
        custodian.setCustodianName(name);
        if (request.getStatus() != null) {
            custodian.setStatus(request.getStatus());
        }
        Custodian saved = custodianRepository.save(custodian);

        if (request.getSubCustodians() != null) {
            List<CustodianDtl> details = request.getSubCustodians().stream()
                    .map(row -> CustodianDtl.builder()
                            .custodianId(saved.getId())
                            .subCustodianName(
                                    row.getSubCustodianName().trim())
                            .status(row.getStatus())
                            .build())
                    .toList();
            custodianDtlRepository.saveAll(details);
        }

        return toResponse(saved);
    }

    @Override
    @Transactional
    public CustodianResponseDto updateCustodian(
            Long id, CustodianRequestDto request) {
        Custodian existing = findCustodian(id);

        validateParents(request.getBgId(), request.getBuId());
        validateName(request.getCustodianName(), "Custodian name");
        StatusUtil.validate(request.getStatus());

        String name = request.getCustodianName().trim();
        validateSubRows(request.getSubCustodians(), name);

        boolean duplicateExists = custodianRepository
                .findByBuIdAndCustodianNameIgnoreCase(
                        request.getBuId(), name)
                .stream()
                .anyMatch(x -> !x.getId().equals(id));
        if (duplicateExists) {
            throw new DuplicateResourceException(
                    "Custodian already exists: " + name);
        }

        existing.setBgId(request.getBgId());
        existing.setBuId(request.getBuId());
        existing.setCustodianName(name);
        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }
        Custodian saved = custodianRepository.save(existing);

        List<CustodianDtl> current =
                custodianDtlRepository.findByCustodianIdOrderByIdAsc(id);
        Map<Long, CustodianDtl> byId = new HashMap<>();
        current.forEach(detail -> byId.put(detail.getId(), detail));
        Set<Long> handled = new HashSet<>();
        List<CustodianDtl> newRows = new ArrayList<>();

        List<SubCustodianDto> rows = request.getSubCustodians() == null
                ? Collections.emptyList()
                : request.getSubCustodians();
        for (SubCustodianDto row : rows) {
            if (row.getId() != null) {
                CustodianDtl detail = byId.get(row.getId());
                if (detail == null) {
                    throw new BadRequestException(
                            "Sub custodian id " + row.getId()
                                    + " does not belong to this Custodian");
                }
                detail.setSubCustodianName(
                        row.getSubCustodianName().trim());
                if (row.getStatus() != null) {
                    detail.setStatus(row.getStatus());
                }
                handled.add(row.getId());
            } else {
                newRows.add(CustodianDtl.builder()
                        .custodianId(id)
                        .subCustodianName(
                                row.getSubCustodianName().trim())
                        .status(row.getStatus())
                        .build());
            }
        }

        for (CustodianDtl detail : current) {
            if (!handled.contains(detail.getId())) {
                detail.setStatus(0);
            }
        }

        List<CustodianDtl> allRows = new ArrayList<>(current);
        allRows.addAll(newRows);
        Set<String> names = new HashSet<>();
        for (CustodianDtl detail : allRows) {
            String detailName = detail.getSubCustodianName().trim();
            if (!names.add(detailName.toLowerCase(Locale.ROOT))) {
                throw new DuplicateResourceException(
                        "Sub custodian name already exists under this Custodian: "
                                + detailName);
            }
        }

        custodianDtlRepository.saveAll(allRows);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteCustodian(Long id) {
        Custodian custodian = findCustodian(id);
        if (assetLocationRepository.existsByCustodianId(id)) {
            throw new BadRequestException(
                    "Cannot delete: Locations exist under this Custodian");
        }
        List<CustodianDtl> details =
                custodianDtlRepository.findByCustodianIdOrderByIdAsc(id);
        custodianDtlRepository.deleteAll(details);
        custodianRepository.delete(custodian);
    }

    private Custodian findCustodian(Long id) {
        return custodianRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Custodian not found with id: " + id));
    }

    private CustodianResponseDto toResponse(Custodian custodian) {
        List<SubCustodianDto> subCustodians =
                custodianDtlRepository
                        .findByCustodianIdOrderByIdAsc(custodian.getId())
                        .stream()
                        .map(detail -> SubCustodianDto.builder()
                                .id(detail.getId())
                                .subCustodianName(
                                        detail.getSubCustodianName())
                                .status(detail.getStatus())
                                .build())
                        .toList();

        return CustodianResponseDto.builder()
                .id(custodian.getId())
                .bgId(custodian.getBgId())
                .buId(custodian.getBuId())
                .custodianName(custodian.getCustodianName())
                .status(custodian.getStatus())
                .subCustodians(subCustodians)
                .build();
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

    private void validateName(String name, String label) {
        if (name == null || name.trim().isEmpty()) {
            throw new BadRequestException(label + " cannot be empty");
        }
        if (name.trim().length() < 2) {
            throw new BadRequestException(
                    label + " must be at least 2 characters");
        }
        if (name.trim().length() > 100) {
            throw new BadRequestException(
                    label + " cannot exceed 100 characters");
        }
    }

    private void validateSubRows(
            List<SubCustodianDto> rows, String parentName) {
        if (rows == null) {
            return;
        }

        Set<String> names = new HashSet<>();
        for (SubCustodianDto row : rows) {
            validateName(row.getSubCustodianName(), "Sub custodian name");
            StatusUtil.validate(row.getStatus());

            String name = row.getSubCustodianName().trim();
            if (name.equalsIgnoreCase(parentName.trim())) {
                throw new BadRequestException(
                        "Sub custodian name cannot be same as Custodian name: "
                                + name);
            }
            if (!names.add(name.toLowerCase(Locale.ROOT))) {
                throw new DuplicateResourceException(
                        "Duplicate sub custodian name: " + name);
            }
        }
    }
}
