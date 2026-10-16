package com.tse.erp.module.hr.service.impl;

import com.tse.erp.common.BaseEntity;
import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.hr.dto.EmpAttConfigDto;
import com.tse.erp.module.hr.entity.*;
import com.tse.erp.module.hr.repository.*;
import com.tse.erp.module.hr.service.EmpAttConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class EmpAttConfigServiceImpl implements EmpAttConfigService {

    private static final List<String> METHOD_ORDER =
            List.of("WIFI", "GPS", "MACHINE");

    private final EmpAttConfigRepository configRepository;
    private final EmpAttShiftRepository shiftMapRepository;
    private final EmpAttWifiRepository wifiMapRepository;
    private final EmpAttGpsRepository gpsMapRepository;
    private final EmployeeRepository employeeRepository;
    private final ShiftRepository shiftRepository;
    private final WifiInformationRepository wifiRepository;
    private final GpsInformationRepository gpsRepository;

    private record Selection(List<Long> shiftIds,
                             List<Long> wifiIds,
                             List<Long> gpsIds) {}

    // =========================================
    // READ
    // =========================================
    @Override
    public List<EmpAttConfigDto> getAll(Long buId) {
        List<EmpAttConfig> list = buId == null
                ? configRepository.findAllByOrderByIdDesc()
                : configRepository.findByBuIdOrderByIdDesc(buId);
        return list.stream().map(c -> toDto(c, false)).toList();
    }

    @Override
    public EmpAttConfigDto getById(Long id) {
        return toDto(findConfig(id), true);
    }

    // =========================================
    // CREATE
    // =========================================
    @Override
    @Transactional
    public EmpAttConfigDto create(EmpAttConfigDto dto) {

        StatusUtil.validate(dto.getStatus());

        Employee e = findEmployee(dto.getEmployeeId());
        if (!Integer.valueOf(1).equals(e.getStatus())) {
            throw new BadRequestException("Selected Employee is not active");
        }
        checkBu(dto, e);

        if (configRepository.findByEmployeeId(e.getId()).stream()
                .anyMatch(x -> Integer.valueOf(1).equals(x.getStatus()))) {
            throw new DuplicateResourceException(
                    "Attendance configuration already exists for this Employee");
        }

        EmpAttConfig c = new EmpAttConfig();
        Selection sel = apply(c, dto, e);
        c.setEmployeeId(e.getId());
        if (dto.getStatus() != null) {
            c.setStatus(dto.getStatus());
        }
        EmpAttConfig saved = configRepository.save(c);

        syncChildren(saved.getId(), sel);
        return toDto(saved, true);
    }

    // =========================================
    // UPDATE
    // =========================================
    @Override
    @Transactional
    public EmpAttConfigDto update(Long id, EmpAttConfigDto dto) {

        EmpAttConfig c = findConfig(id);
        StatusUtil.validate(dto.getStatus());

        // Employee change kora jay na
        if (dto.getEmployeeId() != null
                && !dto.getEmployeeId().equals(c.getEmployeeId())) {
            throw new BadRequestException("Employee cannot be changed");
        }
        Employee e = findEmployee(c.getEmployeeId());
        checkBu(dto, e);

        int effective = dto.getStatus() != null
                ? dto.getStatus()
                : (c.getStatus() == null ? 1 : c.getStatus());
        if (effective == 1 && configRepository.findByEmployeeId(e.getId())
                .stream().anyMatch(x -> !x.getId().equals(id)
                        && Integer.valueOf(1).equals(x.getStatus()))) {
            throw new DuplicateResourceException(
                    "Attendance configuration already exists for this Employee");
        }

        Selection sel = apply(c, dto, e);
        if (dto.getStatus() != null) {
            c.setStatus(dto.getStatus());
        }
        configRepository.save(c);

        syncChildren(id, sel);
        return toDto(c, true);
    }

    // =========================================
    // DELETE
    // =========================================
    @Override
    @Transactional
    public void delete(Long id) {
        EmpAttConfig c = findConfig(id);
        // TODO: attendance record thakle delete block
        shiftMapRepository.deleteAll(
                shiftMapRepository.findByAttConfigIdOrderByIdAsc(id));
        wifiMapRepository.deleteAll(
                wifiMapRepository.findByAttConfigIdOrderByIdAsc(id));
        gpsMapRepository.deleteAll(
                gpsMapRepository.findByAttConfigIdOrderByIdAsc(id));
        configRepository.delete(c);
    }

    // =========================================
    // VALIDATE + APPLY
    // =========================================
    private Selection apply(EmpAttConfig c, EmpAttConfigDto d, Employee e) {

        List<String> methods = parseMethods(d.getAttendanceMethods());
        boolean wifi = methods.contains("WIFI");
        boolean gps = methods.contains("GPS");

        // WIFI
        int wifiFlex = flag(d.getWifiFlexible(), "WIFI flexibility");
        int wifiIn = flag(d.getWifiInFlexible(), "WIFI in time flexible");
        int wifiOut = flag(d.getWifiOutFlexible(), "WIFI out time flexible");
        if (!wifi && (wifiFlex == 1 || wifiIn == 1 || wifiOut == 1)) {
            throw new BadRequestException(
                    "WIFI settings require WIFI in Attendance Permission");
        }
        if (wifiFlex == 0) {
            wifiIn = 0;
            wifiOut = 0;
        }

        // GPS
        int gpsFlex = flag(d.getGpsFlexible(), "GPS flexibility");
        int gpsIn = flag(d.getGpsInFlexible(), "GPS in time flexible");
        int gpsOut = flag(d.getGpsOutFlexible(), "GPS out time flexible");
        int tracking = flag(d.getGpsTracking(), "GPS tracking");
        if (!gps && (gpsFlex == 1 || gpsIn == 1 || gpsOut == 1
                || tracking == 1)) {
            throw new BadRequestException(
                    "GPS settings require GPS in Attendance Permission");
        }
        if (gpsFlex == 0) {
            gpsIn = 0;
            gpsOut = 0;
        }

        // Tracking window
        if (tracking == 1) {
            if (d.getTrackingFromTime() == null) {
                throw new BadRequestException(
                        "Tracking from time cannot be empty");
            }
            if (d.getTrackingToTime() == null) {
                throw new BadRequestException(
                        "Tracking to time cannot be empty");
            }
            if (d.getTrackingFromTime().equals(d.getTrackingToTime())) {
                throw new BadRequestException(
                        "Tracking from time and to time must be different");
            }
            c.setTrackingFromTime(d.getTrackingFromTime());
            c.setTrackingToTime(d.getTrackingToTime());
        } else {
            c.setTrackingFromTime(null);
            c.setTrackingToTime(null);
        }

        // Shift
        String mode = d.getShiftMode() == null || d.getShiftMode().isBlank()
                ? "SINGLE" : d.getShiftMode().trim().toUpperCase();
        if (!mode.equals("SINGLE") && !mode.equals("MULTIPLE")) {
            throw new BadRequestException(
                    "Attendance shift must be Single or Multiple");
        }
        List<Long> shiftIds = ids(d.getShiftIds(), "Shift");
        if (mode.equals("SINGLE") && shiftIds.size() != 1) {
            throw new BadRequestException(
                    "Exactly one shift must be selected for Single mode");
        }
        if (mode.equals("MULTIPLE") && shiftIds.size() < 2) {
            throw new BadRequestException(
                    "At least two shifts must be selected for Multiple mode");
        }
        for (Long sid : shiftIds) {
            Shift s = shiftRepository.findById(sid).orElseThrow(
                    () -> new ResourceNotFoundException(
                            "Shift not found with id: " + sid));
            if (!Integer.valueOf(1).equals(s.getStatus())) {
                throw new BadRequestException(
                        "Selected Shift is not active: " + sid);
            }
            if (!e.getBuId().equals(s.getBuId())) {
                throw new BadRequestException(
                        "Shift does not belong to the Employee's Business Unit: "
                                + sid);
            }
        }

        // WIFI rows
        List<Long> wifiIds = ids(d.getWifiIds(), "WiFi");
        if (!wifi && !wifiIds.isEmpty()) {
            throw new BadRequestException(
                    "WiFi rows require WIFI in Attendance Permission");
        }
        if (wifi && wifiIds.isEmpty()) {
            throw new BadRequestException(
                    "At least one WiFi must be selected");
        }
        for (Long wid : wifiIds) {
            WifiInformation w = wifiRepository.findById(wid).orElseThrow(
                    () -> new ResourceNotFoundException(
                            "Wifi Information not found with id: " + wid));
            if (!Integer.valueOf(1).equals(w.getStatus())) {
                throw new BadRequestException(
                        "Selected WiFi is not active: " + wid);
            }
            if (!e.getBuId().equals(w.getBuId())) {
                throw new BadRequestException(
                        "WiFi does not belong to the Employee's Business Unit: "
                                + wid);
            }
        }

        // GPS rows
        List<Long> gpsIds = ids(d.getGpsIds(), "GPS location");
        if (!gps && !gpsIds.isEmpty()) {
            throw new BadRequestException(
                    "GPS rows require GPS in Attendance Permission");
        }
        if (gps && gpsIds.isEmpty()) {
            throw new BadRequestException(
                    "At least one GPS location must be selected");
        }
        for (Long gid : gpsIds) {
            GpsInformation g = gpsRepository.findById(gid).orElseThrow(
                    () -> new ResourceNotFoundException(
                            "GPS Information not found with id: " + gid));
            if (!Integer.valueOf(1).equals(g.getStatus())) {
                throw new BadRequestException(
                        "Selected GPS location is not active: " + gid);
            }
            if (!e.getBuId().equals(g.getBuId())) {
                throw new BadRequestException(
                        "GPS location does not belong to the Employee's Business Unit: "
                                + gid);
            }
        }

        c.setBuId(e.getBuId());
        c.setAttendanceMethods(String.join(",", methods));
        c.setWifiFlexible(wifiFlex);
        c.setWifiInFlexible(wifiIn);
        c.setWifiOutFlexible(wifiOut);
        c.setGpsFlexible(gpsFlex);
        c.setGpsInFlexible(gpsIn);
        c.setGpsOutFlexible(gpsOut);
        c.setGpsTracking(tracking);
        c.setShiftMode(mode);

        return new Selection(shiftIds, wifiIds, gpsIds);
    }

    // =========================================
    // CHILD SYNC (soft remove / reactivate)
    // =========================================
    private void syncChildren(Long configId, Selection sel) {

        shiftMapRepository.saveAll(sync(
                shiftMapRepository.findByAttConfigIdOrderByIdAsc(configId),
                sel.shiftIds(),
                EmpAttShift::getShiftId,
                ref -> {
                    EmpAttShift r = new EmpAttShift();
                    r.setAttConfigId(configId);
                    r.setShiftId(ref);
                    return r;
                }));

        wifiMapRepository.saveAll(sync(
                wifiMapRepository.findByAttConfigIdOrderByIdAsc(configId),
                sel.wifiIds(),
                EmpAttWifi::getWifiId,
                ref -> {
                    EmpAttWifi r = new EmpAttWifi();
                    r.setAttConfigId(configId);
                    r.setWifiId(ref);
                    return r;
                }));

        gpsMapRepository.saveAll(sync(
                gpsMapRepository.findByAttConfigIdOrderByIdAsc(configId),
                sel.gpsIds(),
                EmpAttGps::getLocationId,
                ref -> {
                    EmpAttGps r = new EmpAttGps();
                    r.setAttConfigId(configId);
                    r.setLocationId(ref);
                    return r;
                }));
    }

    // Request-e thakle active, na thakle status 0, notun hole new row
    private <T extends BaseEntity> List<T> sync(
            List<T> existing, List<Long> wanted,
            Function<T, Long> refOf, Function<Long, T> create) {

        Set<Long> wantedSet = new HashSet<>(wanted);
        Set<Long> present = new HashSet<>();
        List<T> result = new ArrayList<>(existing);

        for (T row : existing) {
            Long ref = refOf.apply(row);
            present.add(ref);
            row.setStatus(wantedSet.contains(ref) ? 1 : 0);
        }
        for (Long ref : wanted) {
            if (!present.contains(ref)) {
                result.add(create.apply(ref));
            }
        }
        return result;
    }

    // =========================================
    // HELPERS
    // =========================================
    private EmpAttConfig findConfig(Long id) {
        return configRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(
                        "Employee Attendance Config not found with id: " + id));
    }

    private Employee findEmployee(Long employeeId) {
        if (employeeId == null) {
            throw new BadRequestException("Employee cannot be empty");
        }
        return employeeRepository.findById(employeeId).orElseThrow(
                () -> new ResourceNotFoundException(
                        "Employee not found with id: " + employeeId));
    }

    private void checkBu(EmpAttConfigDto d, Employee e) {
        if (d.getBuId() != null && !d.getBuId().equals(e.getBuId())) {
            throw new BadRequestException(
                    "Employee does not belong to the selected Business Unit");
        }
    }

    private int flag(Integer v, String label) {
        if (v == null) {
            return 0;
        }
        if (v != 0 && v != 1) {
            throw new BadRequestException(label + " must be 0 or 1");
        }
        return v;
    }

    private List<String> parseMethods(List<String> raw) {
        if (raw == null || raw.isEmpty()) {
            throw new BadRequestException(
                    "At least one attendance permission must be selected");
        }
        Set<String> set = new HashSet<>();
        for (String r : raw) {
            if (r == null || r.isBlank()) {
                throw new BadRequestException(
                        "Attendance permission cannot be empty");
            }
            String m = r.trim().toUpperCase();
            if (!METHOD_ORDER.contains(m)) {
                throw new BadRequestException(
                        "Invalid attendance permission: " + r);
            }
            if (!set.add(m)) {
                throw new DuplicateResourceException(
                        "Duplicate attendance permission: " + m);
            }
        }
        return METHOD_ORDER.stream().filter(set::contains).toList();
    }

    private List<Long> ids(List<Long> raw, String label) {
        if (raw == null) {
            return new ArrayList<>();
        }
        Set<Long> seen = new LinkedHashSet<>();
        for (Long id : raw) {
            if (id == null) {
                throw new BadRequestException(label + " cannot be empty");
            }
            if (!seen.add(id)) {
                throw new DuplicateResourceException(
                        "Duplicate " + label + " in request: " + id);
            }
        }
        return new ArrayList<>(seen);
    }

    // =========================================
    // ENTITY -> DTO
    // =========================================
    private EmpAttConfigDto toDto(EmpAttConfig c, boolean withDetails) {
        EmpAttConfigDto d = new EmpAttConfigDto();
        d.setId(c.getId());
        d.setBuId(c.getBuId());
        d.setEmployeeId(c.getEmployeeId());
        d.setAttendanceMethods(
                Arrays.asList(c.getAttendanceMethods().split(",")));
        d.setWifiFlexible(c.getWifiFlexible());
        d.setWifiInFlexible(c.getWifiInFlexible());
        d.setWifiOutFlexible(c.getWifiOutFlexible());
        d.setGpsFlexible(c.getGpsFlexible());
        d.setGpsInFlexible(c.getGpsInFlexible());
        d.setGpsOutFlexible(c.getGpsOutFlexible());
        d.setGpsTracking(c.getGpsTracking());
        d.setTrackingFromTime(c.getTrackingFromTime());
        d.setTrackingToTime(c.getTrackingToTime());
        d.setShiftMode(c.getShiftMode());
        d.setStatus(c.getStatus());

        employeeRepository.findById(c.getEmployeeId()).ifPresent(e -> {
            d.setEmployeeName(e.getEmployeeName());
            d.setEmployeeCode(e.getEmployeeCode());
            d.setEmpTypeId(e.getEmploymentTypeId());
            d.setDeptId(e.getDeptId());
            d.setDesigId(e.getDesigId());
        });

        if (!withDetails) {
            return d;
        }

        List<Long> shiftIds = new ArrayList<>();
        List<EmpAttConfigDto.ShiftItem> shifts = new ArrayList<>();
        for (EmpAttShift r : shiftMapRepository
                .findByAttConfigIdOrderByIdAsc(c.getId())) {
            if (!Integer.valueOf(1).equals(r.getStatus())) {
                continue;
            }
            shiftIds.add(r.getShiftId());
            EmpAttConfigDto.ShiftItem item = new EmpAttConfigDto.ShiftItem();
            item.setShiftId(r.getShiftId());
            shiftRepository.findById(r.getShiftId())
                    .ifPresent(s -> item.setShiftTitle(s.getShiftTitle()));
            shifts.add(item);
        }
        d.setShiftIds(shiftIds);
        d.setShifts(shifts);

        List<Long> wifiIds = new ArrayList<>();
        List<EmpAttConfigDto.WifiItem> wifis = new ArrayList<>();
        for (EmpAttWifi r : wifiMapRepository
                .findByAttConfigIdOrderByIdAsc(c.getId())) {
            if (!Integer.valueOf(1).equals(r.getStatus())) {
                continue;
            }
            wifiIds.add(r.getWifiId());
            EmpAttConfigDto.WifiItem item = new EmpAttConfigDto.WifiItem();
            item.setWifiId(r.getWifiId());
            wifiRepository.findById(r.getWifiId()).ifPresent(w -> {
                item.setWifiName(w.getWifiName());
                item.setMacAddress(w.getMacAddress());
                item.setStatus(w.getStatus());
            });
            wifis.add(item);
        }
        d.setWifiIds(wifiIds);
        d.setWifis(wifis);

        List<Long> gpsIds = new ArrayList<>();
        List<EmpAttConfigDto.GpsItem> locations = new ArrayList<>();
        for (EmpAttGps r : gpsMapRepository
                .findByAttConfigIdOrderByIdAsc(c.getId())) {
            if (!Integer.valueOf(1).equals(r.getStatus())) {
                continue;
            }
            gpsIds.add(r.getLocationId());
            EmpAttConfigDto.GpsItem item = new EmpAttConfigDto.GpsItem();
            item.setLocationId(r.getLocationId());
            gpsRepository.findById(r.getLocationId()).ifPresent(g -> {
                item.setLocationName(g.getLocationName());
                item.setLatitude(g.getLatitude());
                item.setLongitude(g.getLongitude());
                item.setStatus(g.getStatus());
            });
            locations.add(item);
        }
        d.setGpsIds(gpsIds);
        d.setGpsLocations(locations);

        return d;
    }
}