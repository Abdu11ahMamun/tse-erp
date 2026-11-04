package com.tse.erp.module.accounting.service;

import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.accounting.dto.AfmUserSetupRequestDto;
import com.tse.erp.module.accounting.dto.AfmUserSetupResponseDto;
import com.tse.erp.module.accounting.dto.UserOptionDto;
import com.tse.erp.module.accounting.entity.AfmBankDtls;
import com.tse.erp.module.accounting.entity.AfmLookupValue;
import com.tse.erp.module.accounting.entity.AfmUserLocation;
import com.tse.erp.module.accounting.entity.AfmUserSetup;
import com.tse.erp.module.accounting.repository.AfmBankDtlsRepository;
import com.tse.erp.module.accounting.repository.AfmLookupValueRepository;
import com.tse.erp.module.accounting.repository.AfmUserLocationRepository;
import com.tse.erp.module.accounting.repository.AfmUserSetupRepository;
import com.tse.erp.module.accounting.service.impl.AfmUserSetupServiceImpl;
import com.tse.erp.module.admin.entity.BusinessGroup;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.entity.User;
import com.tse.erp.module.admin.entity.UserEmplMap;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.admin.repository.UserEmplMapRepository;
import com.tse.erp.module.admin.repository.UserRepository;
import com.tse.erp.module.hr.entity.Department;
import com.tse.erp.module.hr.entity.Employee;
import com.tse.erp.module.hr.repository.DepartmentRepository;
import com.tse.erp.module.hr.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AfmUserSetupServiceTest {

    @Mock
    private AfmUserSetupRepository userSetupRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserEmplMapRepository userEmplMapRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private BusinessGroupRepository businessGroupRepository;
    @Mock
    private BusinessUnitRepository businessUnitRepository;
    @Mock
    private AfmLookupValueRepository lookupValueRepository;
    @Mock
    private AfmUserLocationRepository userLocationRepository;
    @Mock
    private AfmBankDtlsRepository bankDtlsRepository;
    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private AfmUserSetupServiceImpl userSetupService;

    private BusinessGroup businessGroup;
    private BusinessUnit businessUnit;
    private User testUser;
    private Employee employee;
    private UserEmplMap map;

    @BeforeEach
    void setUp() {
        businessGroup = BusinessGroup.builder().id(1L).groupName("TSE Group").build();
        businessGroup.setStatus(1);

        businessUnit = BusinessUnit.builder().id(10L).bgId(1L).businessUnit("TSE Textiles").build();
        businessUnit.setStatus(1);

        testUser = User.builder().id(5L).userName("karim01").bgId(1L).buId(10L).build();
        testUser.setStatus(1);

        employee = Employee.builder().id(20L).employeeCode("EMP001").employeeName("Karim Khan").build();
        map = new UserEmplMap();
        map.setId(100L);
        map.setUserId(5L);
        map.setEmplId(20L);
        map.setStatus(1);
    }

    @Test
    void createUserSetup_success() {
        when(businessGroupRepository.findById(1L)).thenReturn(Optional.of(businessGroup));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(businessUnit));
        when(userRepository.findById(5L)).thenReturn(Optional.of(testUser));
        when(userSetupRepository.existsByBuIdAndUserId(10L, 5L)).thenReturn(false);

        AfmUserSetup savedEntity = AfmUserSetup.builder()
                .id(1L)
                .bgId(1L)
                .buId(10L)
                .userId(5L)
                .voucherDate("All")
                .voucherType("1,2")
                .voucherTypeDefault("1")
                .location("3,4")
                .locationDefault("3")
                .bankAccount("101,102")
                .bankAccountDefault("101")
                .costCenter("50,51")
                .costCenterDefault("50")
                .build();
        savedEntity.setStatus(1);

        when(userSetupRepository.save(any(AfmUserSetup.class))).thenReturn(savedEntity);
        when(userEmplMapRepository.findByUserIdOrderByIdAsc(5L)).thenReturn(List.of(map));
        when(employeeRepository.findById(20L)).thenReturn(Optional.of(employee));

        AfmUserSetupRequestDto request = AfmUserSetupRequestDto.builder()
                .bgId(1L)
                .buId(10L)
                .userId(5L)
                .voucherDate("All")
                .voucherType("1,2")
                .voucherTypeDefault("1")
                .location("3,4")
                .locationDefault("3")
                .bankAccount("101,102")
                .bankAccountDefault("101")
                .costCenter("50,51")
                .costCenterDefault("50")
                .status(1)
                .build();

        AfmUserSetupResponseDto response = userSetupService.createUserSetup(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUserName()).isEqualTo("karim01");
        assertThat(response.getUserDisplayLabel()).contains("karim01 : EMP001 : Karim Khan");
        verify(userSetupRepository).save(any(AfmUserSetup.class));
    }

    @Test
    void createUserSetup_duplicateUser_throwsException() {
        when(businessGroupRepository.findById(1L)).thenReturn(Optional.of(businessGroup));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(businessUnit));
        when(userRepository.findById(5L)).thenReturn(Optional.of(testUser));
        when(userSetupRepository.existsByBuIdAndUserId(10L, 5L)).thenReturn(true);

        AfmUserSetupRequestDto request = AfmUserSetupRequestDto.builder()
                .bgId(1L)
                .buId(10L)
                .userId(5L)
                .build();

        assertThatThrownBy(() -> userSetupService.createUserSetup(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("User Setup already exists");
    }

    @Test
    void getAvailableUsers_returnsCorrectUserOptionDtos() {
        when(userRepository.findByBgIdAndBuIdOrderByIdDesc(1L, 10L)).thenReturn(List.of(testUser));
        when(userEmplMapRepository.findByUserIdOrderByIdAsc(5L)).thenReturn(List.of(map));
        when(employeeRepository.findById(20L)).thenReturn(Optional.of(employee));

        List<UserOptionDto> users = userSetupService.getAvailableUsers(1L, 10L);

        assertThat(users).hasSize(1);
        assertThat(users.get(0).getUserId()).isEqualTo(5L);
        assertThat(users.get(0).getUserName()).isEqualTo("karim01");
        assertThat(users.get(0).getEmployeeCode()).isEqualTo("EMP001");
        assertThat(users.get(0).getEmployeeName()).isEqualTo("Karim Khan");
        assertThat(users.get(0).getDisplayLabel()).isEqualTo("karim01 : EMP001 : Karim Khan");
    }

    @Test
    void getUserSetupById_notFound_throwsException() {
        when(userSetupRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userSetupService.getUserSetupById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
