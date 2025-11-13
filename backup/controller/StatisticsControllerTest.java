package com.logismart.logismartv2.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logismart.logismartv2.dto.statistics.DeliveryPersonStatisticsDTO;
import com.logismart.logismartv2.dto.statistics.GlobalStatisticsDTO;
import com.logismart.logismartv2.dto.statistics.ZoneStatisticsDTO;
import com.logismart.logismartv2.exception.ResourceNotFoundException;
import com.logismart.logismartv2.service.StatisticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitaires pour StatisticsController
 * Utilise MockMvc pour tester les endpoints REST de statistiques
 */
@WebMvcTest(StatisticsController.class)
@DisplayName("StatisticsController Unit Tests")
class StatisticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StatisticsService statisticsService;

    private GlobalStatisticsDTO globalStatisticsDTO;
    private DeliveryPersonStatisticsDTO deliveryPersonStatisticsDTO;
    private ZoneStatisticsDTO zoneStatisticsDTO;

    @BeforeEach
    void setUp() {
        // Setup Global Statistics DTO
        Map<String, Long> parcelsByStatus = new HashMap<>();
        parcelsByStatus.put("CREATED", 100L);
        parcelsByStatus.put("COLLECTED", 80L);
        parcelsByStatus.put("IN_STOCK", 50L);
        parcelsByStatus.put("IN_TRANSIT", 40L);
        parcelsByStatus.put("DELIVERED", 150L);

        Map<String, Long> parcelsByPriority = new HashMap<>();
        parcelsByPriority.put("NORMAL", 250L);
        parcelsByPriority.put("URGENT", 50L);
        parcelsByPriority.put("EXPRESS", 20L);

        globalStatisticsDTO = GlobalStatisticsDTO.builder()
                .totalParcels(320L)
                .totalWeight(BigDecimal.valueOf(1500.50))
                .totalZones(5L)
                .totalDeliveryPersons(15L)
                .totalSenderClients(100L)
                .totalRecipients(250L)
                .totalProducts(500L)
                .parcelsByStatus(parcelsByStatus)
                .parcelsByPriority(parcelsByPriority)
                .unassignedParcels(10L)
                .highPriorityPending(8L)
                .averageParcelsPerDeliveryPerson(21.33)
                .averageWeight(BigDecimal.valueOf(4.69))
                .build();

        // Setup Delivery Person Statistics DTO
        deliveryPersonStatisticsDTO = DeliveryPersonStatisticsDTO.builder()
                .deliveryPersonId("dp-1")
                .deliveryPersonName("John Doe")
                .zoneName("Zone A")
                .totalParcels(45L)
                .totalWeight(BigDecimal.valueOf(200.75))
                .averageWeight(BigDecimal.valueOf(4.46))
                .parcelsCreated(45L)
                .parcelsCollected(40L)
                .parcelsInStock(15L)
                .parcelsInTransit(10L)
                .parcelsDelivered(20L)
                .deliveryRate(44.44)
                .build();

        // Setup Zone Statistics DTO
        zoneStatisticsDTO = ZoneStatisticsDTO.builder()
                .zoneId("zone-1")
                .zoneName("Zone A")
                .postalCode("75001")
                .totalParcels(120L)
                .totalWeight(BigDecimal.valueOf(600.25))
                .averageWeight(BigDecimal.valueOf(5.00))
                .deliveryPersonCount(8L)
                .parcelsCreated(120L)
                .parcelsCollected(100L)
                .parcelsInStock(45L)
                .parcelsInTransit(35L)
                .parcelsDelivered(40L)
                .parcelsNormal(100L)
                .parcelsUrgent(15L)
                .parcelsExpress(5L)
                .averageParcelsPerDeliveryPerson(15.0)
                .build();
    }

    // ==================== Tests pour GET /api/statistics/global ====================

    @Test
    @DisplayName("Should get global statistics successfully and return 200")
    void testGetGlobalStatistics_Success() throws Exception {
        // GIVEN
        when(statisticsService.getGlobalStatistics()).thenReturn(globalStatisticsDTO);

        // WHEN & THEN
        mockMvc.perform(get("/api/statistics/global"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalParcels").value(320L))
                .andExpect(jsonPath("$.totalWeight").value(1500.50))
                .andExpect(jsonPath("$.totalZones").value(5L))
                .andExpect(jsonPath("$.totalDeliveryPersons").value(15L))
                .andExpect(jsonPath("$.totalSenderClients").value(100L))
                .andExpect(jsonPath("$.totalRecipients").value(250L))
                .andExpect(jsonPath("$.totalProducts").value(500L))
                .andExpect(jsonPath("$.unassignedParcels").value(10L))
                .andExpect(jsonPath("$.highPriorityPending").value(8L))
                .andExpect(jsonPath("$.averageParcelsPerDeliveryPerson").value(21.33))
                .andExpect(jsonPath("$.averageWeight").value(4.69));

        verify(statisticsService).getGlobalStatistics();
    }

    @Test
    @DisplayName("Should return 200 with parcelsByStatus and parcelsByPriority maps")
    void testGetGlobalStatistics_WithMaps() throws Exception {
        // GIVEN
        when(statisticsService.getGlobalStatistics()).thenReturn(globalStatisticsDTO);

        // WHEN & THEN
        mockMvc.perform(get("/api/statistics/global"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parcelsByStatus.CREATED").value(100L))
                .andExpect(jsonPath("$.parcelsByStatus.DELIVERED").value(150L))
                .andExpect(jsonPath("$.parcelsByPriority.NORMAL").value(250L))
                .andExpect(jsonPath("$.parcelsByPriority.URGENT").value(50L))
                .andExpect(jsonPath("$.parcelsByPriority.EXPRESS").value(20L));

        verify(statisticsService).getGlobalStatistics();
    }

    // ==================== Tests pour GET /api/statistics/delivery-person/{id} ====================

    @Test
    @DisplayName("Should get delivery person statistics by id successfully and return 200")
    void testGetDeliveryPersonStatistics_Success() throws Exception {
        // GIVEN
        when(statisticsService.getDeliveryPersonStatistics("dp-1")).thenReturn(deliveryPersonStatisticsDTO);

        // WHEN & THEN
        mockMvc.perform(get("/api/statistics/delivery-person/dp-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deliveryPersonId").value("dp-1"))
                .andExpect(jsonPath("$.deliveryPersonName").value("John Doe"))
                .andExpect(jsonPath("$.zoneName").value("Zone A"))
                .andExpect(jsonPath("$.totalParcels").value(45L))
                .andExpect(jsonPath("$.totalWeight").value(200.75))
                .andExpect(jsonPath("$.averageWeight").value(4.46))
                .andExpect(jsonPath("$.parcelsDelivered").value(20L))
                .andExpect(jsonPath("$.deliveryRate").value(44.44));

        verify(statisticsService).getDeliveryPersonStatistics("dp-1");
    }

    @Test
    @DisplayName("Should return 404 when delivery person not found")
    void testGetDeliveryPersonStatistics_NotFound() throws Exception {
        // GIVEN
        when(statisticsService.getDeliveryPersonStatistics("dp-999"))
                .thenThrow(new ResourceNotFoundException("DeliveryPerson", "id", "dp-999"));

        // WHEN & THEN
        mockMvc.perform(get("/api/statistics/delivery-person/dp-999"))
                .andExpect(status().isNotFound());

        verify(statisticsService).getDeliveryPersonStatistics("dp-999");
    }

    @Test
    @DisplayName("Should return 200 with all parcels status breakdowns")
    void testGetDeliveryPersonStatistics_AllStatuses() throws Exception {
        // GIVEN
        when(statisticsService.getDeliveryPersonStatistics("dp-1")).thenReturn(deliveryPersonStatisticsDTO);

        // WHEN & THEN
        mockMvc.perform(get("/api/statistics/delivery-person/dp-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parcelsCreated").value(45L))
                .andExpect(jsonPath("$.parcelsCollected").value(40L))
                .andExpect(jsonPath("$.parcelsInStock").value(15L))
                .andExpect(jsonPath("$.parcelsInTransit").value(10L))
                .andExpect(jsonPath("$.parcelsDelivered").value(20L));

        verify(statisticsService).getDeliveryPersonStatistics("dp-1");
    }

    // ==================== Tests pour GET /api/statistics/delivery-person ====================

    @Test
    @DisplayName("Should get all delivery persons statistics successfully and return 200")
    void testGetAllDeliveryPersonStatistics_Success() throws Exception {
        // GIVEN
        DeliveryPersonStatisticsDTO deliveryPerson2 = DeliveryPersonStatisticsDTO.builder()
                .deliveryPersonId("dp-2")
                .deliveryPersonName("Jane Smith")
                .zoneName("Zone B")
                .totalParcels(55L)
                .totalWeight(BigDecimal.valueOf(275.50))
                .averageWeight(BigDecimal.valueOf(5.00))
                .parcelsCreated(55L)
                .parcelsCollected(50L)
                .parcelsInStock(20L)
                .parcelsInTransit(15L)
                .parcelsDelivered(30L)
                .deliveryRate(54.55)
                .build();

        List<DeliveryPersonStatisticsDTO> allStats = Arrays.asList(deliveryPersonStatisticsDTO, deliveryPerson2);
        when(statisticsService.getAllDeliveryPersonStatistics()).thenReturn(allStats);

        // WHEN & THEN
        mockMvc.perform(get("/api/statistics/delivery-person"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].deliveryPersonId").value("dp-1"))
                .andExpect(jsonPath("$[0].deliveryPersonName").value("John Doe"))
                .andExpect(jsonPath("$[1].deliveryPersonId").value("dp-2"))
                .andExpect(jsonPath("$[1].deliveryPersonName").value("Jane Smith"));

        verify(statisticsService).getAllDeliveryPersonStatistics();
    }

    @Test
    @DisplayName("Should return empty list when no delivery persons exist")
    void testGetAllDeliveryPersonStatistics_EmptyList() throws Exception {
        // GIVEN
        when(statisticsService.getAllDeliveryPersonStatistics()).thenReturn(Arrays.asList());

        // WHEN & THEN
        mockMvc.perform(get("/api/statistics/delivery-person"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(statisticsService).getAllDeliveryPersonStatistics();
    }

    @Test
    @DisplayName("Should return 200 with multiple delivery persons and their statistics")
    void testGetAllDeliveryPersonStatistics_MultiplePersons() throws Exception {
        // GIVEN
        DeliveryPersonStatisticsDTO person2 = DeliveryPersonStatisticsDTO.builder()
                .deliveryPersonId("dp-2")
                .deliveryPersonName("Jane Smith")
                .zoneName("Zone B")
                .totalParcels(55L)
                .totalWeight(BigDecimal.valueOf(275.50))
                .parcelsDelivered(30L)
                .deliveryRate(54.55)
                .build();

        DeliveryPersonStatisticsDTO person3 = DeliveryPersonStatisticsDTO.builder()
                .deliveryPersonId("dp-3")
                .deliveryPersonName("Mike Johnson")
                .zoneName("Zone C")
                .totalParcels(65L)
                .totalWeight(BigDecimal.valueOf(325.75))
                .parcelsDelivered(35L)
                .deliveryRate(53.85)
                .build();

        List<DeliveryPersonStatisticsDTO> allStats = Arrays.asList(deliveryPersonStatisticsDTO, person2, person3);
        when(statisticsService.getAllDeliveryPersonStatistics()).thenReturn(allStats);

        // WHEN & THEN
        mockMvc.perform(get("/api/statistics/delivery-person"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].deliveryPersonId").value("dp-1"))
                .andExpect(jsonPath("$[1].deliveryPersonId").value("dp-2"))
                .andExpect(jsonPath("$[2].deliveryPersonId").value("dp-3"));

        verify(statisticsService).getAllDeliveryPersonStatistics();
    }

    // ==================== Tests pour GET /api/statistics/zone/{id} ====================

    @Test
    @DisplayName("Should get zone statistics by id successfully and return 200")
    void testGetZoneStatistics_Success() throws Exception {
        // GIVEN
        when(statisticsService.getZoneStatistics("zone-1")).thenReturn(zoneStatisticsDTO);

        // WHEN & THEN
        mockMvc.perform(get("/api/statistics/zone/zone-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.zoneId").value("zone-1"))
                .andExpect(jsonPath("$.zoneName").value("Zone A"))
                .andExpect(jsonPath("$.postalCode").value("75001"))
                .andExpect(jsonPath("$.totalParcels").value(120L))
                .andExpect(jsonPath("$.totalWeight").value(600.25))
                .andExpect(jsonPath("$.averageWeight").value(5.00))
                .andExpect(jsonPath("$.deliveryPersonCount").value(8L));

        verify(statisticsService).getZoneStatistics("zone-1");
    }

    @Test
    @DisplayName("Should return 404 when zone not found")
    void testGetZoneStatistics_NotFound() throws Exception {
        // GIVEN
        when(statisticsService.getZoneStatistics("zone-999"))
                .thenThrow(new ResourceNotFoundException("Zone", "id", "zone-999"));

        // WHEN & THEN
        mockMvc.perform(get("/api/statistics/zone/zone-999"))
                .andExpect(status().isNotFound());

        verify(statisticsService).getZoneStatistics("zone-999");
    }

    @Test
    @DisplayName("Should return 200 with parcel status breakdown for zone")
    void testGetZoneStatistics_WithStatusBreakdown() throws Exception {
        // GIVEN
        when(statisticsService.getZoneStatistics("zone-1")).thenReturn(zoneStatisticsDTO);

        // WHEN & THEN
        mockMvc.perform(get("/api/statistics/zone/zone-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parcelsCreated").value(120L))
                .andExpect(jsonPath("$.parcelsCollected").value(100L))
                .andExpect(jsonPath("$.parcelsInStock").value(45L))
                .andExpect(jsonPath("$.parcelsInTransit").value(35L))
                .andExpect(jsonPath("$.parcelsDelivered").value(40L));

        verify(statisticsService).getZoneStatistics("zone-1");
    }

    @Test
    @DisplayName("Should return 200 with parcel priority breakdown for zone")
    void testGetZoneStatistics_WithPriorityBreakdown() throws Exception {
        // GIVEN
        when(statisticsService.getZoneStatistics("zone-1")).thenReturn(zoneStatisticsDTO);

        // WHEN & THEN
        mockMvc.perform(get("/api/statistics/zone/zone-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parcelsNormal").value(100L))
                .andExpect(jsonPath("$.parcelsUrgent").value(15L))
                .andExpect(jsonPath("$.parcelsExpress").value(5L))
                .andExpect(jsonPath("$.averageParcelsPerDeliveryPerson").value(15.0));

        verify(statisticsService).getZoneStatistics("zone-1");
    }

    // ==================== Tests pour GET /api/statistics/zone ====================

    @Test
    @DisplayName("Should get all zones statistics successfully and return 200")
    void testGetAllZoneStatistics_Success() throws Exception {
        // GIVEN
        ZoneStatisticsDTO zone2 = ZoneStatisticsDTO.builder()
                .zoneId("zone-2")
                .zoneName("Zone B")
                .postalCode("75002")
                .totalParcels(95L)
                .totalWeight(BigDecimal.valueOf(475.50))
                .averageWeight(BigDecimal.valueOf(5.00))
                .deliveryPersonCount(6L)
                .parcelsCreated(95L)
                .parcelsCollected(85L)
                .parcelsInStock(35L)
                .parcelsInTransit(30L)
                .parcelsDelivered(35L)
                .parcelsNormal(80L)
                .parcelsUrgent(10L)
                .parcelsExpress(5L)
                .averageParcelsPerDeliveryPerson(15.83)
                .build();

        List<ZoneStatisticsDTO> allStats = Arrays.asList(zoneStatisticsDTO, zone2);
        when(statisticsService.getAllZoneStatistics()).thenReturn(allStats);

        // WHEN & THEN
        mockMvc.perform(get("/api/statistics/zone"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].zoneId").value("zone-1"))
                .andExpect(jsonPath("$[0].zoneName").value("Zone A"))
                .andExpect(jsonPath("$[1].zoneId").value("zone-2"))
                .andExpect(jsonPath("$[1].zoneName").value("Zone B"));

        verify(statisticsService).getAllZoneStatistics();
    }

    @Test
    @DisplayName("Should return empty list when no zones exist")
    void testGetAllZoneStatistics_EmptyList() throws Exception {
        // GIVEN
        when(statisticsService.getAllZoneStatistics()).thenReturn(Arrays.asList());

        // WHEN & THEN
        mockMvc.perform(get("/api/statistics/zone"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(statisticsService).getAllZoneStatistics();
    }

    @Test
    @DisplayName("Should return 200 with multiple zones and their statistics")
    void testGetAllZoneStatistics_MultipleZones() throws Exception {
        // GIVEN
        ZoneStatisticsDTO zone2 = ZoneStatisticsDTO.builder()
                .zoneId("zone-2")
                .zoneName("Zone B")
                .postalCode("75002")
                .totalParcels(95L)
                .totalWeight(BigDecimal.valueOf(475.50))
                .deliveryPersonCount(6L)
                .parcelsDelivered(35L)
                .build();

        ZoneStatisticsDTO zone3 = ZoneStatisticsDTO.builder()
                .zoneId("zone-3")
                .zoneName("Zone C")
                .postalCode("75003")
                .totalParcels(85L)
                .totalWeight(BigDecimal.valueOf(425.25))
                .deliveryPersonCount(5L)
                .parcelsDelivered(40L)
                .build();

        List<ZoneStatisticsDTO> allStats = Arrays.asList(zoneStatisticsDTO, zone2, zone3);
        when(statisticsService.getAllZoneStatistics()).thenReturn(allStats);

        // WHEN & THEN
        mockMvc.perform(get("/api/statistics/zone"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].zoneId").value("zone-1"))
                .andExpect(jsonPath("$[1].zoneId").value("zone-2"))
                .andExpect(jsonPath("$[2].zoneId").value("zone-3"));

        verify(statisticsService).getAllZoneStatistics();
    }
}
