package com.tse.erp.module.hr.service.impl;

import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.hr.entity.WifiInformation;
import com.tse.erp.module.hr.repository.EmpAttWifiRepository;
import com.tse.erp.module.hr.repository.WifiInformationRepository;
import com.tse.erp.module.hr.service.WifiInformationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WifiInformationServiceImpl
        implements WifiInformationService {

    private final WifiInformationRepository wifiInformationRepository;
    private final BusinessUnitRepository businessUnitRepository;
    private final EmpAttWifiRepository empAttWifiRepository;

    @Override
    public List<WifiInformation> getAllWifiInformations(Long buId) {
        if (buId == null) {
            return wifiInformationRepository.findAllByOrderByIdDesc();
        }
        return wifiInformationRepository.findByBuIdOrderByIdDesc(buId);
    }

    @Override
    public WifiInformation getWifiInformationById(Long id) {
        return wifiInformationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Wifi Information not found with id: " + id));
    }

    @Override
    public WifiInformation createWifiInformation(
            WifiInformation wifiInformation) {
        String[] values = prepare(wifiInformation);
        StatusUtil.validate(wifiInformation.getStatus());
        String name = values[0];
        String mac = values[1];
        if (!wifiInformationRepository.findByMacAddressIgnoreCase(mac)
                .isEmpty()) {
            throw new DuplicateResourceException(
                    "MAC address already exists: " + mac);
        }
        wifiInformation.setId(null);
        wifiInformation.setWifiName(name);
        wifiInformation.setMacAddress(mac);
        return wifiInformationRepository.save(wifiInformation);
    }

    @Override
    public WifiInformation updateWifiInformation(
            Long id, WifiInformation wifiInformation) {
        WifiInformation existing = getWifiInformationById(id);
        String[] values = prepare(wifiInformation);
        StatusUtil.validate(wifiInformation.getStatus());
        String name = values[0];
        String mac = values[1];
        boolean duplicateExists = wifiInformationRepository
                .findByMacAddressIgnoreCase(mac)
                .stream()
                .anyMatch(x -> !x.getId().equals(id));
        if (duplicateExists) {
            throw new DuplicateResourceException(
                    "MAC address already exists: " + mac);
        }

        existing.setBuId(wifiInformation.getBuId());
        existing.setWifiName(name);
        existing.setMacAddress(mac);
        if (wifiInformation.getStatus() != null) {
            existing.setStatus(wifiInformation.getStatus());
        }
        return wifiInformationRepository.save(existing);
    }

    @Override
    public void deleteWifiInformation(Long id) {
        WifiInformation existing = getWifiInformationById(id);
        // TODO: block delete when attendance records use this wifi.
        if (empAttWifiRepository.existsByWifiIdAndStatus(id, 1)) {
            throw new BadRequestException(
                    "Cannot delete: WiFi is used in Employee Attendance Config");
        }
        wifiInformationRepository.delete(existing);
    }

    private String[] prepare(WifiInformation wifiInformation) {
        if (wifiInformation.getBuId() == null) {
            throw new BadRequestException(
                    "Business Unit id cannot be empty");
        }
        BusinessUnit bu = businessUnitRepository
                .findById(wifiInformation.getBuId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Business Unit not found with id: "
                                + wifiInformation.getBuId()));
        if (!Integer.valueOf(1).equals(bu.getStatus())) {
            throw new BadRequestException(
                    "Selected Business Unit is not active");
        }

        String wifiName = wifiInformation.getWifiName();
        if (wifiName == null || wifiName.trim().isEmpty()) {
            throw new BadRequestException("Wifi name cannot be empty");
        }
        String name = wifiName.trim();
        if (name.length() < 2) {
            throw new BadRequestException(
                    "Wifi name must be at least 2 characters");
        }
        if (name.length() > 100) {
            throw new BadRequestException(
                    "Wifi name cannot exceed 100 characters");
        }

        String macAddress = wifiInformation.getMacAddress();
        if (macAddress == null || macAddress.trim().isEmpty()) {
            throw new BadRequestException("MAC address cannot be empty");
        }
        String mac = macAddress.trim().toUpperCase().replace('-', ':');
        if (!mac.matches("^([0-9A-F]{2}:){5}[0-9A-F]{2}$")) {
            throw new BadRequestException(
                    "Invalid MAC address. Use format AA:BB:CC:DD:EE:FF");
        }
        return new String[]{name, mac};
    }
}
