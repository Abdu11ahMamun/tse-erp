package com.tse.erp.module.inventory.service;

import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.BusinessGroup;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.admin.repository.UserRepository;
import com.tse.erp.module.hr.repository.EmployeeRepository;
import com.tse.erp.module.inventory.dto.InvItemAttributeRequestDto;
import com.tse.erp.module.inventory.dto.InvItemAttributeResponseDto;
import com.tse.erp.module.inventory.entity.InvItemAttribute;
import com.tse.erp.module.inventory.repository.InvItemAttributeRepository;
import com.tse.erp.module.inventory.service.impl.InvItemAttributeServiceImpl;
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
class InvItemAttributeServiceTest {

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
    private InvItemAttributeServiceImpl itemAttributeService;

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
    void createItemAttribute_success_withGeneratedCode() {
        when(businessGroupRepository.findById(1L)).thenReturn(Optional.of(businessGroup));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(businessUnit));
        when(itemAttributeRepository.existsByBuIdAndAttributeNameIgnoreCase(10L, "Color")).thenReturn(false);
        when(itemAttributeRepository.countByBuId(10L)).thenReturn(0L);
        when(itemAttributeRepository.existsByBuIdAndAttributeCodeIgnoreCase(10L, "ATTR-00001")).thenReturn(false);

        InvItemAttribute saved = InvItemAttribute.builder()
                .id(100L)
                .bgId(1L)
                .buId(10L)
                .attributeName("Color")
                .attributeCode("ATTR-00001")
                .build();
        saved.setStatus(1);
        when(itemAttributeRepository.save(any(InvItemAttribute.class))).thenReturn(saved);

        InvItemAttributeRequestDto request = InvItemAttributeRequestDto.builder()
                .bgId(1L)
                .buId(10L)
                .attributeName("Color")
                .status(1)
                .build();

        InvItemAttributeResponseDto response = itemAttributeService.createItemAttribute(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getAttributeName()).isEqualTo("Color");
        assertThat(response.getAttributeCode()).isEqualTo("ATTR-00001");
        assertThat(response.getGroupName()).isEqualTo("TSE Group");
        assertThat(response.getBusinessUnitName()).isEqualTo("TSE Textiles");
    }

    @Test
    void createItemAttribute_duplicateName_throwsException() {
        when(businessGroupRepository.findById(1L)).thenReturn(Optional.of(businessGroup));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(businessUnit));
        when(itemAttributeRepository.existsByBuIdAndAttributeNameIgnoreCase(10L, "Size")).thenReturn(true);

        InvItemAttributeRequestDto request = InvItemAttributeRequestDto.builder()
                .bgId(1L)
                .buId(10L)
                .attributeName("Size")
                .build();

        assertThatThrownBy(() -> itemAttributeService.createItemAttribute(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void createItemAttribute_duplicateCode_throwsException() {
        when(businessGroupRepository.findById(1L)).thenReturn(Optional.of(businessGroup));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(businessUnit));
        when(itemAttributeRepository.existsByBuIdAndAttributeNameIgnoreCase(10L, "Weight")).thenReturn(false);
        when(itemAttributeRepository.existsByBuIdAndAttributeCodeIgnoreCase(10L, "ATTR-WT-01")).thenReturn(true);

        InvItemAttributeRequestDto request = InvItemAttributeRequestDto.builder()
                .bgId(1L)
                .buId(10L)
                .attributeName("Weight")
                .attributeCode("ATTR-WT-01")
                .build();

        assertThatThrownBy(() -> itemAttributeService.createItemAttribute(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void updateItemAttribute_success() {
        InvItemAttribute existing = InvItemAttribute.builder()
                .id(100L)
                .bgId(1L)
                .buId(10L)
                .attributeName("Old Attribute")
                .attributeCode("ATTR-00001")
                .build();
        existing.setStatus(1);

        when(itemAttributeRepository.findById(100L)).thenReturn(Optional.of(existing));
        when(businessGroupRepository.findById(1L)).thenReturn(Optional.of(businessGroup));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(businessUnit));
        when(itemAttributeRepository.findByBuIdAndAttributeNameIgnoreCase(10L, "Updated Attribute"))
                .thenReturn(Optional.empty());
        when(itemAttributeRepository.findByBuIdAndAttributeCodeIgnoreCase(10L, "ATTR-00001"))
                .thenReturn(Optional.of(existing));
        when(itemAttributeRepository.save(any(InvItemAttribute.class))).thenReturn(existing);

        InvItemAttributeRequestDto request = InvItemAttributeRequestDto.builder()
                .bgId(1L)
                .buId(10L)
                .attributeName("Updated Attribute")
                .attributeCode("ATTR-00001")
                .status(1)
                .build();

        InvItemAttributeResponseDto response = itemAttributeService.updateItemAttribute(100L, request);

        assertThat(response.getAttributeName()).isEqualTo("Updated Attribute");
        verify(itemAttributeRepository).save(existing);
    }

    @Test
    void deleteItemAttribute_success() {
        InvItemAttribute existing = InvItemAttribute.builder().id(100L).build();
        when(itemAttributeRepository.findById(100L)).thenReturn(Optional.of(existing));

        itemAttributeService.deleteItemAttribute(100L);

        verify(itemAttributeRepository).delete(existing);
    }

    @Test
    void getItemAttributeById_notFound_throwsException() {
        when(itemAttributeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemAttributeService.getItemAttributeById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

