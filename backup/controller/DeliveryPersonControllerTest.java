package com.logismart.logismartv2.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logismart.logismartv2.dto.deliveryperson.DeliveryPersonCreateDTO;
import com.logismart.logismartv2.dto.deliveryperson.DeliveryPersonResponseDTO;
import com.logismart.logismartv2.dto.deliveryperson.DeliveryPersonStatsDTO;
import com.logismart.logismartv2.dto.deliveryperson.DeliveryPersonUpdateDTO;
import com.logismart.logismartv2.dto.parcel.ParcelResponseDTO;
import com.logismart.logismartv2.entity.ParcelPriority;
import com.logismart.logismartv2.exception.DuplicateResourceException;
import com.logismart.logismartv2.exception.ResourceNotFoundException;
import com.logismart.logismartv2.service.DeliveryPersonService;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitaires pour DeliveryPersonController
 * Utilise MockMvc pour tester les endpoints REST
 */
@WebMvcTest(DeliveryPersonController.class)
@DisplayName("DeliveryPersonController Unit Tests")
class DeliveryPersonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DeliveryPersonService deliveryPersonService;

    private DeliveryPersonResponseDTO responseDTO;
    private DeliveryPersonCreateDTO createDTO;
    private DeliveryPersonUpdateDTO updateDTO;
    private DeliveryPersonStatsDTO statsDTO;

    @BeforeEach
    void setUp() {
        responseDTO = new DeliveryPersonResponseDTO();
        responseDTO.setId("delivery-person-1");
        responseDTO.setFirstName("John");
        responseDTO.setLastName("Doe");
        responseDTO.setFullName("John Doe");
        responseDTO.setPhone("555-0001");
        responseDTO.setVehicle("Van");
        responseDTO.setAssignedZoneId("zone-1");
        responseDTO.setAssignedZoneName("Zone Paris");

        createDTO = new DeliveryPersonCreateDTO();
        createDTO.setFirstName("John");
        createDTO.setLastName("Doe");
        createDTO.setPhone("555-0001");
        createDTO.setVehicle("Van");
        createDTO.setAssignedZoneId("zone-1");

        updateDTO = new DeliveryPersonUpdateDTO();
        updateDTO.setId("delivery-person-1");
        updateDTO.setFirstName("John");
        updateDTO.setLastName("Smith");
        updateDTO.setPhone("555-0002");
        updateDTO.setVehicle("Truck");
        updateDTO.setAssignedZoneId("zone-2");

        statsDTO = new DeliveryPersonStatsDTO();
        statsDTO.setDeliveryPersonId("delivery-person-1");
        statsDTO.setDeliveryPersonName("John Doe");
        statsDTO.setTotalParcels(50L);
        statsDTO.setTotalWeight(150.5);
        statsDTO.setActiveParcels(5L);
        statsDTO.setDeliveredParcels(45L);
        statsDTO.setInTransitParcels(3L);
    }

    // ==================== Tests pour POST /api/delivery-persons ====================

    @Test
    @DisplayName("Should create delivery person successfully and return 201")
    void testCreateDeliveryPerson_Success() throws Exception {
        // GIVEN
        when(deliveryPersonService.create(any(DeliveryPersonCreateDTO.class))).thenReturn(responseDTO);

        // WHEN & THEN
        mockMvc.perform(post("/api/delivery-persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("delivery-person-1"))
                .andExpect(jsonPath("$.fullName").value("John Doe"))
                .andExpect(jsonPath("$.phone").value("555-0001"));

        verify(deliveryPersonService).create(any(DeliveryPersonCreateDTO.class));
    }

    @Test
    @DisplayName("Should return 409 when creating delivery person with duplicate phone")
    void testCreateDeliveryPerson_DuplicatePhone() throws Exception {
        // GIVEN
        when(deliveryPersonService.create(any(DeliveryPersonCreateDTO.class)))
                .thenThrow(new DuplicateResourceException("DeliveryPerson", "phone", createDTO.getPhone()));

        // WHEN & THEN
        mockMvc.perform(post("/api/delivery-persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isConflict());

        verify(deliveryPersonService).create(any(DeliveryPersonCreateDTO.class));
    }

    // ==================== Tests pour GET /api/delivery-persons/{id} ====================

    @Test
    @DisplayName("Should get delivery person by id successfully and return 200")
    void testGetDeliveryPersonById_Success() throws Exception {
        // GIVEN
        when(deliveryPersonService.findById("delivery-person-1")).thenReturn(responseDTO);

        // WHEN & THEN
        mockMvc.perform(get("/api/delivery-persons/delivery-person-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("delivery-person-1"))
                .andExpect(jsonPath("$.fullName").value("John Doe"))
                .andExpect(jsonPath("$.phone").value("555-0001"));

        verify(deliveryPersonService).findById("delivery-person-1");
    }

    @Test
    @DisplayName("Should return 404 when delivery person not found")
    void testGetDeliveryPersonById_NotFound() throws Exception {
        // GIVEN
        when(deliveryPersonService.findById("delivery-person-999"))
                .thenThrow(new ResourceNotFoundException("DeliveryPerson", "id", "delivery-person-999"));

        // WHEN & THEN
        mockMvc.perform(get("/api/delivery-persons/delivery-person-999"))
                .andExpect(status().isNotFound());

        verify(deliveryPersonService).findById("delivery-person-999");
    }

    // ==================== Tests pour GET /api/delivery-persons ====================

    @Test
    @DisplayName("Should get all delivery persons successfully and return 200")
    void testGetAllDeliveryPersons_Success() throws Exception {
        // GIVEN
        DeliveryPersonResponseDTO deliveryPerson2 = new DeliveryPersonResponseDTO();
        deliveryPerson2.setId("delivery-person-2");
        deliveryPerson2.setFirstName("Jane");
        deliveryPerson2.setLastName("Smith");
        deliveryPerson2.setFullName("Jane Smith");
        deliveryPerson2.setPhone("555-0002");

        List<DeliveryPersonResponseDTO> deliveryPersons = Arrays.asList(responseDTO, deliveryPerson2);
        when(deliveryPersonService.findAll()).thenReturn(deliveryPersons);

        // WHEN & THEN
        mockMvc.perform(get("/api/delivery-persons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value("delivery-person-1"))
                .andExpect(jsonPath("$[1].id").value("delivery-person-2"));

        verify(deliveryPersonService).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no delivery persons exist")
    void testGetAllDeliveryPersons_EmptyList() throws Exception {
        // GIVEN
        when(deliveryPersonService.findAll()).thenReturn(Arrays.asList());

        // WHEN & THEN
        mockMvc.perform(get("/api/delivery-persons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(deliveryPersonService).findAll();
    }

    // ==================== Tests pour PUT /api/delivery-persons/{id} ====================

    @Test
    @DisplayName("Should update delivery person successfully and return 200")
    void testUpdateDeliveryPerson_Success() throws Exception {
        // GIVEN
        responseDTO.setFirstName("John");
        responseDTO.setLastName("Smith");
        responseDTO.setFullName("John Smith");
        responseDTO.setPhone("555-0002");
        when(deliveryPersonService.update(any(DeliveryPersonUpdateDTO.class))).thenReturn(responseDTO);

        // WHEN & THEN
        mockMvc.perform(put("/api/delivery-persons/delivery-person-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("delivery-person-1"))
                .andExpect(jsonPath("$.fullName").value("John Smith"))
                .andExpect(jsonPath("$.phone").value("555-0002"));

        verify(deliveryPersonService).update(any(DeliveryPersonUpdateDTO.class));
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent delivery person")
    void testUpdateDeliveryPerson_NotFound() throws Exception {
        // GIVEN
        when(deliveryPersonService.update(any(DeliveryPersonUpdateDTO.class)))
                .thenThrow(new ResourceNotFoundException("DeliveryPerson", "id", "delivery-person-999"));

        // WHEN & THEN
        mockMvc.perform(put("/api/delivery-persons/delivery-person-999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());

        verify(deliveryPersonService).update(any(DeliveryPersonUpdateDTO.class));
    }

    // ==================== Tests pour DELETE /api/delivery-persons/{id} ====================

    @Test
    @DisplayName("Should delete delivery person successfully and return 204")
    void testDeleteDeliveryPerson_Success() throws Exception {
        // GIVEN
        doNothing().when(deliveryPersonService).delete("delivery-person-1");

        // WHEN & THEN
        mockMvc.perform(delete("/api/delivery-persons/delivery-person-1"))
                .andExpect(status().isNoContent());

        verify(deliveryPersonService).delete("delivery-person-1");
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent delivery person")
    void testDeleteDeliveryPerson_NotFound() throws Exception {
        // GIVEN
        doThrow(new ResourceNotFoundException("DeliveryPerson", "id", "delivery-person-999"))
                .when(deliveryPersonService).delete("delivery-person-999");

        // WHEN & THEN
        mockMvc.perform(delete("/api/delivery-persons/delivery-person-999"))
                .andExpect(status().isNotFound());

        verify(deliveryPersonService).delete("delivery-person-999");
    }

    // ==================== Tests pour GET /api/delivery-persons/zone/{zoneId} ====================

    @Test
    @DisplayName("Should get delivery persons by zone successfully")
    void testGetDeliveryPersonsByZone_Success() throws Exception {
        // GIVEN
        List<DeliveryPersonResponseDTO> deliveryPersons = Arrays.asList(responseDTO);
        when(deliveryPersonService.findByZone("zone-1")).thenReturn(deliveryPersons);

        // WHEN & THEN
        mockMvc.perform(get("/api/delivery-persons/zone/zone-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].assignedZoneId").value("zone-1"));

        verify(deliveryPersonService).findByZone("zone-1");
    }

    @Test
    @DisplayName("Should return empty list when no delivery persons in zone")
    void testGetDeliveryPersonsByZone_EmptyList() throws Exception {
        // GIVEN
        when(deliveryPersonService.findByZone("zone-999")).thenReturn(Arrays.asList());

        // WHEN & THEN
        mockMvc.perform(get("/api/delivery-persons/zone/zone-999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(deliveryPersonService).findByZone("zone-999");
    }

    // ==================== Tests pour GET /api/delivery-persons/unassigned ====================

    @Test
    @DisplayName("Should get unassigned delivery persons successfully")
    void testGetUnassignedDeliveryPersons_Success() throws Exception {
        // GIVEN
        DeliveryPersonResponseDTO unassignedPerson = new DeliveryPersonResponseDTO();
        unassignedPerson.setId("delivery-person-3");
        unassignedPerson.setFirstName("Unassigned");
        unassignedPerson.setLastName("Person");
        unassignedPerson.setFullName("Unassigned Person");
        unassignedPerson.setPhone("555-0003");
        unassignedPerson.setAssignedZoneId(null);

        List<DeliveryPersonResponseDTO> unassigned = Arrays.asList(unassignedPerson);
        when(deliveryPersonService.findUnassigned()).thenReturn(unassigned);

        // WHEN & THEN
        mockMvc.perform(get("/api/delivery-persons/unassigned"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].fullName").value("Unassigned Person"));

        verify(deliveryPersonService).findUnassigned();
    }

    @Test
    @DisplayName("Should return empty list when all delivery persons are assigned")
    void testGetUnassignedDeliveryPersons_EmptyList() throws Exception {
        // GIVEN
        when(deliveryPersonService.findUnassigned()).thenReturn(Arrays.asList());

        // WHEN & THEN
        mockMvc.perform(get("/api/delivery-persons/unassigned"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(deliveryPersonService).findUnassigned();
    }

    // ==================== Tests pour GET /api/delivery-persons/available ====================

    @Test
    @DisplayName("Should get available delivery persons successfully")
    void testGetAvailableDeliveryPersons_Success() throws Exception {
        // GIVEN
        List<DeliveryPersonResponseDTO> available = Arrays.asList(responseDTO);
        when(deliveryPersonService.findAvailable()).thenReturn(available);

        // WHEN & THEN
        mockMvc.perform(get("/api/delivery-persons/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].fullName").value("John Doe"));

        verify(deliveryPersonService).findAvailable();
    }

    @Test
    @DisplayName("Should return empty list when no available delivery persons")
    void testGetAvailableDeliveryPersons_EmptyList() throws Exception {
        // GIVEN
        when(deliveryPersonService.findAvailable()).thenReturn(Arrays.asList());

        // WHEN & THEN
        mockMvc.perform(get("/api/delivery-persons/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(deliveryPersonService).findAvailable();
    }

    // ==================== Tests pour GET /api/delivery-persons/available/zone/{zoneId} ====================

    @Test
    @DisplayName("Should get available delivery persons in zone successfully")
    void testGetAvailableDeliveryPersonsInZone_Success() throws Exception {
        // GIVEN
        List<DeliveryPersonResponseDTO> available = Arrays.asList(responseDTO);
        when(deliveryPersonService.findAvailableInZone("zone-1")).thenReturn(available);

        // WHEN & THEN
        mockMvc.perform(get("/api/delivery-persons/available/zone/zone-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].assignedZoneId").value("zone-1"));

        verify(deliveryPersonService).findAvailableInZone("zone-1");
    }

    @Test
    @DisplayName("Should return empty list when no available delivery persons in zone")
    void testGetAvailableDeliveryPersonsInZone_EmptyList() throws Exception {
        // GIVEN
        when(deliveryPersonService.findAvailableInZone("zone-999")).thenReturn(Arrays.asList());

        // WHEN & THEN
        mockMvc.perform(get("/api/delivery-persons/available/zone/zone-999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(deliveryPersonService).findAvailableInZone("zone-999");
    }

    // ==================== Tests pour GET /api/delivery-persons/{id}/parcels/active/count ====================

    @Test
    @DisplayName("Should count active parcels successfully and return 200")
    void testCountActiveParcels_Success() throws Exception {
        // GIVEN
        when(deliveryPersonService.countActiveParcels("delivery-person-1")).thenReturn(5L);

        // WHEN & THEN
        mockMvc.perform(get("/api/delivery-persons/delivery-person-1/parcels/active/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));

        verify(deliveryPersonService).countActiveParcels("delivery-person-1");
    }

    @Test
    @DisplayName("Should return 404 when counting active parcels for non-existent delivery person")
    void testCountActiveParcels_NotFound() throws Exception {
        // GIVEN
        when(deliveryPersonService.countActiveParcels("delivery-person-999"))
                .thenThrow(new ResourceNotFoundException("DeliveryPerson", "id", "delivery-person-999"));

        // WHEN & THEN
        mockMvc.perform(get("/api/delivery-persons/delivery-person-999/parcels/active/count"))
                .andExpect(status().isNotFound());

        verify(deliveryPersonService).countActiveParcels("delivery-person-999");
    }

    @Test
    @DisplayName("Should return zero when delivery person has no active parcels")
    void testCountActiveParcels_Zero() throws Exception {
        // GIVEN
        when(deliveryPersonService.countActiveParcels("delivery-person-2")).thenReturn(0L);

        // WHEN & THEN
        mockMvc.perform(get("/api/delivery-persons/delivery-person-2/parcels/active/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));

        verify(deliveryPersonService).countActiveParcels("delivery-person-2");
    }

    // ==================== Tests pour GET /api/delivery-persons/{id}/parcels/delivered/count ====================

    @Test
    @DisplayName("Should count delivered parcels successfully and return 200")
    void testCountDeliveredParcels_Success() throws Exception {
        // GIVEN
        when(deliveryPersonService.countDeliveredParcels("delivery-person-1")).thenReturn(45L);

        // WHEN & THEN
        mockMvc.perform(get("/api/delivery-persons/delivery-person-1/parcels/delivered/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("45"));

        verify(deliveryPersonService).countDeliveredParcels("delivery-person-1");
    }

    @Test
    @DisplayName("Should return 404 when counting delivered parcels for non-existent delivery person")
    void testCountDeliveredParcels_NotFound() throws Exception {
        // GIVEN
        when(deliveryPersonService.countDeliveredParcels("delivery-person-999"))
                .thenThrow(new ResourceNotFoundException("DeliveryPerson", "id", "delivery-person-999"));

        // WHEN & THEN
        mockMvc.perform(get("/api/delivery-persons/delivery-person-999/parcels/delivered/count"))
                .andExpect(status().isNotFound());

        verify(deliveryPersonService).countDeliveredParcels("delivery-person-999");
    }

    // ==================== Tests pour GET /api/delivery-persons/{id}/parcels/urgent ====================

    @Test
    @DisplayName("Should get urgent parcels successfully and return 200")
    void testGetUrgentParcels_Success() throws Exception {
        // GIVEN
        ParcelResponseDTO urgentParcel = new ParcelResponseDTO();
        urgentParcel.setId("parcel-1");
        urgentParcel.setPriority(ParcelPriority.URGENT);

        List<ParcelResponseDTO> urgentParcels = Arrays.asList(urgentParcel);
        when(deliveryPersonService.findUrgentParcels("delivery-person-1")).thenReturn(urgentParcels);

        // WHEN & THEN
        mockMvc.perform(get("/api/delivery-persons/delivery-person-1/parcels/urgent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].priority").value("URGENT"));

        verify(deliveryPersonService).findUrgentParcels("delivery-person-1");
    }

    @Test
    @DisplayName("Should return empty list when no urgent parcels for delivery person")
    void testGetUrgentParcels_EmptyList() throws Exception {
        // GIVEN
        when(deliveryPersonService.findUrgentParcels("delivery-person-1")).thenReturn(Arrays.asList());

        // WHEN & THEN
        mockMvc.perform(get("/api/delivery-persons/delivery-person-1/parcels/urgent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(deliveryPersonService).findUrgentParcels("delivery-person-1");
    }

    @Test
    @DisplayName("Should return 404 when getting urgent parcels for non-existent delivery person")
    void testGetUrgentParcels_NotFound() throws Exception {
        // GIVEN
        when(deliveryPersonService.findUrgentParcels("delivery-person-999"))
                .thenThrow(new ResourceNotFoundException("DeliveryPerson", "id", "delivery-person-999"));

        // WHEN & THEN
        mockMvc.perform(get("/api/delivery-persons/delivery-person-999/parcels/urgent"))
                .andExpect(status().isNotFound());

        verify(deliveryPersonService).findUrgentParcels("delivery-person-999");
    }

    // ==================== Tests pour GET /api/delivery-persons/{id}/stats ====================

    @Test
    @DisplayName("Should get delivery person statistics successfully and return 200")
    void testGetStats_Success() throws Exception {
        // GIVEN
        when(deliveryPersonService.getStats("delivery-person-1")).thenReturn(statsDTO);

        // WHEN & THEN
        mockMvc.perform(get("/api/delivery-persons/delivery-person-1/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deliveryPersonId").value("delivery-person-1"))
                .andExpect(jsonPath("$.deliveryPersonName").value("John Doe"))
                .andExpect(jsonPath("$.totalParcels").value(50))
                .andExpect(jsonPath("$.totalWeight").value(150.5))
                .andExpect(jsonPath("$.activeParcels").value(5))
                .andExpect(jsonPath("$.deliveredParcels").value(45))
                .andExpect(jsonPath("$.inTransitParcels").value(3));

        verify(deliveryPersonService).getStats("delivery-person-1");
    }

    @Test
    @DisplayName("Should return 404 when getting statistics for non-existent delivery person")
    void testGetStats_NotFound() throws Exception {
        // GIVEN
        when(deliveryPersonService.getStats("delivery-person-999"))
                .thenThrow(new ResourceNotFoundException("DeliveryPerson", "id", "delivery-person-999"));

        // WHEN & THEN
        mockMvc.perform(get("/api/delivery-persons/delivery-person-999/stats"))
                .andExpect(status().isNotFound());

        verify(deliveryPersonService).getStats("delivery-person-999");
    }

    @Test
    @DisplayName("Should get statistics with zero parcels")
    void testGetStats_ZeroParcels() throws Exception {
        // GIVEN
        DeliveryPersonStatsDTO emptyStats = new DeliveryPersonStatsDTO();
        emptyStats.setDeliveryPersonId("delivery-person-2");
        emptyStats.setDeliveryPersonName("Jane Smith");
        emptyStats.setTotalParcels(0L);
        emptyStats.setTotalWeight(0.0);
        emptyStats.setActiveParcels(0L);
        emptyStats.setDeliveredParcels(0L);
        emptyStats.setInTransitParcels(0L);

        when(deliveryPersonService.getStats("delivery-person-2")).thenReturn(emptyStats);

        // WHEN & THEN
        mockMvc.perform(get("/api/delivery-persons/delivery-person-2/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalParcels").value(0))
                .andExpect(jsonPath("$.activeParcels").value(0));

        verify(deliveryPersonService).getStats("delivery-person-2");
    }
}
