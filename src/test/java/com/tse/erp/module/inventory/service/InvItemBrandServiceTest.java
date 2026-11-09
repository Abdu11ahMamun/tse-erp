package com.tse.erp.module.inventory.service;

import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.BusinessGroup;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.admin.repository.UserRepository;
import com.tse.erp.module.hr.repository.EmployeeRepository;
import com.tse.erp.module.inventory.dto.InvItemBrandRequestDto;
import com.tse.erp.module.inventory.dto.InvItemBrandResponseDto;
import com.tse.erp.module.inventory.entity.InvItemBrand;
import com.tse.erp.module.inventory.repository.InvItemBrandRepository;
import com.tse.erp.module.inventory.service.impl.InvItemBrandServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvItemBrandServiceTest {

    @Mock
    private InvItemBrandRepository itemBrandRepository;
    @Mock
    private BusinessGroupRepository businessGroupRepository;
    @Mock
    private BusinessUnitRepository businessUnitRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private InvItemBrandServiceImpl itemBrandService;

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
    void createItemBrand_success_withGeneratedCode() {
        when(businessGroupRepository.findById(1L)).thenReturn(Optional.of(businessGroup));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(businessUnit));
        when(itemBrandRepository.existsByBuIdAndBrandNameIgnoreCase(10L, "Samsung")).thenReturn(false);
        when(itemBrandRepository.countByBuId(10L)).thenReturn(0L);
        when(itemBrandRepository.existsByBuIdAndBrandCodeIgnoreCase(10L, "BRD-00001")).thenReturn(false);

        InvItemBrand saved = InvItemBrand.builder()
                .id(100L)
                .bgId(1L)
                .buId(10L)
                .brandName("Samsung")
                .brandCode("BRD-00001")
                .build();
        saved.setStatus(1);
        when(itemBrandRepository.save(any(InvItemBrand.class))).thenReturn(saved);

        InvItemBrandRequestDto request = InvItemBrandRequestDto.builder()
                .bgId(1L)
                .buId(10L)
                .brandName("Samsung")
                .status(1)
                .build();

        InvItemBrandResponseDto response = itemBrandService.createItemBrand(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getBrandName()).isEqualTo("Samsung");
        assertThat(response.getBrandCode()).isEqualTo("BRD-00001");
        assertThat(response.getGroupName()).isEqualTo("TSE Group");
        assertThat(response.getBusinessUnitName()).isEqualTo("TSE Textiles");
    }

    @Test
    void createItemBrand_duplicateName_throwsException() {
        when(businessGroupRepository.findById(1L)).thenReturn(Optional.of(businessGroup));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(businessUnit));
        when(itemBrandRepository.existsByBuIdAndBrandNameIgnoreCase(10L, "Sony")).thenReturn(true);

        InvItemBrandRequestDto request = InvItemBrandRequestDto.builder()
                .bgId(1L)
                .buId(10L)
                .brandName("Sony")
                .build();

        assertThatThrownBy(() -> itemBrandService.createItemBrand(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void createItemBrand_duplicateCode_throwsException() {
        when(businessGroupRepository.findById(1L)).thenReturn(Optional.of(businessGroup));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(businessUnit));
        when(itemBrandRepository.existsByBuIdAndBrandNameIgnoreCase(10L, "LG")).thenReturn(false);
        when(itemBrandRepository.existsByBuIdAndBrandCodeIgnoreCase(10L, "BRD-LG-01")).thenReturn(true);

        InvItemBrandRequestDto request = InvItemBrandRequestDto.builder()
                .bgId(1L)
                .buId(10L)
                .brandName("LG")
                .brandCode("BRD-LG-01")
                .build();

        assertThatThrownBy(() -> itemBrandService.createItemBrand(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void updateItemBrand_success() {
        InvItemBrand existing = InvItemBrand.builder()
                .id(100L)
                .bgId(1L)
                .buId(10L)
                .brandName("Old Brand")
                .brandCode("BRD-00001")
                .build();
        existing.setStatus(1);

        when(itemBrandRepository.findById(100L)).thenReturn(Optional.of(existing));
        when(businessGroupRepository.findById(1L)).thenReturn(Optional.of(businessGroup));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(businessUnit));
        when(itemBrandRepository.findByBuIdAndBrandNameIgnoreCase(10L, "Updated Brand"))
                .thenReturn(Optional.empty());
        when(itemBrandRepository.findByBuIdAndBrandCodeIgnoreCase(10L, "BRD-00001"))
                .thenReturn(Optional.of(existing));
        when(itemBrandRepository.save(any(InvItemBrand.class))).thenReturn(existing);

        InvItemBrandRequestDto request = InvItemBrandRequestDto.builder()
                .bgId(1L)
                .buId(10L)
                .brandName("Updated Brand")
                .brandCode("BRD-00001")
                .status(1)
                .build();

        InvItemBrandResponseDto response = itemBrandService.updateItemBrand(100L, request);

        assertThat(response.getBrandName()).isEqualTo("Updated Brand");
        verify(itemBrandRepository).save(existing);
    }

    @Test
    void deleteItemBrand_success() {
        InvItemBrand existing = InvItemBrand.builder().id(100L).build();
        when(itemBrandRepository.findById(100L)).thenReturn(Optional.of(existing));

        itemBrandService.deleteItemBrand(100L);

        verify(itemBrandRepository).delete(existing);
    }

    @Test
    void getItemBrandById_notFound_throwsException() {
        when(itemBrandRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemBrandService.getItemBrandById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

