package com.tse.erp.module.inventory.service;

import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.BusinessGroup;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.admin.repository.UserRepository;
import com.tse.erp.module.hr.repository.EmployeeRepository;
import com.tse.erp.module.inventory.dto.InvItemCategoryRequestDto;
import com.tse.erp.module.inventory.dto.InvItemCategoryResponseDto;
import com.tse.erp.module.inventory.entity.InvItemCategory;
import com.tse.erp.module.inventory.repository.InvItemCategoryRepository;
import com.tse.erp.module.inventory.service.impl.InvItemCategoryServiceImpl;
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
class InvItemCategoryServiceTest {

    @Mock
    private InvItemCategoryRepository itemCategoryRepository;
    @Mock
    private BusinessGroupRepository businessGroupRepository;
    @Mock
    private BusinessUnitRepository businessUnitRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private InvItemCategoryServiceImpl itemCategoryService;

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
    void createItemCategory_success_withGeneratedCode() {
        when(businessGroupRepository.findById(1L)).thenReturn(Optional.of(businessGroup));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(businessUnit));
        when(itemCategoryRepository.existsByBuIdAndCategoryNameIgnoreCase(10L, "Electronics")).thenReturn(false);
        when(itemCategoryRepository.countByBuId(10L)).thenReturn(0L);
        when(itemCategoryRepository.existsByBuIdAndCategoryCodeIgnoreCase(10L, "CAT-00001")).thenReturn(false);

        InvItemCategory saved = InvItemCategory.builder()
                .id(100L)
                .bgId(1L)
                .buId(10L)
                .categoryName("Electronics")
                .categoryCode("CAT-00001")
                .build();
        saved.setStatus(1);
        when(itemCategoryRepository.save(any(InvItemCategory.class))).thenReturn(saved);

        InvItemCategoryRequestDto request = InvItemCategoryRequestDto.builder()
                .bgId(1L)
                .buId(10L)
                .categoryName("Electronics")
                .status(1)
                .build();

        InvItemCategoryResponseDto response = itemCategoryService.createItemCategory(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getCategoryName()).isEqualTo("Electronics");
        assertThat(response.getCategoryCode()).isEqualTo("CAT-00001");
        assertThat(response.getGroupName()).isEqualTo("TSE Group");
        assertThat(response.getBusinessUnitName()).isEqualTo("TSE Textiles");
    }

    @Test
    void createItemCategory_duplicateName_throwsException() {
        when(businessGroupRepository.findById(1L)).thenReturn(Optional.of(businessGroup));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(businessUnit));
        when(itemCategoryRepository.existsByBuIdAndCategoryNameIgnoreCase(10L, "Raw Materials")).thenReturn(true);

        InvItemCategoryRequestDto request = InvItemCategoryRequestDto.builder()
                .bgId(1L)
                .buId(10L)
                .categoryName("Raw Materials")
                .build();

        assertThatThrownBy(() -> itemCategoryService.createItemCategory(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void createItemCategory_duplicateCode_throwsException() {
        when(businessGroupRepository.findById(1L)).thenReturn(Optional.of(businessGroup));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(businessUnit));
        when(itemCategoryRepository.existsByBuIdAndCategoryNameIgnoreCase(10L, "Hardware")).thenReturn(false);
        when(itemCategoryRepository.existsByBuIdAndCategoryCodeIgnoreCase(10L, "CAT-HW-01")).thenReturn(true);

        InvItemCategoryRequestDto request = InvItemCategoryRequestDto.builder()
                .bgId(1L)
                .buId(10L)
                .categoryName("Hardware")
                .categoryCode("CAT-HW-01")
                .build();

        assertThatThrownBy(() -> itemCategoryService.createItemCategory(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void updateItemCategory_success() {
        InvItemCategory existing = InvItemCategory.builder()
                .id(100L)
                .bgId(1L)
                .buId(10L)
                .categoryName("Old Category")
                .categoryCode("CAT-00001")
                .build();
        existing.setStatus(1);

        when(itemCategoryRepository.findById(100L)).thenReturn(Optional.of(existing));
        when(businessGroupRepository.findById(1L)).thenReturn(Optional.of(businessGroup));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(businessUnit));
        when(itemCategoryRepository.findByBuIdAndCategoryNameIgnoreCase(10L, "Updated Category"))
                .thenReturn(Optional.empty());
        when(itemCategoryRepository.findByBuIdAndCategoryCodeIgnoreCase(10L, "CAT-00001"))
                .thenReturn(Optional.of(existing));
        when(itemCategoryRepository.save(any(InvItemCategory.class))).thenReturn(existing);

        InvItemCategoryRequestDto request = InvItemCategoryRequestDto.builder()
                .bgId(1L)
                .buId(10L)
                .categoryName("Updated Category")
                .categoryCode("CAT-00001")
                .status(1)
                .build();

        InvItemCategoryResponseDto response = itemCategoryService.updateItemCategory(100L, request);

        assertThat(response.getCategoryName()).isEqualTo("Updated Category");
        verify(itemCategoryRepository).save(existing);
    }

    @Test
    void deleteItemCategory_success() {
        InvItemCategory existing = InvItemCategory.builder().id(100L).build();
        when(itemCategoryRepository.findById(100L)).thenReturn(Optional.of(existing));

        itemCategoryService.deleteItemCategory(100L);

        verify(itemCategoryRepository).delete(existing);
    }

    @Test
    void getItemCategoryById_notFound_throwsException() {
        when(itemCategoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemCategoryService.getItemCategoryById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

