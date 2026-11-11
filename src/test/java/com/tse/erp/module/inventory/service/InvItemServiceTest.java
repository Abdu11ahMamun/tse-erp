package com.tse.erp.module.inventory.service;

import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.BusinessGroup;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.admin.repository.UserRepository;
import com.tse.erp.module.asset.entity.AssetUom;
import com.tse.erp.module.asset.repository.AssetUomRepository;
import com.tse.erp.module.hr.repository.EmployeeRepository;
import com.tse.erp.module.inventory.dto.InvItemAttributeDtlsDto;
import com.tse.erp.module.inventory.dto.InvItemRequestDto;
import com.tse.erp.module.inventory.dto.InvItemResponseDto;
import com.tse.erp.module.inventory.entity.*;
import com.tse.erp.module.inventory.repository.*;
import com.tse.erp.module.inventory.service.impl.InvItemServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvItemServiceTest {

    @Mock
    private InvItemRepository itemRepository;
    @Mock
    private InvItemAttributeDtlsRepository itemAttributeDtlsRepository;
    @Mock
    private InvItemCategoryRepository itemCategoryRepository;
    @Mock
    private InvItemBrandRepository itemBrandRepository;
    @Mock
    private AssetUomRepository assetUomRepository;
    @Mock
    private InvItemStoreRepository itemStoreRepository;
    @Mock
    private InvItemAttributeRepository itemAttributeRepository;
    @Mock
    private BusinessGroupRepository businessGroupRepository;
    @Mock
    private BusinessUnitRepository businessUnitRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private InvItemServiceImpl itemService;

    private BusinessGroup businessGroup;
    private BusinessUnit businessUnit;
    private InvItemCategory category;
    private InvItemBrand brand;
    private AssetUom uom;
    private InvItemStore store;
    private InvItemAttribute attribute;

    @BeforeEach
    void setUp() {
        businessGroup = BusinessGroup.builder().id(1L).groupName("TSE Group").build();
        businessGroup.setStatus(1);

        businessUnit = BusinessUnit.builder().id(10L).bgId(1L).businessUnit("TSE Textiles").build();
        businessUnit.setStatus(1);

        category = InvItemCategory.builder().id(20L).buId(10L).categoryName("Fabrics").categoryCode("CAT-00001").build();
        brand = InvItemBrand.builder().id(30L).buId(10L).brandName("CottonCo").brandCode("BRD-00001").build();
        uom = AssetUom.builder().id(40L).buId(10L).unitName("Meter").build();
        store = InvItemStore.builder().id(50L).buId(10L).storeName("Central Store").storeCode("STR-00001").build();
        attribute = InvItemAttribute.builder().id(60L).buId(10L).attributeName("Color").attributeCode("ATTR-00001").build();
    }

    @Test
    void createItem_success_withGeneratedCode() {
        when(businessGroupRepository.findById(1L)).thenReturn(Optional.of(businessGroup));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(businessUnit));
        when(itemRepository.existsByBuIdAndItemNameIgnoreCase(10L, "Cotton Fabric")).thenReturn(false);
        when(itemRepository.countByBuId(10L)).thenReturn(0L);
        when(itemRepository.existsByBuIdAndItemCodeIgnoreCase(10L, "ITEM-00001")).thenReturn(false);

        when(itemCategoryRepository.findById(20L)).thenReturn(Optional.of(category));
        when(itemBrandRepository.findById(30L)).thenReturn(Optional.of(brand));
        when(assetUomRepository.findById(40L)).thenReturn(Optional.of(uom));
        when(itemStoreRepository.findById(50L)).thenReturn(Optional.of(store));

        InvItem saved = InvItem.builder()
                .id(100L)
                .bgId(1L)
                .buId(10L)
                .itemName("Cotton Fabric")
                .itemCode("ITEM-00001")
                .categoryId(20L)
                .brandId(30L)
                .uomId(40L)
                .storeId("50")
                .isWarrantyApplicable(0)
                .build();
        saved.setStatus(1);
        when(itemRepository.save(any(InvItem.class))).thenReturn(saved);
        when(itemAttributeDtlsRepository.findByItemIdOrderByIdAsc(100L)).thenReturn(Collections.emptyList());

        InvItemRequestDto request = InvItemRequestDto.builder()
                .bgId(1L)
                .buId(10L)
                .itemName("Cotton Fabric")
                .categoryId(20L)
                .brandId(30L)
                .uomId(40L)
                .storeId("50")
                .isWarrantyApplicable(0)
                .status(1)
                .build();

        InvItemResponseDto response = itemService.createItem(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getItemName()).isEqualTo("Cotton Fabric");
        assertThat(response.getItemCode()).isEqualTo("ITEM-00001");
        assertThat(response.getCategoryName()).isEqualTo("Fabrics");
        assertThat(response.getBrandName()).isEqualTo("CottonCo");
        assertThat(response.getUomName()).isEqualTo("Meter");
        assertThat(response.getStoreName()).isEqualTo("Central Store");
    }

    @Test
    void createItem_withDetails_success() {
        when(businessGroupRepository.findById(1L)).thenReturn(Optional.of(businessGroup));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(businessUnit));
        when(itemRepository.existsByBuIdAndItemNameIgnoreCase(10L, "Silk Fabric")).thenReturn(false);
        when(itemRepository.countByBuId(10L)).thenReturn(1L);
        when(itemRepository.existsByBuIdAndItemCodeIgnoreCase(10L, "ITEM-00002")).thenReturn(false);
        when(itemAttributeRepository.findById(60L)).thenReturn(Optional.of(attribute));

        InvItem saved = InvItem.builder()
                .id(101L)
                .bgId(1L)
                .buId(10L)
                .itemName("Silk Fabric")
                .itemCode("ITEM-00002")
                .build();
        saved.setStatus(1);
        when(itemRepository.save(any(InvItem.class))).thenReturn(saved);

        InvItemAttributeDtls savedDtl = InvItemAttributeDtls.builder()
                .id(201L)
                .bgId(1L)
                .buId(10L)
                .itemId(101L)
                .attributeId(60L)
                .price(new BigDecimal("150.00"))
                .openingStock(new BigDecimal("10.00"))
                .build();
        savedDtl.setStatus(1);
        when(itemAttributeDtlsRepository.findByItemIdOrderByIdAsc(101L)).thenReturn(List.of(savedDtl));

        InvItemAttributeDtlsDto dtlDto = InvItemAttributeDtlsDto.builder()
                .attributeId(60L)
                .price(new BigDecimal("150.00"))
                .openingStock(new BigDecimal("10.00"))
                .status(1)
                .build();

        InvItemRequestDto request = InvItemRequestDto.builder()
                .bgId(1L)
                .buId(10L)
                .itemName("Silk Fabric")
                .status(1)
                .attributeDetails(List.of(dtlDto))
                .build();

        InvItemResponseDto response = itemService.createItem(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(101L);
        assertThat(response.getAttributeDetails()).hasSize(1);
        assertThat(response.getAttributeDetails().get(0).getAttributeName()).isEqualTo("Color");
        verify(itemAttributeDtlsRepository).saveAll(any());
    }

    @Test
    void createItem_duplicateName_throwsException() {
        when(businessGroupRepository.findById(1L)).thenReturn(Optional.of(businessGroup));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(businessUnit));
        when(itemRepository.existsByBuIdAndItemNameIgnoreCase(10L, "Cotton Fabric")).thenReturn(true);

        InvItemRequestDto request = InvItemRequestDto.builder()
                .bgId(1L)
                .buId(10L)
                .itemName("Cotton Fabric")
                .build();

        assertThatThrownBy(() -> itemService.createItem(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void createItem_duplicateCode_throwsException() {
        when(businessGroupRepository.findById(1L)).thenReturn(Optional.of(businessGroup));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(businessUnit));
        when(itemRepository.existsByBuIdAndItemNameIgnoreCase(10L, "Linen Fabric")).thenReturn(false);
        when(itemRepository.existsByBuIdAndItemCodeIgnoreCase(10L, "CUSTOM-01")).thenReturn(true);

        InvItemRequestDto request = InvItemRequestDto.builder()
                .bgId(1L)
                .buId(10L)
                .itemName("Linen Fabric")
                .itemCode("CUSTOM-01")
                .build();

        assertThatThrownBy(() -> itemService.createItem(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void updateItem_success() {
        InvItem existing = InvItem.builder()
                .id(100L)
                .bgId(1L)
                .buId(10L)
                .itemName("Old Fabric")
                .itemCode("ITEM-00001")
                .build();
        existing.setStatus(1);

        when(itemRepository.findById(100L)).thenReturn(Optional.of(existing));
        when(businessGroupRepository.findById(1L)).thenReturn(Optional.of(businessGroup));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(businessUnit));
        when(itemRepository.findByBuIdAndItemNameIgnoreCase(10L, "Updated Fabric")).thenReturn(Optional.empty());
        when(itemRepository.findByBuIdAndItemCodeIgnoreCase(10L, "ITEM-00001")).thenReturn(Optional.of(existing));
        when(itemRepository.save(any(InvItem.class))).thenReturn(existing);
        when(itemAttributeDtlsRepository.findByItemIdOrderByIdAsc(100L)).thenReturn(Collections.emptyList());

        InvItemRequestDto request = InvItemRequestDto.builder()
                .bgId(1L)
                .buId(10L)
                .itemName("Updated Fabric")
                .itemCode("ITEM-00001")
                .status(1)
                .build();

        InvItemResponseDto response = itemService.updateItem(100L, request);

        assertThat(response.getItemName()).isEqualTo("Updated Fabric");
        verify(itemRepository).save(existing);
    }

    @Test
    void deleteItem_success_deletesDetailsFirst() {
        InvItem existing = InvItem.builder().id(100L).build();
        when(itemRepository.findById(100L)).thenReturn(Optional.of(existing));

        InvItemAttributeDtls dtl = InvItemAttributeDtls.builder().id(201L).itemId(100L).build();
        when(itemAttributeDtlsRepository.findByItemIdOrderByIdAsc(100L)).thenReturn(List.of(dtl));

        itemService.deleteItem(100L);

        verify(itemAttributeDtlsRepository).deleteAll(List.of(dtl));
        verify(itemRepository).delete(existing);
    }

    @Test
    void getItemById_notFound_throwsException() {
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.getItemById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

