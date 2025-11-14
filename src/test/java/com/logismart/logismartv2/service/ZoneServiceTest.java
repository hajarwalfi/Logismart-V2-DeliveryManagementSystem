package com.logismart.logismartv2.service;

import com.logismart.logismartv2.dto.zone.ZoneCreateDTO;
import com.logismart.logismartv2.dto.zone.ZoneResponseDTO;
import com.logismart.logismartv2.dto.zone.ZoneStatsDTO;
import com.logismart.logismartv2.dto.zone.ZoneUpdateDTO;
import com.logismart.logismartv2.entity.Parcel;
import com.logismart.logismartv2.entity.ParcelStatus;
import com.logismart.logismartv2.entity.Zone;
import com.logismart.logismartv2.exception.DuplicateResourceException;
import com.logismart.logismartv2.exception.ResourceNotFoundException;
import com.logismart.logismartv2.mapper.ZoneMapper;
import com.logismart.logismartv2.repository.ParcelRepository;
import com.logismart.logismartv2.repository.ZoneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour ZoneService
 * Service: 5/9 - Moyen (Gestion des zones géographiques)
 * Méthodes testées: 14 méthodes publiques (CRUD + recherches + statistiques)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ZoneService Unit Tests")
class ZoneServiceTest {

    @Mock
    private ZoneRepository zoneRepository;

    @Mock
    private ZoneMapper zoneMapper;

    @Mock
    private ParcelRepository parcelRepository;

    @InjectMocks
    private ZoneService zoneService;

    private Zone zone;
    private ZoneCreateDTO createDTO;
    private ZoneUpdateDTO updateDTO;
    private ZoneResponseDTO responseDTO;
    private Parcel parcel1;
    private Parcel parcel2;

    @BeforeEach
    void setUp() {
        zone = new Zone();
        zone.setId("zone-1");
        zone.setName("Zone Centre");
        zone.setPostalCode("20000");

        createDTO = new ZoneCreateDTO();
        createDTO.setName("Zone Centre");
        createDTO.setPostalCode("20000");

        updateDTO = new ZoneUpdateDTO();
        updateDTO.setId("zone-1");
        updateDTO.setName("Zone Centre Updated");
        updateDTO.setPostalCode("20001");

        responseDTO = new ZoneResponseDTO();
        responseDTO.setId("zone-1");
        responseDTO.setName("Zone Centre");
        responseDTO.setPostalCode("20000");

        parcel1 = new Parcel();
        parcel1.setId("parcel-1");
        parcel1.setWeight(new BigDecimal("2.5"));
        parcel1.setStatus(ParcelStatus.IN_TRANSIT);

        parcel2 = new Parcel();
        parcel2.setId("parcel-2");
        parcel2.setWeight(new BigDecimal("3.0"));
        parcel2.setStatus(ParcelStatus.DELIVERED);
    }

    // ==================== Tests pour create() ====================

    @Test
    @DisplayName("Should create zone successfully when name and postal code are unique")
    void testCreate_Success() {
        // GIVEN
        when(zoneRepository.existsByName(createDTO.getName())).thenReturn(false);
        when(zoneRepository.existsByPostalCode(createDTO.getPostalCode())).thenReturn(false);
        when(zoneMapper.toEntity(createDTO)).thenReturn(zone);
        when(zoneRepository.save(zone)).thenReturn(zone);
        when(zoneMapper.toResponseDTO(zone)).thenReturn(responseDTO);

        // WHEN
        ZoneResponseDTO result = zoneService.create(createDTO);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Zone Centre");
        assertThat(result.getPostalCode()).isEqualTo("20000");

        verify(zoneRepository).existsByName("Zone Centre");
        verify(zoneRepository).existsByPostalCode("20000");
        verify(zoneRepository).save(zone);
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when creating zone with existing name")
    void testCreate_DuplicateName() {
        // GIVEN
        when(zoneRepository.existsByName(createDTO.getName())).thenReturn(true);

        // WHEN & THEN
        assertThatThrownBy(() -> zoneService.create(createDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Zone")
                .hasMessageContaining("name");

        verify(zoneRepository).existsByName("Zone Centre");
        verify(zoneRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when creating zone with existing postal code")
    void testCreate_DuplicatePostalCode() {
        // GIVEN
        when(zoneRepository.existsByName(createDTO.getName())).thenReturn(false);
        when(zoneRepository.existsByPostalCode(createDTO.getPostalCode())).thenReturn(true);

        // WHEN & THEN
        assertThatThrownBy(() -> zoneService.create(createDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Zone")
                .hasMessageContaining("postalCode");

        verify(zoneRepository).existsByName("Zone Centre");
        verify(zoneRepository).existsByPostalCode("20000");
        verify(zoneRepository, never()).save(any());
    }

    // ==================== Tests pour findById() ====================

    @Test
    @DisplayName("Should find zone by id successfully")
    void testFindById_Success() {
        // GIVEN
        when(zoneRepository.findById("zone-1")).thenReturn(Optional.of(zone));
        when(zoneMapper.toResponseDTO(zone)).thenReturn(responseDTO);

        // WHEN
        ZoneResponseDTO result = zoneService.findById("zone-1");

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("zone-1");

        verify(zoneRepository).findById("zone-1");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when zone not found by id")
    void testFindById_NotFound() {
        // GIVEN
        when(zoneRepository.findById("zone-999")).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> zoneService.findById("zone-999"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Zone");

        verify(zoneRepository).findById("zone-999");
    }

    // ==================== Tests pour findAll() ====================

    @Test
    @DisplayName("Should return all zones successfully")
    void testFindAll_Success() {
        // GIVEN
        List<Zone> zones = Arrays.asList(zone);
        List<ZoneResponseDTO> responseDTOs = Arrays.asList(responseDTO);

        when(zoneRepository.findAll()).thenReturn(zones);
        when(zoneMapper.toResponseDTOList(zones)).thenReturn(responseDTOs);

        // WHEN
        List<ZoneResponseDTO> result = zoneService.findAll();

        // THEN
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        verify(zoneRepository).findAll();
    }

    // ==================== Tests pour update() ====================

    @Test
    @DisplayName("Should update zone successfully when new values are unique")
    void testUpdate_Success() {
        // GIVEN
        when(zoneRepository.findById(updateDTO.getId())).thenReturn(Optional.of(zone));
        when(zoneRepository.existsByName(updateDTO.getName())).thenReturn(false);
        when(zoneRepository.existsByPostalCode(updateDTO.getPostalCode())).thenReturn(false);
        when(zoneRepository.save(zone)).thenReturn(zone);
        when(zoneMapper.toResponseDTO(zone)).thenReturn(responseDTO);

        // WHEN
        ZoneResponseDTO result = zoneService.update(updateDTO);

        // THEN
        assertThat(result).isNotNull();

        verify(zoneRepository).findById("zone-1");
        verify(zoneMapper).updateEntityFromDTO(updateDTO, zone);
        verify(zoneRepository).save(zone);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent zone")
    void testUpdate_NotFound() {
        // GIVEN
        when(zoneRepository.findById(updateDTO.getId())).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> zoneService.update(updateDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Zone");

        verify(zoneRepository).findById("zone-1");
        verify(zoneRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when updating zone with existing name")
    void testUpdate_DuplicateName() {
        // GIVEN
        when(zoneRepository.findById(updateDTO.getId())).thenReturn(Optional.of(zone));
        when(zoneRepository.existsByName(updateDTO.getName())).thenReturn(true);

        // WHEN & THEN
        assertThatThrownBy(() -> zoneService.update(updateDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("name");

        verify(zoneRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when updating zone with existing postal code")
    void testUpdate_DuplicatePostalCode() {
        // GIVEN
        when(zoneRepository.findById(updateDTO.getId())).thenReturn(Optional.of(zone));
        when(zoneRepository.existsByName(updateDTO.getName())).thenReturn(false);
        when(zoneRepository.existsByPostalCode(updateDTO.getPostalCode())).thenReturn(true);

        // WHEN & THEN
        assertThatThrownBy(() -> zoneService.update(updateDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("postalCode");

        verify(zoneRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update zone successfully when name unchanged")
    void testUpdate_SameName() {
        // GIVEN
        updateDTO.setName("Zone Centre"); // Same as existing
        when(zoneRepository.findById(updateDTO.getId())).thenReturn(Optional.of(zone));
        when(zoneRepository.existsByPostalCode(updateDTO.getPostalCode())).thenReturn(false);
        when(zoneRepository.save(zone)).thenReturn(zone);
        when(zoneMapper.toResponseDTO(zone)).thenReturn(responseDTO);

        // WHEN
        ZoneResponseDTO result = zoneService.update(updateDTO);

        // THEN
        assertThat(result).isNotNull();
        verify(zoneRepository, never()).existsByName(any()); // Should not check if same name
        verify(zoneRepository).save(zone);
    }

    // ==================== Tests pour delete() ====================

    @Test
    @DisplayName("Should delete zone successfully")
    void testDelete_Success() {
        // GIVEN
        when(zoneRepository.existsById("zone-1")).thenReturn(true);
        doNothing().when(zoneRepository).deleteById("zone-1");

        // WHEN
        zoneService.delete("zone-1");

        // THEN
        verify(zoneRepository).existsById("zone-1");
        verify(zoneRepository).deleteById("zone-1");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent zone")
    void testDelete_NotFound() {
        // GIVEN
        when(zoneRepository.existsById("zone-999")).thenReturn(false);

        // WHEN & THEN
        assertThatThrownBy(() -> zoneService.delete("zone-999"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(zoneRepository, never()).deleteById(any());
    }

    // ==================== Tests pour findByName() ====================

    @Test
    @DisplayName("Should find zone by name successfully")
    void testFindByName_Success() {
        // GIVEN
        when(zoneRepository.findByName("Zone Centre")).thenReturn(Optional.of(zone));
        when(zoneMapper.toResponseDTO(zone)).thenReturn(responseDTO);

        // WHEN
        ZoneResponseDTO result = zoneService.findByName("Zone Centre");

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Zone Centre");

        verify(zoneRepository).findByName("Zone Centre");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when zone not found by name")
    void testFindByName_NotFound() {
        // GIVEN
        when(zoneRepository.findByName("Unknown Zone")).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> zoneService.findByName("Unknown Zone"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Zone")
                .hasMessageContaining("name");

        verify(zoneRepository).findByName("Unknown Zone");
    }

    // ==================== Tests pour findByPostalCode() ====================

    @Test
    @DisplayName("Should find zone by postal code successfully")
    void testFindByPostalCode_Success() {
        // GIVEN
        when(zoneRepository.findByPostalCode("20000")).thenReturn(Optional.of(zone));
        when(zoneMapper.toResponseDTO(zone)).thenReturn(responseDTO);

        // WHEN
        ZoneResponseDTO result = zoneService.findByPostalCode("20000");

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getPostalCode()).isEqualTo("20000");

        verify(zoneRepository).findByPostalCode("20000");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when zone not found by postal code")
    void testFindByPostalCode_NotFound() {
        // GIVEN
        when(zoneRepository.findByPostalCode("99999")).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> zoneService.findByPostalCode("99999"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Zone")
                .hasMessageContaining("postalCode");

        verify(zoneRepository).findByPostalCode("99999");
    }

    // ==================== Tests pour searchByName() ====================

    @Test
    @DisplayName("Should search zones by name keyword successfully")
    void testSearchByName_Success() {
        // GIVEN
        List<Zone> zones = Arrays.asList(zone);
        List<ZoneResponseDTO> responseDTOs = Arrays.asList(responseDTO);

        when(zoneRepository.findByNameContainingIgnoreCase("centre")).thenReturn(zones);
        when(zoneMapper.toResponseDTOList(zones)).thenReturn(responseDTOs);

        // WHEN
        List<ZoneResponseDTO> result = zoneService.searchByName("centre");

        // THEN
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        verify(zoneRepository).findByNameContainingIgnoreCase("centre");
    }

    @Test
    @DisplayName("Should return empty list when no zones match search keyword")
    void testSearchByName_NoResults() {
        // GIVEN
        when(zoneRepository.findByNameContainingIgnoreCase("unknown")).thenReturn(Arrays.asList());
        when(zoneMapper.toResponseDTOList(Arrays.asList())).thenReturn(Arrays.asList());

        // WHEN
        List<ZoneResponseDTO> result = zoneService.searchByName("unknown");

        // THEN
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(zoneRepository).findByNameContainingIgnoreCase("unknown");
    }

    // ==================== Tests pour findZonesWithDeliveryPersons() ====================

    @Test
    @DisplayName("Should find zones with delivery persons successfully")
    void testFindZonesWithDeliveryPersons_Success() {
        // GIVEN
        List<Zone> zones = Arrays.asList(zone);
        List<ZoneResponseDTO> responseDTOs = Arrays.asList(responseDTO);

        when(zoneRepository.findZonesWithDeliveryPersons()).thenReturn(zones);
        when(zoneMapper.toResponseDTOList(zones)).thenReturn(responseDTOs);

        // WHEN
        List<ZoneResponseDTO> result = zoneService.findZonesWithDeliveryPersons();

        // THEN
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        verify(zoneRepository).findZonesWithDeliveryPersons();
    }

    // ==================== Tests pour findZonesWithoutDeliveryPersons() ====================

    @Test
    @DisplayName("Should find zones without delivery persons successfully")
    void testFindZonesWithoutDeliveryPersons_Success() {
        // GIVEN
        List<Zone> zones = Arrays.asList(zone);
        List<ZoneResponseDTO> responseDTOs = Arrays.asList(responseDTO);

        when(zoneRepository.findZonesWithoutDeliveryPersons()).thenReturn(zones);
        when(zoneMapper.toResponseDTOList(zones)).thenReturn(responseDTOs);

        // WHEN
        List<ZoneResponseDTO> result = zoneService.findZonesWithoutDeliveryPersons();

        // THEN
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        verify(zoneRepository).findZonesWithoutDeliveryPersons();
    }

    // ==================== Tests pour countDeliveryPersons() ====================

    @Test
    @DisplayName("Should count delivery persons in zone successfully")
    void testCountDeliveryPersons_Success() {
        // GIVEN
        when(zoneRepository.existsById("zone-1")).thenReturn(true);
        when(zoneRepository.countDeliveryPersonsByZoneId("zone-1")).thenReturn(5L);

        // WHEN
        Long result = zoneService.countDeliveryPersons("zone-1");

        // THEN
        assertThat(result).isEqualTo(5L);

        verify(zoneRepository).existsById("zone-1");
        verify(zoneRepository).countDeliveryPersonsByZoneId("zone-1");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when counting delivery persons for non-existent zone")
    void testCountDeliveryPersons_ZoneNotFound() {
        // GIVEN
        when(zoneRepository.existsById("zone-999")).thenReturn(false);

        // WHEN & THEN
        assertThatThrownBy(() -> zoneService.countDeliveryPersons("zone-999"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Zone");

        verify(zoneRepository).existsById("zone-999");
        verify(zoneRepository, never()).countDeliveryPersonsByZoneId(any());
    }

    // ==================== Tests pour countParcels() ====================

    @Test
    @DisplayName("Should count parcels in zone successfully")
    void testCountParcels_Success() {
        // GIVEN
        when(zoneRepository.existsById("zone-1")).thenReturn(true);
        when(parcelRepository.countByZoneId("zone-1")).thenReturn(10L);

        // WHEN
        Long result = zoneService.countParcels("zone-1");

        // THEN
        assertThat(result).isEqualTo(10L);

        verify(zoneRepository).existsById("zone-1");
        verify(parcelRepository).countByZoneId("zone-1");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when counting parcels for non-existent zone")
    void testCountParcels_ZoneNotFound() {
        // GIVEN
        when(zoneRepository.existsById("zone-999")).thenReturn(false);

        // WHEN & THEN
        assertThatThrownBy(() -> zoneService.countParcels("zone-999"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Zone");

        verify(zoneRepository).existsById("zone-999");
        verify(parcelRepository, never()).countByZoneId(any());
    }

    // ==================== Tests pour getStats() ====================

    @Test
    @DisplayName("Should calculate zone statistics successfully")
    void testGetStats_Success() {
        // GIVEN
        List<Parcel> parcels = Arrays.asList(parcel1, parcel2);

        when(zoneRepository.findById("zone-1")).thenReturn(Optional.of(zone));
        when(parcelRepository.findByZoneId("zone-1")).thenReturn(parcels);

        // WHEN
        ZoneStatsDTO result = zoneService.getStats("zone-1");

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getZoneId()).isEqualTo("zone-1");
        assertThat(result.getZoneName()).isEqualTo("Zone Centre");
        assertThat(result.getTotalParcels()).isEqualTo(2L);
        assertThat(result.getTotalWeight()).isEqualTo(5.5); // 2.5 + 3.0
        assertThat(result.getInTransitParcels()).isEqualTo(1L);
        assertThat(result.getDeliveredParcels()).isEqualTo(1L);
        assertThat(result.getUnassignedParcels()).isEqualTo(2L); // Both have no delivery person

        verify(zoneRepository).findById("zone-1");
        verify(parcelRepository).findByZoneId("zone-1");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when getting stats for non-existent zone")
    void testGetStats_ZoneNotFound() {
        // GIVEN
        when(zoneRepository.findById("zone-999")).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> zoneService.getStats("zone-999"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Zone");

        verify(zoneRepository).findById("zone-999");
        verify(parcelRepository, never()).findByZoneId(any());
    }

    @Test
    @DisplayName("Should calculate stats with zero parcels")
    void testGetStats_NoParcels() {
        // GIVEN
        when(zoneRepository.findById("zone-1")).thenReturn(Optional.of(zone));
        when(parcelRepository.findByZoneId("zone-1")).thenReturn(Arrays.asList());

        // WHEN
        ZoneStatsDTO result = zoneService.getStats("zone-1");

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getTotalParcels()).isEqualTo(0L);
        assertThat(result.getTotalWeight()).isEqualTo(0.0);
        assertThat(result.getInTransitParcels()).isEqualTo(0L);
        assertThat(result.getDeliveredParcels()).isEqualTo(0L);
        assertThat(result.getUnassignedParcels()).isEqualTo(0L);
    }
}
