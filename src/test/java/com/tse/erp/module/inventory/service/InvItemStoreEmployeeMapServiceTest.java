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
import com.tse.erp.module.inventory.dto.AssignStoreEmployeesRequestDto;
import com.tse.erp.module.inventory.dto.InvItemStoreEmployeeMapDto;
import com.tse.erp.module.inventory.entity.InvItemStore;
import com.tse.erp.module.inventory.entity.InvItemStoreEmployeeMap;
import com.tse.erp.module.inventory.repository.InvItemStoreEmployeeMapRepository;
import com.tse.erp.module.inventory.repository.InvItemStoreRepository;
import com.tse.erp.module.inventory.service.impl.InvItemStoreEmployeeMapServiceImpl;
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
class InvItemStoreEmployeeMapServiceTest {

    @Mock
    private InvItemStoreEmployeeMapRepository storeEmployeeMapRepository;
    @Mock
    private InvItemStoreRepository storeRepository;
    @Mock
    private BusinessGroupRepository businessGroupRepository;
    @Mock
    private BusinessUnitRepository businessUnitRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private InvItemStoreEmployeeMapServiceImpl storeEmployeeMapService;

    private BusinessGroup businessGroup;
    private BusinessUnit businessUnit;
    private InvItemStore store;
    private Employee employee;

    @BeforeEach
    void setUp() {
        businessGroup = BusinessGroup.builder().id(1L).groupName("TSE Group").build();
        businessGroup.setStatus(1);

        businessUnit = BusinessUnit.builder().id(10L).bgId(1L).businessUnit("BD Corporation").build();
        businessUnit.setStatus(1);

        store = InvItemStore.builder().id(100L).bgId(1L).buId(10L).storeName("Central Store").storeCode("STR-001").build();
        store.setStatus(1);

        employee = Employee.builder().id(5001L).bgId(1L).buId(10L).employeeName("Md. Abdul Mannan").employeeCode("5001").build();
        employee.setStatus(1);
    }

    @Test
    void assignEmployees_success() {
        when(storeRepository.findById(100L)).thenReturn(Optional.of(store));
        when(employeeRepository.findById(5001L)).thenReturn(Optional.of(employee));
        when(storeEmployeeMapRepository.findByStoreIdAndEmployeeId(100L, 5001L)).thenReturn(Optional.empty());

        InvItemStoreEmployeeMap savedMap = InvItemStoreEmployeeMap.builder()
                .id(1L)
                .bgId(1L)
                .buId(10L)
                .storeId(100L)
                .employeeId(5001L)
                .build();
        savedMap.setStatus(1);
        when(storeEmployeeMapRepository.save(any(InvItemStoreEmployeeMap.class))).thenReturn(savedMap);
        when(storeEmployeeMapRepository.findByStoreIdOrderByIdAsc(100L)).thenReturn(List.of(savedMap));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(businessUnit));

        AssignStoreEmployeesRequestDto request = AssignStoreEmployeesRequestDto.builder()
                .storeId(100L)
                .employeeIds(List.of(5001L))
                .status(1)
                .build();

        List<InvItemStoreEmployeeMapDto> results = storeEmployeeMapService.assignEmployeesToStore(request);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getEmployeeName()).isEqualTo("Md. Abdul Mannan");
    }

    @Test
    void assignEmployees_nullStore_throwsBadRequest() {
        AssignStoreEmployeesRequestDto request = AssignStoreEmployeesRequestDto.builder()
                .employeeIds(List.of(5001L))
                .build();

        assertThatThrownBy(() -> storeEmployeeMapService.assignEmployeesToStore(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Store ID is required");
    }

    @Test
    void removeEmployee_success() {
        doNothing().when(storeEmployeeMapRepository).deleteByStoreIdAndEmployeeId(100L, 5001L);

        storeEmployeeMapService.removeEmployeeFromStore(100L, 5001L);

        verify(storeEmployeeMapRepository).deleteByStoreIdAndEmployeeId(100L, 5001L);
    }
}

