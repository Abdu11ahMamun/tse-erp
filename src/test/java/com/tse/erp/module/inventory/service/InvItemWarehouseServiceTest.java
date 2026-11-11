package com.tse.erp.module.inventory.service;

import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.BusinessGroup;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.admin.repository.UserRepository;
import com.tse.erp.module.hr.repository.EmployeeRepository;
import com.tse.erp.module.inventory.dto.InvItemWarehouseRequestDto;
import com.tse.erp.module.inventory.dto.InvItemWarehouseResponseDto;
import com.tse.erp.module.inventory.entity.InvItemStore;
import com.tse.erp.module.inventory.entity.InvItemWarehouse;
import com.tse.erp.module.inventory.repository.InvItemStoreRepository;
import com.tse.erp.module.inventory.repository.InvItemWarehouseRepository;
import com.tse.erp.module.inventory.service.impl.InvItemWarehouseServiceImpl;
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
class InvItemWarehouseServiceTest {

    @Mock
    private InvItemWarehouseRepository itemWarehouseRepository;
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
    private InvItemWarehouseEmployeeMapService warehouseEmployeeMapService;

    @InjectMocks
    private InvItemWarehouseServiceImpl itemWarehouseService;

    private BusinessGroup businessGroup;
    private BusinessUnit businessUnit;
    private InvItemStore itemStore;

    @BeforeEach
    void setUp() {
        businessGroup = BusinessGroup.builder().id(1L).groupName("TSE Group").build();
        businessGroup.setStatus(1);

        businessUnit = BusinessUnit.builder().id(10L).bgId(1L).businessUnit("TSE Textiles").build();
        businessUnit.setStatus(1);

        itemStore = InvItemStore.builder().id(50L).buId(10L).storeName("Central Store").storeCode("STR-00001").build();
    }

    @Test
    void createItemWarehouse_success_withGeneratedCode() {
        when(businessGroupRepository.findById(1L)).thenReturn(Optional.of(businessGroup));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(businessUnit));
        when(itemWarehouseRepository.existsByBuIdAndWarehouseNameIgnoreCase(10L, "Main Warehouse")).thenReturn(false);
        when(itemWarehouseRepository.countByBuId(10L)).thenReturn(0L);
        when(itemWarehouseRepository.existsByBuIdAndWarehouseCodeIgnoreCase(10L, "WH-00001")).thenReturn(false);
        when(itemStoreRepository.findById(50L)).thenReturn(Optional.of(itemStore));

        InvItemWarehouse saved = InvItemWarehouse.builder()
                .id(100L)
                .bgId(1L)
                .buId(10L)
                .warehouseName("Main Warehouse")
                .warehouseCode("WH-00001")
                .storeId("50")
                .isDefault(1)
                .build();
        saved.setStatus(1);
        when(itemWarehouseRepository.save(any(InvItemWarehouse.class))).thenReturn(saved);

        InvItemWarehouseRequestDto request = InvItemWarehouseRequestDto.builder()
                .bgId(1L)
                .buId(10L)
                .warehouseName("Main Warehouse")
                .storeId("50")
                .isDefault(1)
                .status(1)
                .build();

        InvItemWarehouseResponseDto response = itemWarehouseService.createItemWarehouse(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getWarehouseName()).isEqualTo("Main Warehouse");
        assertThat(response.getWarehouseCode()).isEqualTo("WH-00001");
        assertThat(response.getStoreId()).isEqualTo("50");
        assertThat(response.getStoreName()).isEqualTo("Central Store");
        assertThat(response.getIsDefault()).isEqualTo(1);
        assertThat(response.getGroupName()).isEqualTo("TSE Group");
        assertThat(response.getBusinessUnitName()).isEqualTo("TSE Textiles");
        verify(itemWarehouseRepository).resetOtherDefaults(10L, null);
    }

    @Test
    void createItemWarehouse_multipleStores_success() {
        InvItemStore itemStore2 = InvItemStore.builder().id(51L).buId(10L).storeName("Secondary Store").storeCode("STR-00002").build();
        when(businessGroupRepository.findById(1L)).thenReturn(Optional.of(businessGroup));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(businessUnit));
        when(itemWarehouseRepository.existsByBuIdAndWarehouseNameIgnoreCase(10L, "Multi Store WH")).thenReturn(false);
        when(itemWarehouseRepository.countByBuId(10L)).thenReturn(1L);
        when(itemWarehouseRepository.existsByBuIdAndWarehouseCodeIgnoreCase(10L, "WH-00002")).thenReturn(false);
        when(itemStoreRepository.findById(50L)).thenReturn(Optional.of(itemStore));
        when(itemStoreRepository.findById(51L)).thenReturn(Optional.of(itemStore2));

        InvItemWarehouse saved = InvItemWarehouse.builder()
                .id(101L)
                .bgId(1L)
                .buId(10L)
                .warehouseName("Multi Store WH")
                .warehouseCode("WH-00002")
                .storeId("50,51")
                .isDefault(0)
                .build();
        saved.setStatus(1);
        when(itemWarehouseRepository.save(any(InvItemWarehouse.class))).thenReturn(saved);

        InvItemWarehouseRequestDto request = InvItemWarehouseRequestDto.builder()
                .bgId(1L)
                .buId(10L)
                .warehouseName("Multi Store WH")
                .storeId("50, 51")
                .isDefault(0)
                .status(1)
                .build();

        InvItemWarehouseResponseDto response = itemWarehouseService.createItemWarehouse(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(101L);
        assertThat(response.getStoreId()).isEqualTo("50,51");
        assertThat(response.getStoreName()).isEqualTo("Central Store, Secondary Store");
        assertThat(response.getStoreIds()).containsExactly(50L, 51L);
        assertThat(response.getStoreNames()).containsExactly("Central Store", "Secondary Store");
    }

    @Test
    void createItemWarehouse_duplicateName_throwsException() {
        when(businessGroupRepository.findById(1L)).thenReturn(Optional.of(businessGroup));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(businessUnit));
        when(itemWarehouseRepository.existsByBuIdAndWarehouseNameIgnoreCase(10L, "Main Warehouse")).thenReturn(true);

        InvItemWarehouseRequestDto request = InvItemWarehouseRequestDto.builder()
                .bgId(1L)
                .buId(10L)
                .warehouseName("Main Warehouse")
                .build();

        assertThatThrownBy(() -> itemWarehouseService.createItemWarehouse(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void createItemWarehouse_duplicateCode_throwsException() {
        when(businessGroupRepository.findById(1L)).thenReturn(Optional.of(businessGroup));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(businessUnit));
        when(itemWarehouseRepository.existsByBuIdAndWarehouseNameIgnoreCase(10L, "North Warehouse")).thenReturn(false);
        when(itemWarehouseRepository.existsByBuIdAndWarehouseCodeIgnoreCase(10L, "WH-00002")).thenReturn(true);

        InvItemWarehouseRequestDto request = InvItemWarehouseRequestDto.builder()
                .bgId(1L)
                .buId(10L)
                .warehouseName("North Warehouse")
                .warehouseCode("WH-00002")
                .build();

        assertThatThrownBy(() -> itemWarehouseService.createItemWarehouse(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void updateItemWarehouse_success() {
        InvItemWarehouse existing = InvItemWarehouse.builder()
                .id(100L)
                .bgId(1L)
                .buId(10L)
                .warehouseName("Old Warehouse")
                .warehouseCode("WH-00001")
                .storeId("50")
                .isDefault(0)
                .build();
        existing.setStatus(1);

        when(itemWarehouseRepository.findById(100L)).thenReturn(Optional.of(existing));
        when(businessGroupRepository.findById(1L)).thenReturn(Optional.of(businessGroup));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(businessUnit));
        when(itemWarehouseRepository.findByBuIdAndWarehouseNameIgnoreCase(10L, "Updated Warehouse"))
                .thenReturn(Optional.empty());
        when(itemWarehouseRepository.findByBuIdAndWarehouseCodeIgnoreCase(10L, "WH-00001"))
                .thenReturn(Optional.of(existing));
        when(itemStoreRepository.findById(50L)).thenReturn(Optional.of(itemStore));
        when(itemWarehouseRepository.save(any(InvItemWarehouse.class))).thenReturn(existing);

        InvItemWarehouseRequestDto request = InvItemWarehouseRequestDto.builder()
                .bgId(1L)
                .buId(10L)
                .warehouseName("Updated Warehouse")
                .warehouseCode("WH-00001")
                .storeId("50")
                .isDefault(1)
                .status(1)
                .build();

        InvItemWarehouseResponseDto response = itemWarehouseService.updateItemWarehouse(100L, request);

        assertThat(response.getWarehouseName()).isEqualTo("Updated Warehouse");
        verify(itemWarehouseRepository).resetOtherDefaults(10L, 100L);
        verify(itemWarehouseRepository).save(existing);
    }

    @Test
    void deleteItemWarehouse_success() {
        InvItemWarehouse existing = InvItemWarehouse.builder().id(100L).build();
        when(itemWarehouseRepository.findById(100L)).thenReturn(Optional.of(existing));

        itemWarehouseService.deleteItemWarehouse(100L);

        verify(itemWarehouseRepository).delete(existing);
    }

    @Test
    void getItemWarehouseById_notFound_throwsException() {
        when(itemWarehouseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemWarehouseService.getItemWarehouseById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

