package com.tse.erp.module.inventory.service;

import com.tse.erp.module.inventory.dto.AssignWarehouseEmployeesRequestDto;
import com.tse.erp.module.inventory.dto.InvItemWarehouseEmployeeMapDto;

import java.util.List;

public interface InvItemWarehouseEmployeeMapService {

    List<InvItemWarehouseEmployeeMapDto> getEmployeesByWarehouseId(Long warehouseId);

    List<InvItemWarehouseEmployeeMapDto> assignEmployeesToWarehouse(AssignWarehouseEmployeesRequestDto request);

    void removeEmployeeFromWarehouse(Long warehouseId, Long employeeId);

    void deleteEmployeeMap(Long mapId);

    InvItemWarehouseEmployeeMapDto updateMapStatus(Long mapId, Integer status);

    List<InvItemWarehouseEmployeeMapDto> syncWarehouseEmployees(Long warehouseId, Long bgId, Long buId, List<Long> employeeIds);
}
