package com.logismart.logismartv2.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logismart.logismartv2.dto.zone.ZoneCreateDTO;
import com.logismart.logismartv2.dto.zone.ZoneResponseDTO;
import com.logismart.logismartv2.dto.zone.ZoneStatsDTO;
import com.logismart.logismartv2.dto.zone.ZoneUpdateDTO;
import com.logismart.logismartv2.exception.DuplicateResourceException;
import com.logismart.logismartv2.exception.ResourceNotFoundException;
import com.logismart.logismartv2.service.ZoneService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitaires pour ZoneController
 * Utilise MockMvc pour tester les endpoints REST
 */
@WebMvcTest(ZoneController.class)
@DisplayName("ZoneController Unit Tests")
class ZoneControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ZoneService zoneService;

    private ZoneResponseDTO responseDTO;
    private ZoneCreateDTO createDTO;
    private ZoneUpdateDTO updateDTO;
    private ZoneStatsDTO statsDTO;

    @BeforeEach
    void setUp() {
        responseDTO = new ZoneResponseDTO();
        responseDTO.setId("zone-1");
        responseDTO.setName("Zone Paris");
        responseDTO.setPostalCode("75000");

        createDTO = new ZoneCreateDTO();
        createDTO.setName("Zone Paris");
        createDTO.setPostalCode("75000");

        updateDTO = new ZoneUpdateDTO();
        updateDTO.setId("zone-1");
        updateDTO.setName("Zone Paris Updated");
        updateDTO.setPostalCode("75001");

        statsDTO = new ZoneStatsDTO();
        statsDTO.setZoneId("zone-1");
        statsDTO.setZoneName("Zone Paris");
        statsDTO.setTotalParcels(100L);
        statsDTO.setTotalWeight(150.5);
        statsDTO.setInTransitParcels(30L);
        statsDTO.setDeliveredParcels(60L);
        statsDTO.setUnassignedParcels(10L);
    }

    // ==================== Tests pour POST /api/zones ====================

    @Test
    @DisplayName("Should create zone successfully and return 201")
    void testCreateZone_Success() throws Exception {
        // GIVEN
        when(zoneService.create(any(ZoneCreateDTO.class))).thenReturn(responseDTO);

        // WHEN & THEN
        mockMvc.perform(post("/api/zones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("zone-1"))
                .andExpect(jsonPath("$.name").value("Zone Paris"))
                .andExpect(jsonPath("$.postalCode").value("75000"));

        verify(zoneService).create(any(ZoneCreateDTO.class));
    }

    @Test
    @DisplayName("Should return 409 when creating zone with duplicate name")
    void testCreateZone_DuplicateName() throws Exception {
        // GIVEN
        when(zoneService.create(any(ZoneCreateDTO.class)))
                .thenThrow(new DuplicateResourceException("Zone", "name", createDTO.getName()));

        // WHEN & THEN
        mockMvc.perform(post("/api/zones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isConflict());

        verify(zoneService).create(any(ZoneCreateDTO.class));
    }

    @Test
    @DisplayName("Should return 409 when creating zone with duplicate postal code")
    void testCreateZone_DuplicatePostalCode() throws Exception {
        // GIVEN
        when(zoneService.create(any(ZoneCreateDTO.class)))
                .thenThrow(new DuplicateResourceException("Zone", "postalCode", createDTO.getPostalCode()));

        // WHEN & THEN
        mockMvc.perform(post("/api/zones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isConflict());

        verify(zoneService).create(any(ZoneCreateDTO.class));
    }

    // ==================== Tests pour GET /api/zones/{id} ====================

    @Test
    @DisplayName("Should get zone by id successfully and return 200")
    void testGetZoneById_Success() throws Exception {
        // GIVEN
        when(zoneService.findById("zone-1")).thenReturn(responseDTO);

        // WHEN & THEN
        mockMvc.perform(get("/api/zones/zone-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("zone-1"))
                .andExpect(jsonPath("$.name").value("Zone Paris"))
                .andExpect(jsonPath("$.postalCode").value("75000"));

        verify(zoneService).findById("zone-1");
    }

    @Test
    @DisplayName("Should return 404 when zone not found by id")
    void testGetZoneById_NotFound() throws Exception {
        // GIVEN
        when(zoneService.findById("zone-999"))
                .thenThrow(new ResourceNotFoundException("Zone", "id", "zone-999"));

        // WHEN & THEN
        mockMvc.perform(get("/api/zones/zone-999"))
                .andExpect(status().isNotFound());

        verify(zoneService).findById("zone-999");
    }

    // ==================== Tests pour GET /api/zones ====================

    @Test
    @DisplayName("Should get all zones successfully and return 200")
    void testGetAllZones_Success() throws Exception {
        // GIVEN
        ZoneResponseDTO zone2 = new ZoneResponseDTO();
        zone2.setId("zone-2");
        zone2.setName("Zone Lyon");
        zone2.setPostalCode("69000");

        List<ZoneResponseDTO> zones = Arrays.asList(responseDTO, zone2);
        when(zoneService.findAll()).thenReturn(zones);

        // WHEN & THEN
        mockMvc.perform(get("/api/zones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value("zone-1"))
                .andExpect(jsonPath("$[1].id").value("zone-2"));

        verify(zoneService).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no zones exist")
    void testGetAllZones_EmptyList() throws Exception {
        // GIVEN
        when(zoneService.findAll()).thenReturn(Arrays.asList());

        // WHEN & THEN
        mockMvc.perform(get("/api/zones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(zoneService).findAll();
    }

    // ==================== Tests pour PUT /api/zones/{id} ====================

    @Test
    @DisplayName("Should update zone successfully and return 200")
    void testUpdateZone_Success() throws Exception {
        // GIVEN
        responseDTO.setName("Zone Paris Updated");
        responseDTO.setPostalCode("75001");
        when(zoneService.update(any(ZoneUpdateDTO.class))).thenReturn(responseDTO);

        // WHEN & THEN
        mockMvc.perform(put("/api/zones/zone-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("zone-1"))
                .andExpect(jsonPath("$.name").value("Zone Paris Updated"))
                .andExpect(jsonPath("$.postalCode").value("75001"));

        verify(zoneService).update(any(ZoneUpdateDTO.class));
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent zone")
    void testUpdateZone_NotFound() throws Exception {
        // GIVEN
        when(zoneService.update(any(ZoneUpdateDTO.class)))
                .thenThrow(new ResourceNotFoundException("Zone", "id", "zone-999"));

        // WHEN & THEN
        mockMvc.perform(put("/api/zones/zone-999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());

        verify(zoneService).update(any(ZoneUpdateDTO.class));
    }

    @Test
    @DisplayName("Should return 409 when updating zone with duplicate name")
    void testUpdateZone_DuplicateName() throws Exception {
        // GIVEN
        when(zoneService.update(any(ZoneUpdateDTO.class)))
                .thenThrow(new DuplicateResourceException("Zone", "name", "Zone Paris Updated"));

        // WHEN & THEN
        mockMvc.perform(put("/api/zones/zone-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isConflict());

        verify(zoneService).update(any(ZoneUpdateDTO.class));
    }

    // ==================== Tests pour DELETE /api/zones/{id} ====================

    @Test
    @DisplayName("Should delete zone successfully and return 204")
    void testDeleteZone_Success() throws Exception {
        // GIVEN
        doNothing().when(zoneService).delete("zone-1");

        // WHEN & THEN
        mockMvc.perform(delete("/api/zones/zone-1"))
                .andExpect(status().isNoContent());

        verify(zoneService).delete("zone-1");
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent zone")
    void testDeleteZone_NotFound() throws Exception {
        // GIVEN
        doThrow(new ResourceNotFoundException("Zone", "id", "zone-999"))
                .when(zoneService).delete("zone-999");

        // WHEN & THEN
        mockMvc.perform(delete("/api/zones/zone-999"))
                .andExpect(status().isNotFound());

        verify(zoneService).delete("zone-999");
    }

    // ==================== Tests pour GET /api/zones/search ====================

    @Test
    @DisplayName("Should search zones by name successfully")
    void testSearchZones_Success() throws Exception {
        // GIVEN
        List<ZoneResponseDTO> zones = Arrays.asList(responseDTO);
        when(zoneService.searchByName("Paris")).thenReturn(zones);

        // WHEN & THEN
        mockMvc.perform(get("/api/zones/search")
                        .param("keyword", "Paris"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Zone Paris"));

        verify(zoneService).searchByName("Paris");
    }

    @Test
    @DisplayName("Should return empty list when no zones match search keyword")
    void testSearchZones_NoResults() throws Exception {
        // GIVEN
        when(zoneService.searchByName("NonExistent")).thenReturn(Arrays.asList());

        // WHEN & THEN
        mockMvc.perform(get("/api/zones/search")
                        .param("keyword", "NonExistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(zoneService).searchByName("NonExistent");
    }

    // ==================== Tests pour GET /api/zones/by-name/{name} ====================

    @Test
    @DisplayName("Should get zone by name successfully")
    void testGetZoneByName_Success() throws Exception {
        // GIVEN
        when(zoneService.findByName("Zone Paris")).thenReturn(responseDTO);

        // WHEN & THEN
        mockMvc.perform(get("/api/zones/by-name/Zone Paris"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("zone-1"))
                .andExpect(jsonPath("$.name").value("Zone Paris"));

        verify(zoneService).findByName("Zone Paris");
    }

    @Test
    @DisplayName("Should return 404 when zone not found by name")
    void testGetZoneByName_NotFound() throws Exception {
        // GIVEN
        when(zoneService.findByName("NonExistentZone"))
                .thenThrow(new ResourceNotFoundException("Zone", "name", "NonExistentZone"));

        // WHEN & THEN
        mockMvc.perform(get("/api/zones/by-name/NonExistentZone"))
                .andExpect(status().isNotFound());

        verify(zoneService).findByName("NonExistentZone");
    }

    // ==================== Tests pour GET /api/zones/by-postal-code/{postalCode} ====================

    @Test
    @DisplayName("Should get zone by postal code successfully")
    void testGetZoneByPostalCode_Success() throws Exception {
        // GIVEN
        when(zoneService.findByPostalCode("75000")).thenReturn(responseDTO);

        // WHEN & THEN
        mockMvc.perform(get("/api/zones/by-postal-code/75000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("zone-1"))
                .andExpect(jsonPath("$.postalCode").value("75000"));

        verify(zoneService).findByPostalCode("75000");
    }

    @Test
    @DisplayName("Should return 404 when zone not found by postal code")
    void testGetZoneByPostalCode_NotFound() throws Exception {
        // GIVEN
        when(zoneService.findByPostalCode("99999"))
                .thenThrow(new ResourceNotFoundException("Zone", "postalCode", "99999"));

        // WHEN & THEN
        mockMvc.perform(get("/api/zones/by-postal-code/99999"))
                .andExpect(status().isNotFound());

        verify(zoneService).findByPostalCode("99999");
    }

    // ==================== Tests pour GET /api/zones/{id}/delivery-persons/count ====================

    @Test
    @DisplayName("Should count delivery persons in zone successfully")
    void testCountDeliveryPersons_Success() throws Exception {
        // GIVEN
        when(zoneService.countDeliveryPersons("zone-1")).thenReturn(5L);

        // WHEN & THEN
        mockMvc.perform(get("/api/zones/zone-1/delivery-persons/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));

        verify(zoneService).countDeliveryPersons("zone-1");
    }

    @Test
    @DisplayName("Should return 404 when counting delivery persons in non-existent zone")
    void testCountDeliveryPersons_NotFound() throws Exception {
        // GIVEN
        when(zoneService.countDeliveryPersons("zone-999"))
                .thenThrow(new ResourceNotFoundException("Zone", "id", "zone-999"));

        // WHEN & THEN
        mockMvc.perform(get("/api/zones/zone-999/delivery-persons/count"))
                .andExpect(status().isNotFound());

        verify(zoneService).countDeliveryPersons("zone-999");
    }

    @Test
    @DisplayName("Should return 0 when zone has no delivery persons")
    void testCountDeliveryPersons_Zero() throws Exception {
        // GIVEN
        when(zoneService.countDeliveryPersons("zone-1")).thenReturn(0L);

        // WHEN & THEN
        mockMvc.perform(get("/api/zones/zone-1/delivery-persons/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));

        verify(zoneService).countDeliveryPersons("zone-1");
    }

    // ==================== Tests pour GET /api/zones/{id}/parcels/count ====================

    @Test
    @DisplayName("Should count parcels in zone successfully")
    void testCountParcels_Success() throws Exception {
        // GIVEN
        when(zoneService.countParcels("zone-1")).thenReturn(100L);

        // WHEN & THEN
        mockMvc.perform(get("/api/zones/zone-1/parcels/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("100"));

        verify(zoneService).countParcels("zone-1");
    }

    @Test
    @DisplayName("Should return 404 when counting parcels in non-existent zone")
    void testCountParcels_NotFound() throws Exception {
        // GIVEN
        when(zoneService.countParcels("zone-999"))
                .thenThrow(new ResourceNotFoundException("Zone", "id", "zone-999"));

        // WHEN & THEN
        mockMvc.perform(get("/api/zones/zone-999/parcels/count"))
                .andExpect(status().isNotFound());

        verify(zoneService).countParcels("zone-999");
    }

    @Test
    @DisplayName("Should return 0 when zone has no parcels")
    void testCountParcels_Zero() throws Exception {
        // GIVEN
        when(zoneService.countParcels("zone-1")).thenReturn(0L);

        // WHEN & THEN
        mockMvc.perform(get("/api/zones/zone-1/parcels/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));

        verify(zoneService).countParcels("zone-1");
    }

    // ==================== Tests pour GET /api/zones/{id}/stats ====================

    @Test
    @DisplayName("Should get zone statistics successfully")
    void testGetStats_Success() throws Exception {
        // GIVEN
        when(zoneService.getStats("zone-1")).thenReturn(statsDTO);

        // WHEN & THEN
        mockMvc.perform(get("/api/zones/zone-1/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.zoneId").value("zone-1"))
                .andExpect(jsonPath("$.zoneName").value("Zone Paris"))
                .andExpect(jsonPath("$.totalParcels").value(100))
                .andExpect(jsonPath("$.totalWeight").value(150.5))
                .andExpect(jsonPath("$.inTransitParcels").value(30))
                .andExpect(jsonPath("$.deliveredParcels").value(60))
                .andExpect(jsonPath("$.unassignedParcels").value(10));

        verify(zoneService).getStats("zone-1");
    }

    @Test
    @DisplayName("Should return 404 when getting stats for non-existent zone")
    void testGetStats_NotFound() throws Exception {
        // GIVEN
        when(zoneService.getStats("zone-999"))
                .thenThrow(new ResourceNotFoundException("Zone", "id", "zone-999"));

        // WHEN & THEN
        mockMvc.perform(get("/api/zones/zone-999/stats"))
                .andExpect(status().isNotFound());

        verify(zoneService).getStats("zone-999");
    }

    @Test
    @DisplayName("Should get zone statistics with zero parcels")
    void testGetStats_ZeroParcels() throws Exception {
        // GIVEN
        ZoneStatsDTO emptyStats = new ZoneStatsDTO();
        emptyStats.setZoneId("zone-1");
        emptyStats.setZoneName("Zone Paris");
        emptyStats.setTotalParcels(0L);
        emptyStats.setTotalWeight(0.0);
        emptyStats.setInTransitParcels(0L);
        emptyStats.setDeliveredParcels(0L);
        emptyStats.setUnassignedParcels(0L);

        when(zoneService.getStats("zone-1")).thenReturn(emptyStats);

        // WHEN & THEN
        mockMvc.perform(get("/api/zones/zone-1/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalParcels").value(0))
                .andExpect(jsonPath("$.totalWeight").value(0.0));

        verify(zoneService).getStats("zone-1");
    }
}
