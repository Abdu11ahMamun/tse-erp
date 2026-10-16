package com.tse.erp.module.hr.service.impl;

import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.hr.entity.GpsInformation;
import com.tse.erp.module.hr.repository.EmpAttGpsRepository;
import com.tse.erp.module.hr.repository.GpsInformationRepository;
import com.tse.erp.module.hr.service.GpsInformationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GpsInformationServiceImpl
        implements GpsInformationService {

    private final GpsInformationRepository gpsInformationRepository;
    private final BusinessUnitRepository businessUnitRepository;
    private final EmpAttGpsRepository empAttGpsRepository;

    @Override
    public List<GpsInformation> getAllGpsInformations(Long buId) {
        if (buId == null) {
            return gpsInformationRepository.findAllByOrderByIdDesc();
        }
        return gpsInformationRepository.findByBuIdOrderByIdDesc(buId);
    }

    @Override
    public GpsInformation getGpsInformationById(Long id) {
        return gpsInformationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "GPS Information not found with id: " + id));
    }

    @Override
    public GpsInformation createGpsInformation(
            GpsInformation gpsInformation) {
        String name = prepare(gpsInformation);
        StatusUtil.validate(gpsInformation.getStatus());
        if (!gpsInformationRepository
                .findByBuIdAndLocationNameIgnoreCase(
                        gpsInformation.getBuId(), name).isEmpty()) {
            throw new DuplicateResourceException(
                    "Location name already exists: " + name);
        }
        gpsInformation.setId(null);
        gpsInformation.setLocationName(name);
        return gpsInformationRepository.save(gpsInformation);
    }

    @Override
    public GpsInformation updateGpsInformation(
            Long id, GpsInformation gpsInformation) {
        GpsInformation existing = getGpsInformationById(id);
        String name = prepare(gpsInformation);
        StatusUtil.validate(gpsInformation.getStatus());
        boolean duplicateExists = gpsInformationRepository
                .findByBuIdAndLocationNameIgnoreCase(
                        gpsInformation.getBuId(), name)
                .stream()
                .anyMatch(x -> !x.getId().equals(id));
        if (duplicateExists) {
            throw new DuplicateResourceException(
                    "Location name already exists: " + name);
        }

        existing.setBuId(gpsInformation.getBuId());
        existing.setLocationName(name);
        existing.setLatitude(gpsInformation.getLatitude());
        existing.setLongitude(gpsInformation.getLongitude());
        if (gpsInformation.getStatus() != null) {
            existing.setStatus(gpsInformation.getStatus());
        }
        return gpsInformationRepository.save(existing);
    }

    @Override
    public void deleteGpsInformation(Long id) {
        GpsInformation existing = getGpsInformationById(id);
        // TODO: block delete when attendance/geofence records use this location.
        if (empAttGpsRepository.existsByLocationIdAndStatus(id, 1)) {
            throw new BadRequestException(
                    "Cannot delete: GPS location is used in Employee Attendance Config");
        }
        gpsInformationRepository.delete(existing);
    }

    private String prepare(GpsInformation gpsInformation) {
        if (gpsInformation.getBuId() == null) {
            throw new BadRequestException(
                    "Business Unit id cannot be empty");
        }
        BusinessUnit bu = businessUnitRepository
                .findById(gpsInformation.getBuId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Business Unit not found with id: "
                                + gpsInformation.getBuId()));
        if (!Integer.valueOf(1).equals(bu.getStatus())) {
            throw new BadRequestException(
                    "Selected Business Unit is not active");
        }

        String locationName = gpsInformation.getLocationName();
        if (locationName == null || locationName.trim().isEmpty()) {
            throw new BadRequestException("Location name cannot be empty");
        }
        String name = locationName.trim();
        if (name.length() < 2) {
            throw new BadRequestException(
                    "Location name must be at least 2 characters");
        }
        if (name.length() > 200) {
            throw new BadRequestException(
                    "Location name cannot exceed 200 characters");
        }

        BigDecimal latitude = gpsInformation.getLatitude();
        if (latitude == null) {
            throw new BadRequestException("Latitude cannot be empty");
        }
        if (latitude.compareTo(new BigDecimal("-90")) < 0
                || latitude.compareTo(new BigDecimal("90")) > 0) {
            throw new BadRequestException(
                    "Latitude must be between -90 and 90");
        }

        BigDecimal longitude = gpsInformation.getLongitude();
        if (longitude == null) {
            throw new BadRequestException("Longitude cannot be empty");
        }
        if (longitude.compareTo(new BigDecimal("-180")) < 0
                || longitude.compareTo(new BigDecimal("180")) > 0) {
            throw new BadRequestException(
                    "Longitude must be between -180 and 180");
        }

        gpsInformation.setLatitude(
                latitude.setScale(7, RoundingMode.HALF_UP));
        gpsInformation.setLongitude(
                longitude.setScale(7, RoundingMode.HALF_UP));
        return name;
    }
}
