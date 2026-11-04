package com.tse.erp.module.accounting.service;

import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.accounting.dto.AfmUserLocationRequestDto;
import com.tse.erp.module.accounting.dto.AfmUserLocationResponseDto;
import com.tse.erp.module.accounting.entity.AfmUserLocation;
import com.tse.erp.module.accounting.repository.AfmUserLocationRepository;
import com.tse.erp.module.accounting.service.impl.AfmUserLocationServiceImpl;
import com.tse.erp.module.admin.entity.BusinessGroup;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
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
class AfmUserLocationServiceTest {

    @Mock
    private AfmUserLocationRepository userLocationRepository;
    @Mock
    private BusinessGroupRepository businessGroupRepository;
    @Mock
    private BusinessUnitRepository businessUnitRepository;

    @InjectMocks
    private AfmUserLocationServiceImpl userLocationService;

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
    void createUserLocation_success() {
        when(businessGroupRepository.findById(1L)).thenReturn(Optional.of(businessGroup));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(businessUnit));
        when(userLocationRepository.existsByBuIdAndLocationIgnoreCase(10L, "Dhaka Head Office")).thenReturn(false);

        AfmUserLocation saved = AfmUserLocation.builder()
                .id(1L)
                .bgId(1L)
                .buId(10L)
                .location("Dhaka Head Office")
                .shortName("DHO")
                .build();
        saved.setStatus(1);

        when(userLocationRepository.save(any(AfmUserLocation.class))).thenReturn(saved);

        AfmUserLocationRequestDto request = AfmUserLocationRequestDto.builder()
                .bgId(1L)
                .buId(10L)
                .location("Dhaka Head Office")
                .shortName("DHO")
                .status(1)
                .build();

        AfmUserLocationResponseDto response = userLocationService.createUserLocation(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getLocation()).isEqualTo("Dhaka Head Office");
        assertThat(response.getShortName()).isEqualTo("DHO");
        assertThat(response.getGroupName()).isEqualTo("TSE Group");
        assertThat(response.getBusinessUnitName()).isEqualTo("TSE Textiles");
        verify(userLocationRepository, times(1)).save(any(AfmUserLocation.class));
    }

    @Test
    void createUserLocation_duplicateLocation_throwsException() {
        when(businessGroupRepository.findById(1L)).thenReturn(Optional.of(businessGroup));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(businessUnit));
        when(userLocationRepository.existsByBuIdAndLocationIgnoreCase(10L, "Dhaka Head Office")).thenReturn(true);

        AfmUserLocationRequestDto request = AfmUserLocationRequestDto.builder()
                .bgId(1L)
                .buId(10L)
                .location("Dhaka Head Office")
                .status(1)
                .build();

        assertThatThrownBy(() -> userLocationService.createUserLocation(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists for this Business Unit");
    }

    @Test
    void createUserLocation_emptyLocation_throwsException() {
        when(businessGroupRepository.findById(1L)).thenReturn(Optional.of(businessGroup));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(businessUnit));

        AfmUserLocationRequestDto request = AfmUserLocationRequestDto.builder()
                .bgId(1L)
                .buId(10L)
                .location("  ")
                .status(1)
                .build();

        assertThatThrownBy(() -> userLocationService.createUserLocation(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Location name cannot be empty");
    }

    @Test
    void getUserLocationById_success() {
        AfmUserLocation location = AfmUserLocation.builder()
                .id(1L)
                .bgId(1L)
                .buId(10L)
                .location("Factory 1")
                .shortName("F1")
                .build();
        location.setStatus(1);

        when(userLocationRepository.findById(1L)).thenReturn(Optional.of(location));
        when(businessGroupRepository.findById(1L)).thenReturn(Optional.of(businessGroup));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(businessUnit));

        AfmUserLocationResponseDto response = userLocationService.getUserLocationById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getLocation()).isEqualTo("Factory 1");
    }

    @Test
    void getUserLocationById_notFound_throwsException() {
        when(userLocationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userLocationService.getUserLocationById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void updateUserLocation_success() {
        AfmUserLocation existing = AfmUserLocation.builder()
                .id(1L)
                .bgId(1L)
                .buId(10L)
                .location("Old Location")
                .shortName("OLD")
                .build();
        existing.setStatus(1);

        when(userLocationRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(businessGroupRepository.findById(1L)).thenReturn(Optional.of(businessGroup));
        when(businessUnitRepository.findById(10L)).thenReturn(Optional.of(businessUnit));
        when(userLocationRepository.findByBuIdAndLocationIgnoreCase(10L, "New Location")).thenReturn(Optional.empty());
        when(userLocationRepository.save(any(AfmUserLocation.class))).thenReturn(existing);

        AfmUserLocationRequestDto request = AfmUserLocationRequestDto.builder()
                .bgId(1L)
                .buId(10L)
                .location("New Location")
                .shortName("NEW")
                .status(1)
                .build();

        AfmUserLocationResponseDto updated = userLocationService.updateUserLocation(1L, request);

        assertThat(updated).isNotNull();
        verify(userLocationRepository, times(1)).save(existing);
    }

    @Test
    void deleteUserLocation_success() {
        AfmUserLocation existing = AfmUserLocation.builder().id(1L).bgId(1L).buId(10L).location("Loc").build();
        when(userLocationRepository.findById(1L)).thenReturn(Optional.of(existing));

        userLocationService.deleteUserLocation(1L);

        verify(userLocationRepository, times(1)).delete(existing);
    }
}

