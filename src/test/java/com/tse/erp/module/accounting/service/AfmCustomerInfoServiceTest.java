package com.tse.erp.module.accounting.service;

import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.accounting.dto.AfmCustomerInfoRequestDto;
import com.tse.erp.module.accounting.dto.AfmCustomerInfoResponseDto;
import com.tse.erp.module.accounting.entity.AfmCustomerInfo;
import com.tse.erp.module.accounting.repository.AfmCustomerInfoRepository;
import com.tse.erp.module.accounting.service.impl.AfmCustomerInfoServiceImpl;
import com.tse.erp.module.admin.entity.BusinessGroup;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.admin.repository.UserRepository;
import com.tse.erp.module.hr.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AfmCustomerInfoServiceTest {

    @Mock
    private AfmCustomerInfoRepository customerInfoRepository;
    @Mock
    private BusinessGroupRepository businessGroupRepository;
    @Mock
    private BusinessUnitRepository businessUnitRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private AfmCustomerInfoServiceImpl customerInfoService;

    private BusinessGroup businessGroup;
    private BusinessUnit businessUnit;

    @BeforeEach
    void setUp() {
        businessGroup = BusinessGroup.builder().id(1L).groupName("TSE Group").build();
        businessGroup.setStatus(1);

        businessUnit = BusinessUnit.builder().id(10L).bgId(1L).businessUnit("TSE Textiles").build();
        businessUnit.setStatus(1);
    }

    @Test
    void createCustomer_success() {
        when(businessGroupRepository.findById(1L)).thenReturn(Optional.of(businessGroup));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(businessUnit));
        when(customerInfoRepository.existsByBuIdAndCustomerCodeIgnoreCase(10L, "CUS-00001")).thenReturn(false);

        AfmCustomerInfo saved = AfmCustomerInfo.builder()
                .id(200L)
                .bgId(1L)
                .buId(10L)
                .customerCode("CUS-00001")
                .customerName("Rahim Corp")
                .customerType("Corporate")
                .businessType("Private Limited")
                .phone("+8801700000000")
                .email("rahim@example.com")
                .creditLowerLimit(new BigDecimal("1000.50"))
                .creditUpperLimit(new BigDecimal("100000.75"))
                .agreementDateFrom(LocalDate.of(2025, 1, 1))
                .agreementDateTo(LocalDate.of(2025, 12, 31))
                .financeStatus("Draft")
                .creditDays(45)
                .currencyCode("BDT")
                .shippingAddress("Chittagong Port Warehouse")
                .build();
        saved.setStatus(1);

        when(customerInfoRepository.save(any(AfmCustomerInfo.class))).thenReturn(saved);

        AfmCustomerInfoRequestDto request = AfmCustomerInfoRequestDto.builder()
                .bgId(1L)
                .buId(10L)
                .customerCode("CUS-00001")
                .customerName("Rahim Corp")
                .customerType("Corporate")
                .businessType("Private Limited")
                .phone("+8801700000000")
                .email("rahim@example.com")
                .creditLowerLimit(new BigDecimal("1000.50"))
                .creditUpperLimit(new BigDecimal("100000.75"))
                .agreementDateFrom(LocalDate.of(2025, 1, 1))
                .agreementDateTo(LocalDate.of(2025, 12, 31))
                .financeStatus("Draft")
                .creditDays(45)
                .currencyCode("BDT")
                .shippingAddress("Chittagong Port Warehouse")
                .status(1)
                .build();

        AfmCustomerInfoResponseDto response = customerInfoService.createCustomer(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(200L);
        assertThat(response.getCustomerName()).isEqualTo("Rahim Corp");
        assertThat(response.getCustomerCode()).isEqualTo("CUS-00001");
        assertThat(response.getAgreementDateFrom()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(response.getAgreementDateTo()).isEqualTo(LocalDate.of(2025, 12, 31));
        assertThat(response.getShippingAddress()).isEqualTo("Chittagong Port Warehouse");
        verify(customerInfoRepository).save(any(AfmCustomerInfo.class));
    }

    @Test
    void createCustomer_duplicateCode_throwsException() {
        when(businessGroupRepository.findById(1L)).thenReturn(Optional.of(businessGroup));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(businessUnit));
        when(customerInfoRepository.existsByBuIdAndCustomerCodeIgnoreCase(10L, "CUS-00001")).thenReturn(true);

        AfmCustomerInfoRequestDto request = AfmCustomerInfoRequestDto.builder()
                .bgId(1L)
                .buId(10L)
                .customerCode("CUS-00001")
                .customerName("Rahim Corp")
                .build();

        assertThatThrownBy(() -> customerInfoService.createCustomer(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Customer Code already exists");
    }

    @Test
    void getCustomerById_notFound_throwsException() {
        when(customerInfoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerInfoService.getCustomerById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void generateNextCustomerCode_generatesFormattedSequence() {
        when(customerInfoRepository.countByBuId(10L)).thenReturn(5L);
        when(customerInfoRepository.existsByBuIdAndCustomerCodeIgnoreCase(10L, "CUS-00006")).thenReturn(false);

        String nextCode = customerInfoService.generateNextCustomerCode(10L);
        assertThat(nextCode).isEqualTo("CUS-00006");
    }
}

