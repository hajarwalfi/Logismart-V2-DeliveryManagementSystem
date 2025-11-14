package com.logismart.logismartv2.service;

import com.logismart.logismartv2.dto.deliveryperson.DeliveryPersonCreateDTO;
import com.logismart.logismartv2.dto.deliveryperson.DeliveryPersonResponseDTO;
import com.logismart.logismartv2.dto.deliveryperson.DeliveryPersonStatsDTO;
import com.logismart.logismartv2.dto.deliveryperson.DeliveryPersonUpdateDTO;
import com.logismart.logismartv2.dto.parcel.ParcelResponseDTO;
import com.logismart.logismartv2.entity.DeliveryPerson;
import com.logismart.logismartv2.entity.Parcel;
import com.logismart.logismartv2.entity.ParcelPriority;
import com.logismart.logismartv2.entity.ParcelStatus;
import com.logismart.logismartv2.entity.Zone;
import com.logismart.logismartv2.exception.DuplicateResourceException;
import com.logismart.logismartv2.exception.ResourceNotFoundException;
import com.logismart.logismartv2.mapper.DeliveryPersonMapper;
import com.logismart.logismartv2.mapper.ParcelMapper;
import com.logismart.logismartv2.repository.DeliveryPersonRepository;
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
 * Tests unitaires pour DeliveryPersonService
 * Service: 8/9 - Complexe (Gestion des livreurs avec affectation de zones)
 * Méthodes testées: 14 méthodes publiques (CRUD + recherches + statistiques)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DeliveryPersonService Unit Tests")
class DeliveryPersonServiceTest {

    @Mock
    private DeliveryPersonRepository deliveryPersonRepository;

    @Mock
    private DeliveryPersonMapper deliveryPersonMapper;

    @Mock
    private ZoneRepository zoneRepository;

    @Mock
    private ParcelRepository parcelRepository;

    @Mock
    private ParcelMapper parcelMapper;

    @InjectMocks
    private DeliveryPersonService deliveryPersonService;

    private DeliveryPerson deliveryPerson;
    private DeliveryPersonCreateDTO createDTO;
    private DeliveryPersonUpdateDTO updateDTO;
    private DeliveryPersonResponseDTO responseDTO;
    private Zone zone;
    private Parcel parcel1;
    private Parcel parcel2;

    @BeforeEach
    void setUp() {
        zone = new Zone();
        zone.setId("zone-1");
        zone.setName("Zone Centre");

        deliveryPerson = new DeliveryPerson();
        deliveryPerson.setId("dp-1");
        deliveryPerson.setFirstName("John");
        deliveryPerson.setLastName("Doe");
        deliveryPerson.setPhone("+212612345678");
        deliveryPerson.setAssignedZone(zone);

        createDTO = new DeliveryPersonCreateDTO();
        createDTO.setFirstName("John");
        createDTO.setLastName("Doe");
        createDTO.setPhone("+212612345678");
        createDTO.setAssignedZoneId("zone-1");

        updateDTO = new DeliveryPersonUpdateDTO();
        updateDTO.setId("dp-1");
        updateDTO.setFirstName("Jane");
        updateDTO.setLastName("Smith");
        updateDTO.setPhone("+212687654321");
        updateDTO.setAssignedZoneId("zone-1");

        responseDTO = new DeliveryPersonResponseDTO();
        responseDTO.setId("dp-1");
        responseDTO.setFirstName("John");
        responseDTO.setLastName("Doe");
        responseDTO.setPhone("+212612345678");
        responseDTO.setAssignedZoneId("zone-1");

        parcel1 = new Parcel();
        parcel1.setId("p1");
        parcel1.setWeight(new BigDecimal("2.5"));
        parcel1.setStatus(ParcelStatus.DELIVERED);
        parcel1.setPriority(ParcelPriority.NORMAL);

        parcel2 = new Parcel();
        parcel2.setId("p2");
        parcel2.setWeight(new BigDecimal("3.5"));
        parcel2.setStatus(ParcelStatus.IN_TRANSIT);
        parcel2.setPriority(ParcelPriority.URGENT);
    }

    // ==================== Tests pour create() ====================

    @Test
    @DisplayName("Should create delivery person with zone assignment successfully")
    void testCreate_SuccessWithZone() {
        // GIVEN
        when(deliveryPersonRepository.existsByPhone(createDTO.getPhone())).thenReturn(false);
        when(zoneRepository.findById("zone-1")).thenReturn(Optional.of(zone));
        when(deliveryPersonMapper.toEntity(createDTO)).thenReturn(deliveryPerson);
        when(deliveryPersonRepository.save(deliveryPerson)).thenReturn(deliveryPerson);
        when(deliveryPersonMapper.toResponseDTO(deliveryPerson)).thenReturn(responseDTO);

        // WHEN
        DeliveryPersonResponseDTO result = deliveryPersonService.create(createDTO);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getPhone()).isEqualTo("+212612345678");
        assertThat(result.getAssignedZoneId()).isEqualTo("zone-1");

        verify(deliveryPersonRepository).existsByPhone("+212612345678");
        verify(zoneRepository).findById("zone-1");
        verify(deliveryPersonRepository).save(deliveryPerson);
    }

    @Test
    @DisplayName("Should create delivery person without zone assignment successfully")
    void testCreate_SuccessWithoutZone() {
        // GIVEN
        createDTO.setAssignedZoneId(null);
        when(deliveryPersonRepository.existsByPhone(createDTO.getPhone())).thenReturn(false);
        when(deliveryPersonMapper.toEntity(createDTO)).thenReturn(deliveryPerson);
        when(deliveryPersonRepository.save(deliveryPerson)).thenReturn(deliveryPerson);
        when(deliveryPersonMapper.toResponseDTO(deliveryPerson)).thenReturn(responseDTO);

        // WHEN
        DeliveryPersonResponseDTO result = deliveryPersonService.create(createDTO);

        // THEN
        assertThat(result).isNotNull();

        verify(deliveryPersonRepository).existsByPhone("+212612345678");
        verify(zoneRepository, never()).findById(any());
        verify(deliveryPersonRepository).save(deliveryPerson);
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when creating with existing phone")
    void testCreate_DuplicatePhone() {
        // GIVEN
        when(deliveryPersonRepository.existsByPhone(createDTO.getPhone())).thenReturn(true);

        // WHEN & THEN
        assertThatThrownBy(() -> deliveryPersonService.create(createDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("DeliveryPerson")
                .hasMessageContaining("phone");

        verify(deliveryPersonRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when creating with non-existent zone")
    void testCreate_ZoneNotFound() {
        // GIVEN
        when(deliveryPersonRepository.existsByPhone(createDTO.getPhone())).thenReturn(false);
        when(zoneRepository.findById("zone-1")).thenReturn(Optional.empty());
        when(deliveryPersonMapper.toEntity(createDTO)).thenReturn(deliveryPerson);

        // WHEN & THEN
        assertThatThrownBy(() -> deliveryPersonService.create(createDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Zone");

        verify(deliveryPersonRepository, never()).save(any());
    }

    // ==================== Tests pour findById() ====================

    @Test
    @DisplayName("Should find delivery person by id successfully")
    void testFindById_Success() {
        // GIVEN
        when(deliveryPersonRepository.findByIdWithZone("dp-1")).thenReturn(Optional.of(deliveryPerson));
        when(deliveryPersonMapper.toResponseDTO(deliveryPerson)).thenReturn(responseDTO);

        // WHEN
        DeliveryPersonResponseDTO result = deliveryPersonService.findById("dp-1");

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("dp-1");

        verify(deliveryPersonRepository).findByIdWithZone("dp-1");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when delivery person not found")
    void testFindById_NotFound() {
        // GIVEN
        when(deliveryPersonRepository.findByIdWithZone("dp-999")).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> deliveryPersonService.findById("dp-999"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("DeliveryPerson");

        verify(deliveryPersonRepository).findByIdWithZone("dp-999");
    }

    // ==================== Tests pour findAll() ====================

    @Test
    @DisplayName("Should return all delivery persons successfully")
    void testFindAll_Success() {
        // GIVEN
        List<DeliveryPerson> deliveryPersons = Arrays.asList(deliveryPerson);
        List<DeliveryPersonResponseDTO> responseDTOs = Arrays.asList(responseDTO);

        when(deliveryPersonRepository.findAllWithZones()).thenReturn(deliveryPersons);
        when(deliveryPersonMapper.toResponseDTOList(deliveryPersons)).thenReturn(responseDTOs);

        // WHEN
        List<DeliveryPersonResponseDTO> result = deliveryPersonService.findAll();

        // THEN
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        verify(deliveryPersonRepository).findAllWithZones();
    }

    // ==================== Tests pour update() ====================

    @Test
    @DisplayName("Should update delivery person successfully with zone change")
    void testUpdate_SuccessWithZone() {
        // GIVEN
        when(deliveryPersonRepository.findById(updateDTO.getId())).thenReturn(Optional.of(deliveryPerson));
        when(deliveryPersonRepository.existsByPhone(updateDTO.getPhone())).thenReturn(false);
        when(zoneRepository.findById("zone-1")).thenReturn(Optional.of(zone));
        when(deliveryPersonRepository.save(deliveryPerson)).thenReturn(deliveryPerson);
        when(deliveryPersonMapper.toResponseDTO(deliveryPerson)).thenReturn(responseDTO);

        // WHEN
        DeliveryPersonResponseDTO result = deliveryPersonService.update(updateDTO);

        // THEN
        assertThat(result).isNotNull();

        verify(deliveryPersonRepository).findById("dp-1");
        verify(deliveryPersonMapper).updateEntityFromDTO(updateDTO, deliveryPerson);
        verify(zoneRepository).findById("zone-1");
        verify(deliveryPersonRepository).save(deliveryPerson);
    }

    @Test
    @DisplayName("Should update delivery person successfully without zone (unassign)")
    void testUpdate_SuccessWithoutZone() {
        // GIVEN
        updateDTO.setAssignedZoneId(null);
        when(deliveryPersonRepository.findById(updateDTO.getId())).thenReturn(Optional.of(deliveryPerson));
        when(deliveryPersonRepository.existsByPhone(updateDTO.getPhone())).thenReturn(false);
        when(deliveryPersonRepository.save(deliveryPerson)).thenReturn(deliveryPerson);
        when(deliveryPersonMapper.toResponseDTO(deliveryPerson)).thenReturn(responseDTO);

        // WHEN
        DeliveryPersonResponseDTO result = deliveryPersonService.update(updateDTO);

        // THEN
        assertThat(result).isNotNull();

        verify(zoneRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent delivery person")
    void testUpdate_NotFound() {
        // GIVEN
        when(deliveryPersonRepository.findById(updateDTO.getId())).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> deliveryPersonService.update(updateDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("DeliveryPerson");

        verify(deliveryPersonRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when updating with existing phone")
    void testUpdate_DuplicatePhone() {
        // GIVEN
        when(deliveryPersonRepository.findById(updateDTO.getId())).thenReturn(Optional.of(deliveryPerson));
        when(deliveryPersonRepository.existsByPhone(updateDTO.getPhone())).thenReturn(true);

        // WHEN & THEN
        assertThatThrownBy(() -> deliveryPersonService.update(updateDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("phone");

        verify(deliveryPersonRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating with non-existent zone")
    void testUpdate_ZoneNotFound() {
        // GIVEN
        when(deliveryPersonRepository.findById(updateDTO.getId())).thenReturn(Optional.of(deliveryPerson));
        when(deliveryPersonRepository.existsByPhone(updateDTO.getPhone())).thenReturn(false);
        when(zoneRepository.findById("zone-1")).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> deliveryPersonService.update(updateDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Zone");

        verify(deliveryPersonRepository, never()).save(any());
    }

    // ==================== Tests pour delete() ====================

    @Test
    @DisplayName("Should delete delivery person successfully")
    void testDelete_Success() {
        // GIVEN
        when(deliveryPersonRepository.existsById("dp-1")).thenReturn(true);
        doNothing().when(deliveryPersonRepository).deleteById("dp-1");

        // WHEN
        deliveryPersonService.delete("dp-1");

        // THEN
        verify(deliveryPersonRepository).existsById("dp-1");
        verify(deliveryPersonRepository).deleteById("dp-1");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent delivery person")
    void testDelete_NotFound() {
        // GIVEN
        when(deliveryPersonRepository.existsById("dp-999")).thenReturn(false);

        // WHEN & THEN
        assertThatThrownBy(() -> deliveryPersonService.delete("dp-999"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(deliveryPersonRepository, never()).deleteById(any());
    }

    // ==================== Tests pour findByZone() ====================

    @Test
    @DisplayName("Should find delivery persons by zone successfully")
    void testFindByZone_Success() {
        // GIVEN
        List<DeliveryPerson> deliveryPersons = Arrays.asList(deliveryPerson);
        List<DeliveryPersonResponseDTO> responseDTOs = Arrays.asList(responseDTO);

        when(zoneRepository.existsById("zone-1")).thenReturn(true);
        when(deliveryPersonRepository.findByAssignedZoneId("zone-1")).thenReturn(deliveryPersons);
        when(deliveryPersonMapper.toResponseDTOList(deliveryPersons)).thenReturn(responseDTOs);

        // WHEN
        List<DeliveryPersonResponseDTO> result = deliveryPersonService.findByZone("zone-1");

        // THEN
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        verify(zoneRepository).existsById("zone-1");
        verify(deliveryPersonRepository).findByAssignedZoneId("zone-1");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when finding by non-existent zone")
    void testFindByZone_ZoneNotFound() {
        // GIVEN
        when(zoneRepository.existsById("zone-999")).thenReturn(false);

        // WHEN & THEN
        assertThatThrownBy(() -> deliveryPersonService.findByZone("zone-999"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Zone");

        verify(deliveryPersonRepository, never()).findByAssignedZoneId(any());
    }

    // ==================== Tests pour findUnassigned() ====================

    @Test
    @DisplayName("Should find unassigned delivery persons successfully")
    void testFindUnassigned_Success() {
        // GIVEN
        List<DeliveryPerson> unassigned = Arrays.asList(deliveryPerson);
        List<DeliveryPersonResponseDTO> responseDTOs = Arrays.asList(responseDTO);

        when(deliveryPersonRepository.findUnassignedDeliveryPersons()).thenReturn(unassigned);
        when(deliveryPersonMapper.toResponseDTOList(unassigned)).thenReturn(responseDTOs);

        // WHEN
        List<DeliveryPersonResponseDTO> result = deliveryPersonService.findUnassigned();

        // THEN
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        verify(deliveryPersonRepository).findUnassignedDeliveryPersons();
    }

    // ==================== Tests pour findAvailable() ====================

    @Test
    @DisplayName("Should find available delivery persons successfully")
    void testFindAvailable_Success() {
        // GIVEN
        List<DeliveryPerson> available = Arrays.asList(deliveryPerson);
        List<DeliveryPersonResponseDTO> responseDTOs = Arrays.asList(responseDTO);

        when(deliveryPersonRepository.findAvailableDeliveryPersons()).thenReturn(available);
        when(deliveryPersonMapper.toResponseDTOList(available)).thenReturn(responseDTOs);

        // WHEN
        List<DeliveryPersonResponseDTO> result = deliveryPersonService.findAvailable();

        // THEN
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        verify(deliveryPersonRepository).findAvailableDeliveryPersons();
    }

    // ==================== Tests pour findAvailableInZone() ====================

    @Test
    @DisplayName("Should find available delivery persons in zone successfully")
    void testFindAvailableInZone_Success() {
        // GIVEN
        List<DeliveryPerson> available = Arrays.asList(deliveryPerson);
        List<DeliveryPersonResponseDTO> responseDTOs = Arrays.asList(responseDTO);

        when(zoneRepository.existsById("zone-1")).thenReturn(true);
        when(deliveryPersonRepository.findAvailableInZone("zone-1")).thenReturn(available);
        when(deliveryPersonMapper.toResponseDTOList(available)).thenReturn(responseDTOs);

        // WHEN
        List<DeliveryPersonResponseDTO> result = deliveryPersonService.findAvailableInZone("zone-1");

        // THEN
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        verify(zoneRepository).existsById("zone-1");
        verify(deliveryPersonRepository).findAvailableInZone("zone-1");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when finding available in non-existent zone")
    void testFindAvailableInZone_ZoneNotFound() {
        // GIVEN
        when(zoneRepository.existsById("zone-999")).thenReturn(false);

        // WHEN & THEN
        assertThatThrownBy(() -> deliveryPersonService.findAvailableInZone("zone-999"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Zone");

        verify(deliveryPersonRepository, never()).findAvailableInZone(any());
    }

    // ==================== Tests pour countActiveParcels() ====================

    @Test
    @DisplayName("Should count active parcels successfully")
    void testCountActiveParcels_Success() {
        // GIVEN
        when(deliveryPersonRepository.existsById("dp-1")).thenReturn(true);
        when(deliveryPersonRepository.countActiveParcels("dp-1")).thenReturn(5L);

        // WHEN
        Long result = deliveryPersonService.countActiveParcels("dp-1");

        // THEN
        assertThat(result).isEqualTo(5L);

        verify(deliveryPersonRepository).existsById("dp-1");
        verify(deliveryPersonRepository).countActiveParcels("dp-1");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when counting parcels for non-existent delivery person")
    void testCountActiveParcels_NotFound() {
        // GIVEN
        when(deliveryPersonRepository.existsById("dp-999")).thenReturn(false);

        // WHEN & THEN
        assertThatThrownBy(() -> deliveryPersonService.countActiveParcels("dp-999"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(deliveryPersonRepository, never()).countActiveParcels(any());
    }

    // ==================== Tests pour countDeliveredParcels() ====================

    @Test
    @DisplayName("Should count delivered parcels successfully")
    void testCountDeliveredParcels_Success() {
        // GIVEN
        when(deliveryPersonRepository.existsById("dp-1")).thenReturn(true);
        when(deliveryPersonRepository.countDeliveredParcels("dp-1")).thenReturn(10L);

        // WHEN
        Long result = deliveryPersonService.countDeliveredParcels("dp-1");

        // THEN
        assertThat(result).isEqualTo(10L);

        verify(deliveryPersonRepository).existsById("dp-1");
        verify(deliveryPersonRepository).countDeliveredParcels("dp-1");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when counting delivered for non-existent delivery person")
    void testCountDeliveredParcels_NotFound() {
        // GIVEN
        when(deliveryPersonRepository.existsById("dp-999")).thenReturn(false);

        // WHEN & THEN
        assertThatThrownBy(() -> deliveryPersonService.countDeliveredParcels("dp-999"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(deliveryPersonRepository, never()).countDeliveredParcels(any());
    }

    // ==================== Tests pour findUrgentParcels() ====================

    @Test
    @DisplayName("Should find urgent parcels successfully")
    void testFindUrgentParcels_Success() {
        // GIVEN
        Parcel expressParcel = new Parcel();
        expressParcel.setId("p3");
        expressParcel.setPriority(ParcelPriority.EXPRESS);

        List<Parcel> allParcels = Arrays.asList(parcel1, parcel2, expressParcel);
        List<Parcel> urgentParcels = Arrays.asList(parcel2, expressParcel);
        List<ParcelResponseDTO> parcelDTOs = Arrays.asList(new ParcelResponseDTO(), new ParcelResponseDTO());

        when(deliveryPersonRepository.existsById("dp-1")).thenReturn(true);
        when(parcelRepository.findByDeliveryPersonId("dp-1")).thenReturn(allParcels);
        when(parcelMapper.toResponseDTOList(anyList())).thenReturn(parcelDTOs);

        // WHEN
        List<ParcelResponseDTO> result = deliveryPersonService.findUrgentParcels("dp-1");

        // THEN
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);

        verify(deliveryPersonRepository).existsById("dp-1");
        verify(parcelRepository).findByDeliveryPersonId("dp-1");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when finding urgent parcels for non-existent delivery person")
    void testFindUrgentParcels_NotFound() {
        // GIVEN
        when(deliveryPersonRepository.existsById("dp-999")).thenReturn(false);

        // WHEN & THEN
        assertThatThrownBy(() -> deliveryPersonService.findUrgentParcels("dp-999"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(parcelRepository, never()).findByDeliveryPersonId(any());
    }

    // ==================== Tests pour getStats() ====================

    @Test
    @DisplayName("Should calculate delivery person stats successfully")
    void testGetStats_Success() {
        // GIVEN
        List<Parcel> parcels = Arrays.asList(parcel1, parcel2);

        when(deliveryPersonRepository.findById("dp-1")).thenReturn(Optional.of(deliveryPerson));
        when(parcelRepository.findByDeliveryPersonId("dp-1")).thenReturn(parcels);

        // WHEN
        DeliveryPersonStatsDTO result = deliveryPersonService.getStats("dp-1");

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getDeliveryPersonId()).isEqualTo("dp-1");
        assertThat(result.getDeliveryPersonName()).isEqualTo("John Doe");
        assertThat(result.getTotalParcels()).isEqualTo(2L);
        assertThat(result.getTotalWeight()).isEqualTo(6.0); // 2.5 + 3.5
        assertThat(result.getActiveParcels()).isEqualTo(1L); // IN_TRANSIT
        assertThat(result.getDeliveredParcels()).isEqualTo(1L);
        assertThat(result.getInTransitParcels()).isEqualTo(1L);

        verify(deliveryPersonRepository).findById("dp-1");
        verify(parcelRepository).findByDeliveryPersonId("dp-1");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when getting stats for non-existent delivery person")
    void testGetStats_NotFound() {
        // GIVEN
        when(deliveryPersonRepository.findById("dp-999")).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> deliveryPersonService.getStats("dp-999"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(parcelRepository, never()).findByDeliveryPersonId(any());
    }
}
