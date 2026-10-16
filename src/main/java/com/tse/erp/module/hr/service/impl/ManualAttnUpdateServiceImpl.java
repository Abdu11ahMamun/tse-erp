package com.tse.erp.module.hr.service.impl;

import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.hr.entity.Department;
import com.tse.erp.module.hr.entity.Designation;
import com.tse.erp.module.hr.entity.ManualAttnUpdate;
import com.tse.erp.module.hr.entity.Shift;
import com.tse.erp.module.hr.repository.*;
import com.tse.erp.module.hr.service.ManualAttnUpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ManualAttnUpdateServiceImpl implements ManualAttnUpdateService {

    private final ManualAttnUpdateRepository repository;
    private final BusinessUnitRepository businessUnitRepository;
    private final DepartmentRepository departmentRepository;
    private final DepartmentGroupRepository departmentGroupRepository;
    private final DesignationRepository designationRepository;
    private final DesignationGroupRepository designationGroupRepository;
    private final DeptDesigMapRepository deptDesigMapRepository;
    private final ShiftRepository shiftRepository;

    @Override
    public List<ManualAttnUpdate> getAll(Long buId, LocalDate attenDate) {
        if (buId != null && attenDate != null) {
            return repository.findByBuIdAndAttenDateOrderByIdDesc(
                    buId, attenDate);
        }
        if (buId != null) {
            return repository.findByBuIdOrderByIdDesc(buId);
        }
        if (attenDate != null) {
            return repository.findByAttenDateOrderByIdDesc(attenDate);
        }
        return repository.findAllByOrderByIdDesc();
    }

    @Override
    public ManualAttnUpdate getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Manual Attendance Update not found with id: " + id));
    }

    @Override
    public ManualAttnUpdate create(ManualAttnUpdate r) {

        prepare(r);
        StatusUtil.validate(r.getStatus());

        int effective = r.getStatus() == null ? 1 : r.getStatus();
        if (effective == 1) {
            checkDuplicate(r, null);
        }

        r.setId(null);

        // Status default 1, BaseEntity @PrePersist handle korbe
        return repository.save(r);
    }

    @Override
    public ManualAttnUpdate update(Long id, ManualAttnUpdate r) {

        ManualAttnUpdate existing = getById(id);

        prepare(r);
        StatusUtil.validate(r.getStatus());

        int effective = r.getStatus() != null
                ? r.getStatus()
                : (existing.getStatus() == null ? 1 : existing.getStatus());
        if (effective == 1) {
            checkDuplicate(r, id);
        }

        existing.setBgId(r.getBgId());
        existing.setBuId(r.getBuId());
        existing.setDeptId(r.getDeptId());
        existing.setDesigId(r.getDesigId());
        existing.setShiftId(r.getShiftId());
        existing.setAttenDate(r.getAttenDate());
        existing.setInTime(r.getInTime());
        existing.setOutTime(r.getOutTime());
        existing.setNote(r.getNote());
        if (r.getStatus() != null) {
            existing.setStatus(r.getStatus());
        }

        return repository.save(existing);
    }

    @Override
    public void delete(Long id) {
        ManualAttnUpdate existing = getById(id);
        repository.delete(existing);
    }

    // =========================================
    // VALIDATE + CLEAN
    // =========================================
    private void prepare(ManualAttnUpdate r) {

        // Business Unit (BG BU theke ashe)
        if (r.getBuId() == null) {
            throw new BadRequestException("Business Unit id cannot be empty");
        }
        BusinessUnit bu = businessUnitRepository.findById(r.getBuId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Business Unit not found with id: " + r.getBuId()));
        if (!Integer.valueOf(1).equals(bu.getStatus())) {
            throw new BadRequestException("Selected Business Unit is not active");
        }
        Long bgId = bu.getBgId();

        // Department
        if (r.getDeptId() == null) {
            throw new BadRequestException("Department cannot be empty");
        }
        Department dept = departmentRepository.findById(r.getDeptId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with id: " + r.getDeptId()));
        if (!Integer.valueOf(1).equals(dept.getStatus())) {
            throw new BadRequestException("Selected Department is not active");
        }
        if (!bgId.equals(dept.getBgId())) {
            throw new BadRequestException(
                    "Department does not belong to the Business Unit's Group");
        }
        if (departmentGroupRepository
                .findByDeptIdAndBuId(r.getDeptId(), r.getBuId()).isEmpty()) {
            throw new BadRequestException(
                    "Department is not mapped to selected Business Unit");
        }

        // Designation
        if (r.getDesigId() == null) {
            throw new BadRequestException("Designation cannot be empty");
        }
        Designation desig = designationRepository.findById(r.getDesigId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Designation not found with id: " + r.getDesigId()));
        if (!Integer.valueOf(1).equals(desig.getStatus())) {
            throw new BadRequestException("Selected Designation is not active");
        }
        if (!bgId.equals(desig.getBgId())) {
            throw new BadRequestException(
                    "Designation does not belong to the Business Unit's Group");
        }
        if (designationGroupRepository
                .findByDesigIdAndBuId(r.getDesigId(), r.getBuId()).isEmpty()) {
            throw new BadRequestException(
                    "Designation is not mapped to selected Business Unit");
        }
        if (deptDesigMapRepository.findByBuIdAndDeptIdAndDesigId(
                r.getBuId(), r.getDeptId(), r.getDesigId()).isEmpty()) {
            throw new BadRequestException(
                    "Designation is not mapped to selected Department");
        }

        // Shift
        if (r.getShiftId() == null) {
            throw new BadRequestException("Shift cannot be empty");
        }
        Shift shift = shiftRepository.findById(r.getShiftId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Shift not found with id: " + r.getShiftId()));
        if (!Integer.valueOf(1).equals(shift.getStatus())) {
            throw new BadRequestException("Selected Shift is not active");
        }
        if (!r.getBuId().equals(shift.getBuId())) {
            throw new BadRequestException(
                    "Shift does not belong to selected Business Unit");
        }

        // Date
        if (r.getAttenDate() == null) {
            throw new BadRequestException("Attendance date cannot be empty");
        }
        if (r.getAttenDate().isAfter(LocalDate.now())) {
            throw new BadRequestException(
                    "Attendance date cannot be a future date");
        }

        // Times
        if (r.getInTime() == null) {
            throw new BadRequestException("In time cannot be empty");
        }
        if (r.getOutTime() != null) {
            if (r.getOutTime().equals(r.getInTime())) {
                throw new BadRequestException(
                        "Out time must be different from in time");
            }
            if (r.getOutTime().isBefore(r.getInTime())
                    && !Integer.valueOf(1).equals(shift.getAllowNextDayAttn())) {
                throw new BadRequestException(
                        "Out time must be later than in time "
                                + "(overnight needs Next Day Attendance on the Shift)");
            }
        }

        // Note
        String note = null;
        if (r.getNote() != null && !r.getNote().trim().isEmpty()) {
            note = r.getNote().trim();
            if (note.length() > 500) {
                throw new BadRequestException(
                        "Note cannot exceed 500 characters");
            }
            if (note.indexOf('<') >= 0 || note.indexOf('>') >= 0) {
                throw new BadRequestException(
                        "Note contains unsupported characters");
            }
        }

        r.setBgId(bgId);
        r.setNote(note);
    }

    private void checkDuplicate(ManualAttnUpdate r, Long selfId) {
        boolean duplicate = repository
                .findByBuIdAndDeptIdAndDesigIdAndShiftIdAndAttenDate(
                        r.getBuId(), r.getDeptId(), r.getDesigId(),
                        r.getShiftId(), r.getAttenDate())
                .stream()
                .anyMatch(x -> Integer.valueOf(1).equals(x.getStatus())
                        && (selfId == null || !x.getId().equals(selfId)));
        if (duplicate) {
            throw new DuplicateResourceException(
                    "An active manual attendance already exists for this "
                            + "Business Unit, Department, Designation, Shift and Date");
        }
    }
}