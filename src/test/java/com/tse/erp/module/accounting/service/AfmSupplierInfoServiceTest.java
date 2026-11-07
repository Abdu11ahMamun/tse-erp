package com.tse.erp.module.accounting.service;

import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.accounting.dto.AfmSupplierInfoRequestDto;
import com.tse.erp.module.accounting.dto.AfmSupplierInfoResponseDto;
import com.tse.erp.module.accounting.entity.AfmSupplierInfo;
import com.tse.erp.module.accounting.repository.AfmSupplierInfoRepository;
import com.tse.erp.module.accounting.service.impl.AfmSupplierInfoServiceImpl;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AfmSupplierInfoServiceTest {

    @Mock
    private AfmSupplierInfoRepository supplierInfoRepository;
    @Mock
    private BusinessGroupRepository businessGroupRepository;
    @Mock
    private BusinessUnitRepository businessUnitRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private AfmSupplierInfoServiceImpl supplierInfoService;

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
    void createSupplier_success() {
        when(businessGroupRepository.findById(1L)).thenReturn(Optional.of(businessGroup));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(businessUnit));
        when(supplierInfoRepository.existsByBuIdAndSupplierCodeIgnoreCase(10L, "SUP-00001")).thenReturn(false);

        AfmSupplierInfo saved = AfmSupplierInfo.builder()
                .id(100L)
                .bgId(1L)
                .buId(10L)
                .supplierCode("SUP-00001")
                .supplierName("Apex Supplies Ltd")
                .supplierType("Wholesaler")
                .businessType("Private Limited")
                .phone("+8801700000000")
                .email("apex@example.com")
                .creditLimit(new BigDecimal("50000.00"))
                .creditDays(30)
                .currencyCode("BDT")
                .build();
        saved.setStatus(1);

        when(supplierInfoRepository.save(any(AfmSupplierInfo.class))).thenReturn(saved);

        AfmSupplierInfoRequestDto request = AfmSupplierInfoRequestDto.builder()
                .bgId(1L)
                .buId(10L)
                .supplierCode("SUP-00001")
                .supplierName("Apex Supplies Ltd")
                .supplierType("Wholesaler")
                .businessType("Private Limited")
                .phone("+8801700000000")
                .email("apex@example.com")
                .creditLimit(new BigDecimal("50000.00"))
                .creditDays(30)
                .currencyCode("BDT")
                .status(1)
                .build();

        AfmSupplierInfoResponseDto response = supplierInfoService.createSupplier(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getSupplierName()).isEqualTo("Apex Supplies Ltd");
        assertThat(response.getSupplierCode()).isEqualTo("SUP-00001");
        verify(supplierInfoRepository).save(any(AfmSupplierInfo.class));
    }

    @Test
    void createSupplier_duplicateCode_throwsException() {
        when(businessGroupRepository.findById(1L)).thenReturn(Optional.of(businessGroup));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(businessUnit));
        when(supplierInfoRepository.existsByBuIdAndSupplierCodeIgnoreCase(10L, "SUP-00001")).thenReturn(true);

        AfmSupplierInfoRequestDto request = AfmSupplierInfoRequestDto.builder()
                .bgId(1L)
                .buId(10L)
                .supplierCode("SUP-00001")
                .supplierName("Apex Supplies Ltd")
                .build();

        assertThatThrownBy(() -> supplierInfoService.createSupplier(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Supplier Code already exists");
    }

    @Test
    void getSupplierById_notFound_throwsException() {
        when(supplierInfoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> supplierInfoService.getSupplierById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void generateNextSupplierCode_generatesFormattedSequence() {
        when(supplierInfoRepository.countByBuId(10L)).thenReturn(5L);
        when(supplierInfoRepository.existsByBuIdAndSupplierCodeIgnoreCase(10L, "SUP-00006")).thenReturn(false);

        String nextCode = supplierInfoService.generateNextSupplierCode(10L);
        assertThat(nextCode).isEqualTo("SUP-00006");
    }
}

