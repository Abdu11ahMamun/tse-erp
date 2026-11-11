package com.tse.erp.module.inventory.service;

import com.tse.erp.module.inventory.dto.AssignStoreEmployeesRequestDto;
import com.tse.erp.module.inventory.dto.InvItemStoreEmployeeMapDto;

import java.util.List;

public interface InvItemStoreEmployeeMapService {

    List<InvItemStoreEmployeeMapDto> getEmployeesByStoreId(Long storeId);

    List<InvItemStoreEmployeeMapDto> assignEmployeesToStore(AssignStoreEmployeesRequestDto request);

    void removeEmployeeFromStore(Long storeId, Long employeeId);

    void deleteEmployeeMap(Long mapId);

    InvItemStoreEmployeeMapDto updateMapStatus(Long mapId, Integer status);

    List<InvItemStoreEmployeeMapDto> syncStoreEmployees(Long storeId, Long bgId, Long buId, List<Long> employeeIds);
}

