package com.tse.erp.module.inventory.service;

import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.BusinessGroup;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.admin.repository.UserRepository;
import com.tse.erp.module.hr.entity.Employee;
import com.tse.erp.module.hr.repository.EmployeeRepository;
import com.tse.erp.module.inventory.dto.AssignWarehouseEmployeesRequestDto;
import com.tse.erp.module.inventory.dto.InvItemWarehouseEmployeeMapDto;
import com.tse.erp.module.inventory.entity.InvItemWarehouse;
import com.tse.erp.module.inventory.entity.InvItemWarehouseEmployeeMap;
import com.tse.erp.module.inventory.repository.InvItemWarehouseEmployeeMapRepository;
import com.tse.erp.module.inventory.repository.InvItemWarehouseRepository;
import com.tse.erp.module.inventory.service.impl.InvItemWarehouseEmployeeMapServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvItemWarehouseEmployeeMapServiceTest {

    @Mock
    private InvItemWarehouseEmployeeMapRepository employeeMapRepository;
    @Mock
    private InvItemWarehouseRepository warehouseRepository;
    @Mock
    private BusinessGroupRepository businessGroupRepository;
    @Mock
    private BusinessUnitRepository businessUnitRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private InvItemWarehouseEmployeeMapServiceImpl employeeMapService;

    private BusinessGroup businessGroup;
    private BusinessUnit businessUnit;
    private InvItemWarehouse warehouse;
    private Employee employee;

    @BeforeEach
    void setUp() {
        businessGroup = BusinessGroup.builder().id(1L).groupName("TSE Group").build();
        businessGroup.setStatus(1);

        businessUnit = BusinessUnit.builder().id(10L).bgId(1L).businessUnit("BD Corporation").build();
        businessUnit.setStatus(1);

        warehouse = InvItemWarehouse.builder().id(100L).bgId(1L).buId(10L).warehouseName("Uttara WareHouse").warehouseCode("WH-001").build();
        warehouse.setStatus(1);

        employee = Employee.builder().id(5001L).bgId(1L).buId(10L).employeeName("Md. Abdul Mannan").employeeCode("5001").build();
        employee.setStatus(1);
    }

    @Test
    void assignEmployees_success() {
        when(warehouseRepository.findById(100L)).thenReturn(Optional.of(warehouse));
        when(employeeRepository.findById(5001L)).thenReturn(Optional.of(employee));
        when(employeeMapRepository.findByWarehouseIdAndEmployeeId(100L, 5001L)).thenReturn(Optional.empty());

        InvItemWarehouseEmployeeMap savedMap = InvItemWarehouseEmployeeMap.builder()
                .id(1L)
                .bgId(1L)
                .buId(10L)
                .warehouseId(100L)
                .employeeId(5001L)
                .build();
        savedMap.setStatus(1);
        when(employeeMapRepository.save(any(InvItemWarehouseEmployeeMap.class))).thenReturn(savedMap);
        when(employeeMapRepository.findByWarehouseIdOrderByIdAsc(100L)).thenReturn(List.of(savedMap));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(businessUnit));

        AssignWarehouseEmployeesRequestDto request = AssignWarehouseEmployeesRequestDto.builder()
                .warehouseId(100L)
                .employeeIds(List.of(5001L))
                .status(1)
                .build();

        List<InvItemWarehouseEmployeeMapDto> results = employeeMapService.assignEmployeesToWarehouse(request);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getEmployeeId()).isEqualTo(5001L);
        assertThat(results.get(0).getEmployeeDisplay()).isEqualTo("Md. Abdul Mannan (5001)");
    }

    @Test
    void assignEmployees_warehouseNotFound() {
        when(warehouseRepository.findById(999L)).thenReturn(Optional.empty());

        AssignWarehouseEmployeesRequestDto request = AssignWarehouseEmployeesRequestDto.builder()
                .warehouseId(999L)
                .employeeIds(List.of(5001L))
                .build();

        assertThatThrownBy(() -> employeeMapService.assignEmployeesToWarehouse(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Warehouse not found");
    }

    @Test
    void deleteEmployeeMap_success() {
        InvItemWarehouseEmployeeMap map = InvItemWarehouseEmployeeMap.builder()
                .id(1L)
                .warehouseId(100L)
                .employeeId(5001L)
                .build();
        when(employeeMapRepository.findById(1L)).thenReturn(Optional.of(map));

        employeeMapService.deleteEmployeeMap(1L);

        verify(employeeMapRepository, times(1)).delete(map);
    }
}
