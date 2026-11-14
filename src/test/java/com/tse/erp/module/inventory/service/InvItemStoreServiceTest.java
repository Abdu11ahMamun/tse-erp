package com.tse.erp.module.inventory.service;

import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.BusinessGroup;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.admin.repository.UserRepository;
import com.tse.erp.module.hr.repository.EmployeeRepository;
import com.tse.erp.module.inventory.dto.InvItemStoreRequestDto;
import com.tse.erp.module.inventory.dto.InvItemStoreResponseDto;
import com.tse.erp.module.inventory.entity.InvItemStore;
import com.tse.erp.module.inventory.repository.InvItemStoreRepository;
import com.tse.erp.module.inventory.service.impl.InvItemStoreServiceImpl;
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
class InvItemStoreServiceTest {

    @Mock
    private InvItemStoreRepository itemStoreRepository;
    @Mock
    private BusinessGroupRepository businessGroupRepository;
    @Mock
    private BusinessUnitRepository businessUnitRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private InvItemStoreEmployeeMapService storeEmployeeMapService;

    @InjectMocks
    private InvItemStoreServiceImpl itemStoreService;

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
    void createItemStore_success_withGeneratedCode() {
        when(businessGroupRepository.findById(1L)).thenReturn(Optional.of(businessGroup));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(businessUnit));
        when(itemStoreRepository.existsByBuIdAndStoreNameIgnoreCase(10L, "Main Central Store")).thenReturn(false);
        when(itemStoreRepository.countByBuId(10L)).thenReturn(0L);
        when(itemStoreRepository.existsByBuIdAndStoreCodeIgnoreCase(10L, "STR-00001")).thenReturn(false);

        InvItemStore saved = InvItemStore.builder()
                .id(100L)
                .bgId(1L)
                .buId(10L)
                .storeName("Main Central Store")
                .storeCode("STR-00001")
                .isDefault(1)
                .build();
        saved.setStatus(1);
        when(itemStoreRepository.save(any(InvItemStore.class))).thenReturn(saved);

        InvItemStoreRequestDto request = InvItemStoreRequestDto.builder()
                .bgId(1L)
                .buId(10L)
                .storeName("Main Central Store")
                .isDefault(1)
                .status(1)
                .build();

        InvItemStoreResponseDto response = itemStoreService.createItemStore(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getStoreName()).isEqualTo("Main Central Store");
        assertThat(response.getStoreCode()).isEqualTo("STR-00001");
        assertThat(response.getIsDefault()).isEqualTo(1);
        assertThat(response.getGroupName()).isEqualTo("TSE Group");
        assertThat(response.getBusinessUnitName()).isEqualTo("TSE Textiles");
        verify(itemStoreRepository).resetOtherDefaults(10L, null);
    }

    @Test
    void createItemStore_duplicateName_throwsException() {
        when(businessGroupRepository.findById(1L)).thenReturn(Optional.of(businessGroup));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(businessUnit));
        when(itemStoreRepository.existsByBuIdAndStoreNameIgnoreCase(10L, "Main Central Store")).thenReturn(true);

        InvItemStoreRequestDto request = InvItemStoreRequestDto.builder()
                .bgId(1L)
                .buId(10L)
                .storeName("Main Central Store")
                .build();

        assertThatThrownBy(() -> itemStoreService.createItemStore(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void createItemStore_duplicateCode_throwsException() {
        when(businessGroupRepository.findById(1L)).thenReturn(Optional.of(businessGroup));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(businessUnit));
        when(itemStoreRepository.existsByBuIdAndStoreNameIgnoreCase(10L, "Warehouse B")).thenReturn(false);
        when(itemStoreRepository.existsByBuIdAndStoreCodeIgnoreCase(10L, "STR-WH-02")).thenReturn(true);

        InvItemStoreRequestDto request = InvItemStoreRequestDto.builder()
                .bgId(1L)
                .buId(10L)
                .storeName("Warehouse B")
                .storeCode("STR-WH-02")
                .build();

        assertThatThrownBy(() -> itemStoreService.createItemStore(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void updateItemStore_success() {
        InvItemStore existing = InvItemStore.builder()
                .id(100L)
                .bgId(1L)
                .buId(10L)
                .storeName("Old Store")
                .storeCode("STR-00001")
                .isDefault(0)
                .build();
        existing.setStatus(1);

        when(itemStoreRepository.findById(100L)).thenReturn(Optional.of(existing));
        when(businessGroupRepository.findById(1L)).thenReturn(Optional.of(businessGroup));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(businessUnit));
        when(itemStoreRepository.findByBuIdAndStoreNameIgnoreCase(10L, "Updated Store"))
                .thenReturn(Optional.empty());
        when(itemStoreRepository.findByBuIdAndStoreCodeIgnoreCase(10L, "STR-00001"))
                .thenReturn(Optional.of(existing));
        when(itemStoreRepository.save(any(InvItemStore.class))).thenReturn(existing);

        InvItemStoreRequestDto request = InvItemStoreRequestDto.builder()
                .bgId(1L)
                .buId(10L)
                .storeName("Updated Store")
                .storeCode("STR-00001")
                .isDefault(1)
                .status(1)
                .build();

        InvItemStoreResponseDto response = itemStoreService.updateItemStore(100L, request);

        assertThat(response.getStoreName()).isEqualTo("Updated Store");
        verify(itemStoreRepository).resetOtherDefaults(10L, 100L);
        verify(itemStoreRepository).save(existing);
    }

    @Test
    void deleteItemStore_success() {
        InvItemStore existing = InvItemStore.builder().id(100L).build();
        when(itemStoreRepository.findById(100L)).thenReturn(Optional.of(existing));

        itemStoreService.deleteItemStore(100L);

        verify(itemStoreRepository).delete(existing);
    }

    @Test
    void getItemStoreById_notFound_throwsException() {
        when(itemStoreRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemStoreService.getItemStoreById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

