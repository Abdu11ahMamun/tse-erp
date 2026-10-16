package com.tse.erp.module.hr.service;

import com.tse.erp.module.hr.entity.Shift;

import java.util.List;

public interface ShiftService {

    List<Shift> getAllShifts(Long buId);

    Shift getShiftById(Long id);

    Shift createShift(Shift shift);

    Shift updateShift(Long id, Shift shift);

    void deleteShift(Long id);
}
