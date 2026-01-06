package com.logismart.logismartv2.service;

import com.logismart.logismartv2.dto.zone.ZoneCreateDTO;
import com.logismart.logismartv2.dto.zone.ZoneResponseDTO;
import com.logismart.logismartv2.dto.zone.ZoneUpdateDTO;
import com.logismart.logismartv2.entity.Zone;
import com.logismart.logismartv2.exception.DuplicateResourceException;
import com.logismart.logismartv2.exception.ResourceNotFoundException;
import com.logismart.logismartv2.mapper.ZoneMapper;
import com.logismart.logismartv2.repository.ZoneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Zone Service Tests")
class ZoneServiceTest {

    @Mock
    private ZoneRepository zoneRepository;

    @Mock
    private ZoneMapper zoneMapper;

    @InjectMocks
    private ZoneService zoneService;

    private Zone zone;
    private ZoneCreateDTO createDTO;
    private ZoneUpdateDTO updateDTO;
    private ZoneResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        zone = new Zone();
        zone.setId("zone-1");
        zone.setName("Casablanca Centre");
        zone.setPostalCode("20000");

        createDTO = new ZoneCreateDTO();
        createDTO.setName("Casablanca Centre");
        createDTO.setPostalCode("20000");

        updateDTO = new ZoneUpdateDTO();
        updateDTO.setId("zone-1");
        updateDTO.setName("Casablanca Centre Updated");
        updateDTO.setPostalCode("20000");

        responseDTO = new ZoneResponseDTO();
        responseDTO.setId("zone-1");
        responseDTO.setName("Casablanca Centre");
        responseDTO.setPostalCode("20000");
    }

    @Test
    @DisplayName("Should create zone successfully")
    void testCreateZone_Success() {
        // Given
        when(zoneRepository.existsByName(createDTO.getName())).thenReturn(false);
        when(zoneRepository.existsByPostalCode(createDTO.getPostalCode())).thenReturn(false);
        when(zoneMapper.toEntity(createDTO)).thenReturn(zone);
        when(zoneRepository.save(zone)).thenReturn(zone);
        when(zoneMapper.toResponseDTO(zone)).thenReturn(responseDTO);

        // When
        ZoneResponseDTO result = zoneService.create(createDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Casablanca Centre");
        verify(zoneRepository).save(zone);
    }

    @Test
    @DisplayName("Should throw exception when creating zone with duplicate name")
    void testCreateZone_DuplicateName() {
        // Given
        when(zoneRepository.existsByName("Casablanca Centre")).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> zoneService.create(createDTO))
                .isInstanceOf(DuplicateResourceException.class);

        verify(zoneRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should find zone by ID successfully")
    void testFindById_Success() {
        // Given
        when(zoneRepository.findById("zone-1")).thenReturn(Optional.of(zone));
        when(zoneMapper.toResponseDTO(zone)).thenReturn(responseDTO);

        // When
        ZoneResponseDTO result = zoneService.findById("zone-1");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("zone-1");
        verify(zoneRepository).findById("zone-1");
    }

    @Test
    @DisplayName("Should throw exception when zone not found")
    void testFindById_NotFound() {
        // Given
        when(zoneRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> zoneService.findById("invalid-id"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should find all zones successfully")
    void testFindAll_Success() {
        // Given
        Zone zone2 = new Zone();
        zone2.setId("zone-2");
        zone2.setName("Rabat Centre");
        zone2.setPostalCode("10000");

        List<Zone> zones = Arrays.asList(zone, zone2);
        when(zoneRepository.findAll()).thenReturn(zones);

        ZoneResponseDTO responseDTO2 = new ZoneResponseDTO();
        responseDTO2.setId("zone-2");
        responseDTO2.setName("Rabat Centre");

        when(zoneMapper.toResponseDTOList(zones)).thenReturn(Arrays.asList(responseDTO, responseDTO2));

        // When
        List<ZoneResponseDTO> result = zoneService.findAll();

        // Then
        assertThat(result).hasSize(2);
        verify(zoneRepository).findAll();
    }

    @Test
    @DisplayName("Should update zone successfully")
    void testUpdateZone_Success() {
        // Given
        when(zoneRepository.findById("zone-1")).thenReturn(Optional.of(zone));
        when(zoneRepository.save(zone)).thenReturn(zone);
        when(zoneMapper.toResponseDTO(zone)).thenReturn(responseDTO);

        // When
        ZoneResponseDTO result = zoneService.update(updateDTO);

        // Then
        assertThat(result).isNotNull();
        verify(zoneRepository).save(zone);
    }

    @Test
    @DisplayName("Should delete zone successfully")
    void testDeleteZone_Success() {
        // Given
        when(zoneRepository.existsById("zone-1")).thenReturn(true);

        // When
        zoneService.delete("zone-1");

        // Then
        verify(zoneRepository).deleteById("zone-1");
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent zone")
    void testDeleteZone_NotFound() {
        // Given
        when(zoneRepository.existsById("invalid-id")).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> zoneService.delete("invalid-id"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(zoneRepository, never()).deleteById(anyString());
    }
}
