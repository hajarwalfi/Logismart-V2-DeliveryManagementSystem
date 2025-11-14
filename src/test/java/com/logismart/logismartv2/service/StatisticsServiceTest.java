package com.logismart.logismartv2.service;

import com.logismart.logismartv2.dto.statistics.DeliveryPersonStatisticsDTO;
import com.logismart.logismartv2.dto.statistics.GlobalStatisticsDTO;
import com.logismart.logismartv2.dto.statistics.ZoneStatisticsDTO;
import com.logismart.logismartv2.entity.*;
import com.logismart.logismartv2.exception.ResourceNotFoundException;
import com.logismart.logismartv2.repository.*;
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
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour StatisticsService
 * Service: 7/9 - Moyen (Calcul de statistiques complexes)
 * Méthodes testées: 5 méthodes publiques (statistiques par livr eur, zone, globales)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StatisticsService Unit Tests")
class StatisticsServiceTest {

    @Mock
    private ParcelRepository parcelRepository;

    @Mock
    private DeliveryPersonRepository deliveryPersonRepository;

    @Mock
    private ZoneRepository zoneRepository;

    @Mock
    private SenderClientRepository senderClientRepository;

    @Mock
    private RecipientRepository recipientRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private StatisticsService statisticsService;

    private DeliveryPerson deliveryPerson;
    private Zone zone;
    private Parcel parcel1;
    private Parcel parcel2;
    private Parcel parcel3;

    @BeforeEach
    void setUp() {
        zone = new Zone();
        zone.setId("zone-1");
        zone.setName("Zone Centre");
        zone.setPostalCode("20000");

        deliveryPerson = new DeliveryPerson();
        deliveryPerson.setId("dp-1");
        deliveryPerson.setFirstName("John");
        deliveryPerson.setLastName("Doe");
        deliveryPerson.setAssignedZone(zone);

        parcel1 = new Parcel();
        parcel1.setId("p1");
        parcel1.setWeight(new BigDecimal("2.5"));
        parcel1.setStatus(ParcelStatus.DELIVERED);
        parcel1.setPriority(ParcelPriority.NORMAL);
        parcel1.setDeliveryPerson(deliveryPerson);

        parcel2 = new Parcel();
        parcel2.setId("p2");
        parcel2.setWeight(new BigDecimal("3.5"));
        parcel2.setStatus(ParcelStatus.IN_TRANSIT);
        parcel2.setPriority(ParcelPriority.URGENT);
        parcel2.setDeliveryPerson(deliveryPerson);

        parcel3 = new Parcel();
        parcel3.setId("p3");
        parcel3.setWeight(new BigDecimal("1.0"));
        parcel3.setStatus(ParcelStatus.CREATED);
        parcel3.setPriority(ParcelPriority.EXPRESS);
    }

    // ==================== Tests pour getDeliveryPersonStatistics() ====================

    @Test
    @DisplayName("Should calculate delivery person statistics successfully with parcels")
    void testGetDeliveryPersonStatistics_Success() {
        // GIVEN
        List<Parcel> parcels = Arrays.asList(parcel1, parcel2);

        when(deliveryPersonRepository.findById("dp-1")).thenReturn(Optional.of(deliveryPerson));
        when(parcelRepository.findByDeliveryPersonId("dp-1")).thenReturn(parcels);

        // WHEN
        DeliveryPersonStatisticsDTO result = statisticsService.getDeliveryPersonStatistics("dp-1");

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getDeliveryPersonId()).isEqualTo("dp-1");
        assertThat(result.getDeliveryPersonName()).isEqualTo("John Doe");
        assertThat(result.getZoneName()).isEqualTo("Zone Centre");
        assertThat(result.getTotalParcels()).isEqualTo(2L);
        assertThat(result.getTotalWeight()).isEqualTo(new BigDecimal("6.0")); // 2.5 + 3.5
        assertThat(result.getAverageWeight()).isEqualTo(new BigDecimal("3.00")); // 6.0 / 2
        assertThat(result.getParcelsDelivered()).isEqualTo(1L);
        assertThat(result.getParcelsInTransit()).isEqualTo(1L);
        assertThat(result.getDeliveryRate()).isEqualTo(50.0); // 1/2 * 100

        verify(deliveryPersonRepository).findById("dp-1");
        verify(parcelRepository).findByDeliveryPersonId("dp-1");
    }

    @Test
    @DisplayName("Should calculate delivery person statistics with zero parcels")
    void testGetDeliveryPersonStatistics_NoParcels() {
        // GIVEN
        when(deliveryPersonRepository.findById("dp-1")).thenReturn(Optional.of(deliveryPerson));
        when(parcelRepository.findByDeliveryPersonId("dp-1")).thenReturn(Arrays.asList());

        // WHEN
        DeliveryPersonStatisticsDTO result = statisticsService.getDeliveryPersonStatistics("dp-1");

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getTotalParcels()).isEqualTo(0L);
        assertThat(result.getTotalWeight()).isEqualTo(BigDecimal.ZERO);
        assertThat(result.getAverageWeight()).isEqualTo(BigDecimal.ZERO);
        assertThat(result.getDeliveryRate()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when delivery person not found")
    void testGetDeliveryPersonStatistics_NotFound() {
        // GIVEN
        when(deliveryPersonRepository.findById("dp-999")).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> statisticsService.getDeliveryPersonStatistics("dp-999"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("DeliveryPerson");

        verify(deliveryPersonRepository).findById("dp-999");
        verify(parcelRepository, never()).findByDeliveryPersonId(any());
    }

    @Test
    @DisplayName("Should handle delivery person with no assigned zone")
    void testGetDeliveryPersonStatistics_NoZone() {
        // GIVEN
        deliveryPerson.setAssignedZone(null);
        when(deliveryPersonRepository.findById("dp-1")).thenReturn(Optional.of(deliveryPerson));
        when(parcelRepository.findByDeliveryPersonId("dp-1")).thenReturn(Arrays.asList());

        // WHEN
        DeliveryPersonStatisticsDTO result = statisticsService.getDeliveryPersonStatistics("dp-1");

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getZoneName()).isEqualTo("Unassigned");
    }

    // ==================== Tests pour getZoneStatistics() ====================

    @Test
    @DisplayName("Should calculate zone statistics successfully with parcels")
    void testGetZoneStatistics_Success() {
        // GIVEN
        List<Parcel> parcels = Arrays.asList(parcel1, parcel2, parcel3);

        when(zoneRepository.findById("zone-1")).thenReturn(Optional.of(zone));
        when(parcelRepository.findByZoneId("zone-1")).thenReturn(parcels);
        when(zoneRepository.countDeliveryPersonsByZoneId("zone-1")).thenReturn(2L);

        // WHEN
        ZoneStatisticsDTO result = statisticsService.getZoneStatistics("zone-1");

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getZoneId()).isEqualTo("zone-1");
        assertThat(result.getZoneName()).isEqualTo("Zone Centre");
        assertThat(result.getPostalCode()).isEqualTo("20000");
        assertThat(result.getTotalParcels()).isEqualTo(3L);
        assertThat(result.getTotalWeight()).isEqualTo(new BigDecimal("7.0")); // 2.5 + 3.5 + 1.0
        assertThat(result.getAverageWeight()).isEqualTo(new BigDecimal("2.33")); // 7.0 / 3
        assertThat(result.getDeliveryPersonCount()).isEqualTo(2L);
        assertThat(result.getParcelsDelivered()).isEqualTo(1L);
        assertThat(result.getParcelsInTransit()).isEqualTo(1L);
        assertThat(result.getParcelsCreated()).isEqualTo(1L);
        assertThat(result.getParcelsNormal()).isEqualTo(1L);
        assertThat(result.getParcelsUrgent()).isEqualTo(1L);
        assertThat(result.getParcelsExpress()).isEqualTo(1L);
        assertThat(result.getAverageParcelsPerDeliveryPerson()).isEqualTo(1.5); // 3 / 2

        verify(zoneRepository).findById("zone-1");
        verify(parcelRepository).findByZoneId("zone-1");
        verify(zoneRepository).countDeliveryPersonsByZoneId("zone-1");
    }

    @Test
    @DisplayName("Should calculate zone statistics with zero parcels")
    void testGetZoneStatistics_NoParcels() {
        // GIVEN
        when(zoneRepository.findById("zone-1")).thenReturn(Optional.of(zone));
        when(parcelRepository.findByZoneId("zone-1")).thenReturn(Arrays.asList());
        when(zoneRepository.countDeliveryPersonsByZoneId("zone-1")).thenReturn(3L);

        // WHEN
        ZoneStatisticsDTO result = statisticsService.getZoneStatistics("zone-1");

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getTotalParcels()).isEqualTo(0L);
        assertThat(result.getTotalWeight()).isEqualTo(BigDecimal.ZERO);
        assertThat(result.getAverageWeight()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should calculate zone statistics with zero delivery persons")
    void testGetZoneStatistics_NoDeliveryPersons() {
        // GIVEN
        List<Parcel> parcels = Arrays.asList(parcel1);

        when(zoneRepository.findById("zone-1")).thenReturn(Optional.of(zone));
        when(parcelRepository.findByZoneId("zone-1")).thenReturn(parcels);
        when(zoneRepository.countDeliveryPersonsByZoneId("zone-1")).thenReturn(0L);

        // WHEN
        ZoneStatisticsDTO result = statisticsService.getZoneStatistics("zone-1");

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getAverageParcelsPerDeliveryPerson()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when zone not found")
    void testGetZoneStatistics_NotFound() {
        // GIVEN
        when(zoneRepository.findById("zone-999")).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> statisticsService.getZoneStatistics("zone-999"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Zone");

        verify(zoneRepository).findById("zone-999");
        verify(parcelRepository, never()).findByZoneId(any());
    }

    // ==================== Tests pour getGlobalStatistics() ====================

    @Test
    @DisplayName("Should calculate global statistics successfully")
    void testGetGlobalStatistics_Success() {
        // GIVEN
        List<Parcel> allParcels = Arrays.asList(parcel1, parcel2, parcel3);

        when(parcelRepository.count()).thenReturn(3L);
        when(zoneRepository.count()).thenReturn(5L);
        when(deliveryPersonRepository.count()).thenReturn(10L);
        when(senderClientRepository.count()).thenReturn(20L);
        when(recipientRepository.count()).thenReturn(30L);
        when(productRepository.count()).thenReturn(100L);
        when(parcelRepository.findAll()).thenReturn(allParcels);

        // WHEN
        GlobalStatisticsDTO result = statisticsService.getGlobalStatistics();

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getTotalParcels()).isEqualTo(3L);
        assertThat(result.getTotalZones()).isEqualTo(5L);
        assertThat(result.getTotalDeliveryPersons()).isEqualTo(10L);
        assertThat(result.getTotalSenderClients()).isEqualTo(20L);
        assertThat(result.getTotalRecipients()).isEqualTo(30L);
        assertThat(result.getTotalProducts()).isEqualTo(100L);
        assertThat(result.getTotalWeight()).isEqualTo(new BigDecimal("7.0"));
        assertThat(result.getAverageWeight()).isEqualTo(new BigDecimal("2.33"));
        assertThat(result.getAverageParcelsPerDeliveryPerson()).isEqualTo(0.3); // 3 / 10

        // Verify status counts
        assertThat(result.getParcelsByStatus().get("DELIVERED")).isEqualTo(1L);
        assertThat(result.getParcelsByStatus().get("IN_TRANSIT")).isEqualTo(1L);
        assertThat(result.getParcelsByStatus().get("CREATED")).isEqualTo(1L);
        assertThat(result.getParcelsByStatus().get("COLLECTED")).isEqualTo(0L);
        assertThat(result.getParcelsByStatus().get("IN_STOCK")).isEqualTo(0L);

        // Verify priority counts
        assertThat(result.getParcelsByPriority().get("NORMAL")).isEqualTo(1L);
        assertThat(result.getParcelsByPriority().get("URGENT")).isEqualTo(1L);
        assertThat(result.getParcelsByPriority().get("EXPRESS")).isEqualTo(1L);

        // Verify unassigned (parcel3 has no delivery person)
        assertThat(result.getUnassignedParcels()).isEqualTo(1L);

        // Verify high priority pending (URGENT + EXPRESS not delivered = 2)
        assertThat(result.getHighPriorityPending()).isEqualTo(2L);

        verify(parcelRepository).count();
        verify(parcelRepository).findAll();
    }

    @Test
    @DisplayName("Should calculate global statistics with zero parcels")
    void testGetGlobalStatistics_NoParcels() {
        // GIVEN
        when(parcelRepository.count()).thenReturn(0L);
        when(zoneRepository.count()).thenReturn(5L);
        when(deliveryPersonRepository.count()).thenReturn(10L);
        when(senderClientRepository.count()).thenReturn(0L);
        when(recipientRepository.count()).thenReturn(0L);
        when(productRepository.count()).thenReturn(0L);
        when(parcelRepository.findAll()).thenReturn(Arrays.asList());

        // WHEN
        GlobalStatisticsDTO result = statisticsService.getGlobalStatistics();

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getTotalParcels()).isEqualTo(0L);
        assertThat(result.getTotalWeight()).isEqualTo(BigDecimal.ZERO);
        assertThat(result.getAverageWeight()).isEqualTo(BigDecimal.ZERO);
        assertThat(result.getUnassignedParcels()).isEqualTo(0L);
        assertThat(result.getHighPriorityPending()).isEqualTo(0L);
    }

    @Test
    @DisplayName("Should calculate global statistics with zero delivery persons")
    void testGetGlobalStatistics_NoDeliveryPersons() {
        // GIVEN
        List<Parcel> parcels = Arrays.asList(parcel1);

        when(parcelRepository.count()).thenReturn(1L);
        when(zoneRepository.count()).thenReturn(0L);
        when(deliveryPersonRepository.count()).thenReturn(0L);
        when(senderClientRepository.count()).thenReturn(0L);
        when(recipientRepository.count()).thenReturn(0L);
        when(productRepository.count()).thenReturn(0L);
        when(parcelRepository.findAll()).thenReturn(parcels);

        // WHEN
        GlobalStatisticsDTO result = statisticsService.getGlobalStatistics();

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getAverageParcelsPerDeliveryPerson()).isEqualTo(0.0);
    }

    // ==================== Tests pour getAllDeliveryPersonStatistics() ====================

    @Test
    @DisplayName("Should calculate statistics for all delivery persons successfully")
    void testGetAllDeliveryPersonStatistics_Success() {
        // GIVEN
        DeliveryPerson dp2 = new DeliveryPerson();
        dp2.setId("dp-2");
        dp2.setFirstName("Jane");
        dp2.setLastName("Smith");
        dp2.setAssignedZone(zone);

        List<DeliveryPerson> allDeliveryPersons = Arrays.asList(deliveryPerson, dp2);

        when(deliveryPersonRepository.findAll()).thenReturn(allDeliveryPersons);
        when(deliveryPersonRepository.findById("dp-1")).thenReturn(Optional.of(deliveryPerson));
        when(deliveryPersonRepository.findById("dp-2")).thenReturn(Optional.of(dp2));
        when(parcelRepository.findByDeliveryPersonId("dp-1")).thenReturn(Arrays.asList(parcel1));
        when(parcelRepository.findByDeliveryPersonId("dp-2")).thenReturn(Arrays.asList(parcel2));

        // WHEN
        List<DeliveryPersonStatisticsDTO> result = statisticsService.getAllDeliveryPersonStatistics();

        // THEN
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getDeliveryPersonId()).isEqualTo("dp-1");
        assertThat(result.get(1).getDeliveryPersonId()).isEqualTo("dp-2");

        verify(deliveryPersonRepository).findAll();
        verify(parcelRepository, times(2)).findByDeliveryPersonId(any());
    }

    @Test
    @DisplayName("Should return empty list when no delivery persons exist")
    void testGetAllDeliveryPersonStatistics_Empty() {
        // GIVEN
        when(deliveryPersonRepository.findAll()).thenReturn(Arrays.asList());

        // WHEN
        List<DeliveryPersonStatisticsDTO> result = statisticsService.getAllDeliveryPersonStatistics();

        // THEN
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(deliveryPersonRepository).findAll();
        verify(parcelRepository, never()).findByDeliveryPersonId(any());
    }

    // ==================== Tests pour getAllZoneStatistics() ====================

    @Test
    @DisplayName("Should calculate statistics for all zones successfully")
    void testGetAllZoneStatistics_Success() {
        // GIVEN
        Zone zone2 = new Zone();
        zone2.setId("zone-2");
        zone2.setName("Zone Nord");
        zone2.setPostalCode("30000");

        List<Zone> allZones = Arrays.asList(zone, zone2);

        when(zoneRepository.findAll()).thenReturn(allZones);
        when(zoneRepository.findById("zone-1")).thenReturn(Optional.of(zone));
        when(zoneRepository.findById("zone-2")).thenReturn(Optional.of(zone2));
        when(parcelRepository.findByZoneId("zone-1")).thenReturn(Arrays.asList(parcel1));
        when(parcelRepository.findByZoneId("zone-2")).thenReturn(Arrays.asList(parcel2));
        when(zoneRepository.countDeliveryPersonsByZoneId("zone-1")).thenReturn(2L);
        when(zoneRepository.countDeliveryPersonsByZoneId("zone-2")).thenReturn(3L);

        // WHEN
        List<ZoneStatisticsDTO> result = statisticsService.getAllZoneStatistics();

        // THEN
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getZoneId()).isEqualTo("zone-1");
        assertThat(result.get(1).getZoneId()).isEqualTo("zone-2");

        verify(zoneRepository).findAll();
        verify(parcelRepository, times(2)).findByZoneId(any());
    }

    @Test
    @DisplayName("Should return empty list when no zones exist")
    void testGetAllZoneStatistics_Empty() {
        // GIVEN
        when(zoneRepository.findAll()).thenReturn(Arrays.asList());

        // WHEN
        List<ZoneStatisticsDTO> result = statisticsService.getAllZoneStatistics();

        // THEN
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(zoneRepository).findAll();
        verify(parcelRepository, never()).findByZoneId(any());
    }
}
