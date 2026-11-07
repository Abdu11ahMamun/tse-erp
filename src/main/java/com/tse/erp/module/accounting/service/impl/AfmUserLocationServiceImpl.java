package com.tse.erp.module.accounting.service.impl;

import com.tse.erp.common.AppConstants;
import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.accounting.dto.AfmUserLocationRequestDto;
import com.tse.erp.module.accounting.dto.AfmUserLocationResponseDto;
import com.tse.erp.module.accounting.entity.AfmUserLocation;
import com.tse.erp.module.accounting.repository.AfmUserLocationRepository;
import com.tse.erp.module.accounting.service.AfmUserLocationService;
import com.tse.erp.module.admin.entity.BusinessGroup;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AfmUserLocationServiceImpl implements AfmUserLocationService {

    private final AfmUserLocationRepository userLocationRepository;
    private final BusinessGroupRepository businessGroupRepository;
    private final BusinessUnitRepository businessUnitRepository;

    @Override
    public List<AfmUserLocationResponseDto> getAllUserLocations(Long bgId, Long buId, String location, Integer status) {
        return userLocationRepository.searchUserLocations(bgId, buId, location, status)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public AfmUserLocationResponseDto getUserLocationById(Long id) {
        return toResponse(findLocation(id));
    }

    @Override
    @Transactional
    public AfmUserLocationResponseDto createUserLocation(AfmUserLocationRequestDto request) {
        validateParents(request.getBgId(), request.getBuId());

        if (request.getLocation() == null || request.getLocation().trim().isEmpty()) {
            throw new BadRequestException("Location name cannot be empty");
        }

        String location = request.getLocation().trim();
        if (userLocationRepository.existsByBuIdAndLocationIgnoreCase(request.getBuId(), location)) {
            throw new DuplicateResourceException("Location '" + location + "' already exists for this Business Unit");
        }

        StatusUtil.validate(request.getStatus());

        AfmUserLocation entity = AfmUserLocation.builder()
                .bgId(request.getBgId())
                .buId(request.getBuId())
                .location(location)
                .shortName(request.getShortName() != null ? request.getShortName().trim() : null)
                .build();

        entity.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        entity.setCreatedApp(AppConstants.APP_NAME);

        return toResponse(userLocationRepository.save(entity));
    }

    @Override
    @Transactional
    public AfmUserLocationResponseDto updateUserLocation(Long id, AfmUserLocationRequestDto request) {
        AfmUserLocation existing = findLocation(id);

        validateParents(request.getBgId(), request.getBuId());

        if (request.getLocation() == null || request.getLocation().trim().isEmpty()) {
            throw new BadRequestException("Location name cannot be empty");
        }

        String location = request.getLocation().trim();
        userLocationRepository.findByBuIdAndLocationIgnoreCase(request.getBuId(), location)
                .ifPresent(other -> {
                    if (!other.getId().equals(id)) {
                        throw new DuplicateResourceException("Location '" + location + "' already exists for this Business Unit");
                    }
                });

        StatusUtil.validate(request.getStatus());

        existing.setBgId(request.getBgId());
        existing.setBuId(request.getBuId());
        existing.setLocation(location);
        existing.setShortName(request.getShortName() != null ? request.getShortName().trim() : null);
        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }
        existing.setUpdatedApp(AppConstants.APP_NAME);

        return toResponse(userLocationRepository.save(existing));
    }

    @Override
    @Transactional
    public void deleteUserLocation(Long id) {
        AfmUserLocation existing = findLocation(id);
        userLocationRepository.delete(existing);
    }

    private AfmUserLocation findLocation(Long id) {
        return userLocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User Location not found with id: " + id));
    }

    private AfmUserLocationResponseDto toResponse(AfmUserLocation entity) {
        String groupName = businessGroupRepository.findById(entity.getBgId())
                .map(BusinessGroup::getGroupName)
                .orElse(null);

        String businessUnitName = businessUnitRepository.findById(entity.getBuId())
                .map(BusinessUnit::getBusinessUnit)
                .orElse(null);

        return AfmUserLocationResponseDto.builder()
                .id(entity.getId())
                .bgId(entity.getBgId())
                .groupName(groupName)
                .buId(entity.getBuId())
                .businessUnitName(businessUnitName)
                .location(entity.getLocation())
                .shortName(entity.getShortName())
                .status(entity.getStatus())
                .createdUserId(entity.getCreatedUserId())
                .createdEmpId(entity.getCreatedEmpId())
                .createdDatetime(entity.getCreatedDatetime())
                .updatedUserId(entity.getUpdatedUserId())
                .updatedEmpId(entity.getUpdatedEmpId())
                .updatedDatetime(entity.getUpdatedDatetime())
                .createdApp(entity.getCreatedApp())
                .updatedApp(entity.getUpdatedApp())
                .build();
    }

    private void validateParents(Long bgId, Long buId) {
        if (bgId == null) throw new BadRequestException("Business Group id cannot be empty");
        if (buId == null) throw new BadRequestException("Business Unit id cannot be empty");

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

