package com.logismart.logismartv2.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logismart.logismartv2.dto.deliveryhistory.DeliveryHistoryResponseDTO;
import com.logismart.logismartv2.dto.parcel.ParcelCreateDTO;
import com.logismart.logismartv2.dto.parcel.ParcelProductItemDTO;
import com.logismart.logismartv2.dto.parcel.ParcelResponseDTO;
import com.logismart.logismartv2.dto.parcel.ParcelUpdateDTO;
import com.logismart.logismartv2.entity.ParcelPriority;
import com.logismart.logismartv2.entity.ParcelStatus;
import com.logismart.logismartv2.exception.ResourceNotFoundException;
import com.logismart.logismartv2.service.ParcelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitaires pour ParcelController
 * Utilise MockMvc pour tester les endpoints REST
 *
 * Cette classe teste tous les endpoints du ParcelController:
 * - CRUD operations (POST, GET, PUT, DELETE)
 * - Search et filtrage avec pagination
 * - Groupement par status, priority, zone, city
 * - Historique de livraison
 * - Comptage et statistiques
 */
@WebMvcTest(ParcelController.class)
@DisplayName("ParcelController Unit Tests")
class ParcelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ParcelService parcelService;

    private ParcelResponseDTO responseDTO;
    private ParcelCreateDTO createDTO;
    private ParcelUpdateDTO updateDTO;
    private DeliveryHistoryResponseDTO historyDTO;

    @BeforeEach
    void setUp() {
        // Initialiser le DTO de réponse pour Parcel
        responseDTO = new ParcelResponseDTO();
        responseDTO.setId("parcel-1");
        responseDTO.setDescription("Test parcel shipment");
        responseDTO.setWeight(java.math.BigDecimal.valueOf(2.5));
        responseDTO.setDestinationCity("Paris");
        responseDTO.setStatus(ParcelStatus.CREATED);
        responseDTO.setPriority(ParcelPriority.NORMAL);
        responseDTO.setSenderClientId("sender-1");
        responseDTO.setRecipientId("recipient-1");
        responseDTO.setDeliveryPersonId("delivery-1");
        responseDTO.setZoneId("zone-1");
        responseDTO.setCreatedAt(LocalDateTime.now());

        // Initialiser le DTO de création
        createDTO = new ParcelCreateDTO();
        createDTO.setDescription("New test parcel");
        createDTO.setWeight(java.math.BigDecimal.valueOf(3.0));
        createDTO.setDestinationCity("Lyon");
        createDTO.setPriority(ParcelPriority.NORMAL);
        createDTO.setSenderClientId("sender-1");
        createDTO.setRecipientId("recipient-1");

        ParcelProductItemDTO product1 = new ParcelProductItemDTO("product-1", 1, java.math.BigDecimal.valueOf(100.0));
        ParcelProductItemDTO product2 = new ParcelProductItemDTO("product-2", 2, java.math.BigDecimal.valueOf(50.0));
        createDTO.setProducts(Arrays.asList(product1, product2));

        // Initialiser le DTO de mise à jour
        updateDTO = new ParcelUpdateDTO();
        updateDTO.setId("parcel-1");
        updateDTO.setDescription("Updated parcel description");
        updateDTO.setWeight(java.math.BigDecimal.valueOf(2.8));
        updateDTO.setDestinationCity("Marseille");
        updateDTO.setStatus(ParcelStatus.IN_TRANSIT);

        // Initialiser le DTO d'historique
        historyDTO = new DeliveryHistoryResponseDTO();
        historyDTO.setId("history-1");
        historyDTO.setParcelId("parcel-1");
        historyDTO.setStatus(ParcelStatus.CREATED);
        historyDTO.setComment("Parcel created");
        historyDTO.setChangedAt(LocalDateTime.now());
    }

    // ==================== Tests pour POST /api/parcels ====================

    @Test
    @DisplayName("Should create parcel successfully and return 201")
    void testCreateParcel_Success() throws Exception {
        // GIVEN
        when(parcelService.create(any(ParcelCreateDTO.class))).thenReturn(responseDTO);

        // WHEN & THEN
        mockMvc.perform(post("/api/parcels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("parcel-1"))
                .andExpect(jsonPath("$.description").value("Test parcel shipment"))
                .andExpect(jsonPath("$.destinationCity").value("Paris"))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.priority").value("NORMAL"));

        verify(parcelService).create(any(ParcelCreateDTO.class));
    }

    @Test
    @DisplayName("Should return 400 when creating parcel with invalid data")
    void testCreateParcel_InvalidData() throws Exception {
        // GIVEN - Missing required fields
        ParcelCreateDTO invalidDTO = new ParcelCreateDTO();
        invalidDTO.setDescription("Incomplete parcel");

        // WHEN & THEN
        mockMvc.perform(post("/api/parcels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    // ==================== Tests pour GET /api/parcels/{id} ====================

    @Test
    @DisplayName("Should get parcel by id successfully and return 200")
    void testGetParcelById_Success() throws Exception {
        // GIVEN
        when(parcelService.findById("parcel-1")).thenReturn(responseDTO);

        // WHEN & THEN
        mockMvc.perform(get("/api/parcels/parcel-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("parcel-1"))
                .andExpect(jsonPath("$.description").value("Test parcel shipment"))
                .andExpect(jsonPath("$.weight").value(2.5));

        verify(parcelService).findById("parcel-1");
    }

    @Test
    @DisplayName("Should return 404 when parcel not found")
    void testGetParcelById_NotFound() throws Exception {
        // GIVEN
        when(parcelService.findById("parcel-999"))
                .thenThrow(new ResourceNotFoundException("Parcel", "id", "parcel-999"));

        // WHEN & THEN
        mockMvc.perform(get("/api/parcels/parcel-999"))
                .andExpect(status().isNotFound());

        verify(parcelService).findById("parcel-999");
    }

    // ==================== Tests pour GET /api/parcels (pagination) ====================

    @Test
    @DisplayName("Should get all parcels with pagination and return 200")
    void testGetAllParcels_Success() throws Exception {
        // GIVEN
        ParcelResponseDTO parcel2 = new ParcelResponseDTO();
        parcel2.setId("parcel-2");
        parcel2.setDescription("Another parcel");
        parcel2.setDestinationCity("Toulouse");
        parcel2.setStatus(ParcelStatus.IN_TRANSIT);

        Page<ParcelResponseDTO> page = new PageImpl<>(
                Arrays.asList(responseDTO, parcel2),
                PageRequest.of(0, 20),
                2
        );
        when(parcelService.findAll(any())).thenReturn(page);

        // WHEN & THEN
        mockMvc.perform(get("/api/parcels")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id").value("parcel-1"))
                .andExpect(jsonPath("$.content[1].id").value("parcel-2"))
                .andExpect(jsonPath("$.totalElements").value(2));

        verify(parcelService).findAll(any());
    }

    @Test
    @DisplayName("Should return empty page when no parcels exist")
    void testGetAllParcels_EmptyList() throws Exception {
        // GIVEN
        Page<ParcelResponseDTO> emptyPage = new PageImpl<>(
                Arrays.asList(),
                PageRequest.of(0, 20),
                0
        );
        when(parcelService.findAll(any())).thenReturn(emptyPage);

        // WHEN & THEN
        mockMvc.perform(get("/api/parcels"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(parcelService).findAll(any());
    }

    // ==================== Tests pour PUT /api/parcels/{id} ====================

    @Test
    @DisplayName("Should update parcel successfully and return 200")
    void testUpdateParcel_Success() throws Exception {
        // GIVEN
        responseDTO.setDescription("Updated parcel description");
        responseDTO.setDestinationCity("Marseille");
        when(parcelService.update(any(ParcelUpdateDTO.class))).thenReturn(responseDTO);

        // WHEN & THEN
        mockMvc.perform(put("/api/parcels/parcel-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("parcel-1"))
                .andExpect(jsonPath("$.description").value("Updated parcel description"))
                .andExpect(jsonPath("$.destinationCity").value("Marseille"));

        verify(parcelService).update(any(ParcelUpdateDTO.class));
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent parcel")
    void testUpdateParcel_NotFound() throws Exception {
        // GIVEN
        when(parcelService.update(any(ParcelUpdateDTO.class)))
                .thenThrow(new ResourceNotFoundException("Parcel", "id", "parcel-999"));

        // WHEN & THEN
        mockMvc.perform(put("/api/parcels/parcel-999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());

        verify(parcelService).update(any(ParcelUpdateDTO.class));
    }

    // ==================== Tests pour DELETE /api/parcels/{id} ====================

    @Test
    @DisplayName("Should delete parcel successfully and return 204")
    void testDeleteParcel_Success() throws Exception {
        // GIVEN
        doNothing().when(parcelService).delete("parcel-1");

        // WHEN & THEN
        mockMvc.perform(delete("/api/parcels/parcel-1"))
                .andExpect(status().isNoContent());

        verify(parcelService).delete("parcel-1");
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent parcel")
    void testDeleteParcel_NotFound() throws Exception {
        // GIVEN
        doThrow(new ResourceNotFoundException("Parcel", "id", "parcel-999"))
                .when(parcelService).delete("parcel-999");

        // WHEN & THEN
        mockMvc.perform(delete("/api/parcels/parcel-999"))
                .andExpect(status().isNotFound());

        verify(parcelService).delete("parcel-999");
    }

    // ==================== Tests pour GET /api/parcels/status/{status} ====================

    @Test
    @DisplayName("Should get parcels by status successfully")
    void testGetParcelsByStatus_Success() throws Exception {
        // GIVEN
        List<ParcelResponseDTO> parcels = Arrays.asList(responseDTO);
        when(parcelService.findByStatus(ParcelStatus.CREATED)).thenReturn(parcels);

        // WHEN & THEN
        mockMvc.perform(get("/api/parcels/status/CREATED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("CREATED"));

        verify(parcelService).findByStatus(ParcelStatus.CREATED);
    }

    @Test
    @DisplayName("Should return empty list when no parcels have the status")
    void testGetParcelsByStatus_EmptyList() throws Exception {
        // GIVEN
        when(parcelService.findByStatus(ParcelStatus.DELIVERED)).thenReturn(Arrays.asList());

        // WHEN & THEN
        mockMvc.perform(get("/api/parcels/status/DELIVERED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(parcelService).findByStatus(ParcelStatus.DELIVERED);
    }

    // ==================== Tests pour GET /api/parcels/priority/{priority} ====================

    @Test
    @DisplayName("Should get parcels by priority successfully")
    void testGetParcelsByPriority_Success() throws Exception {
        // GIVEN
        List<ParcelResponseDTO> parcels = Arrays.asList(responseDTO);
        when(parcelService.findByPriority(ParcelPriority.NORMAL)).thenReturn(parcels);

        // WHEN & THEN
        mockMvc.perform(get("/api/parcels/priority/NORMAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].priority").value("NORMAL"));

        verify(parcelService).findByPriority(ParcelPriority.NORMAL);
    }

    @Test
    @DisplayName("Should get urgent parcels by priority")
    void testGetParcelsByPriority_Urgent() throws Exception {
        // GIVEN
        ParcelResponseDTO urgentParcel = new ParcelResponseDTO();
        urgentParcel.setId("parcel-urgent");
        urgentParcel.setPriority(ParcelPriority.URGENT);
        urgentParcel.setStatus(ParcelStatus.IN_STOCK);

        List<ParcelResponseDTO> parcels = Arrays.asList(urgentParcel);
        when(parcelService.findByPriority(ParcelPriority.URGENT)).thenReturn(parcels);

        // WHEN & THEN
        mockMvc.perform(get("/api/parcels/priority/URGENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].priority").value("URGENT"));

        verify(parcelService).findByPriority(ParcelPriority.URGENT);
    }

    // ==================== Tests pour GET /api/parcels/search ====================

    @Test
    @DisplayName("Should search parcels with status filter and pagination")
    void testSearchParcels_WithStatusFilter() throws Exception {
        // GIVEN
        Page<ParcelResponseDTO> page = new PageImpl<>(
                Arrays.asList(responseDTO),
                PageRequest.of(0, 20),
                1
        );
        when(parcelService.searchParcels(
                eq(ParcelStatus.CREATED), isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), any()
        )).thenReturn(page);

        // WHEN & THEN
        mockMvc.perform(get("/api/parcels/search")
                        .param("status", "CREATED")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].status").value("CREATED"));

        verify(parcelService).searchParcels(
                eq(ParcelStatus.CREATED), isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), any()
        );
    }

    @Test
    @DisplayName("Should search parcels with priority filter")
    void testSearchParcels_WithPriorityFilter() throws Exception {
        // GIVEN
        Page<ParcelResponseDTO> page = new PageImpl<>(
                Arrays.asList(responseDTO),
                PageRequest.of(0, 20),
                1
        );
        when(parcelService.searchParcels(
                isNull(), eq(ParcelPriority.NORMAL), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), any()
        )).thenReturn(page);

        // WHEN & THEN
        mockMvc.perform(get("/api/parcels/search")
                        .param("priority", "NORMAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].priority").value("NORMAL"));

        verify(parcelService).searchParcels(
                isNull(), eq(ParcelPriority.NORMAL), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), any()
        );
    }

    @Test
    @DisplayName("Should search parcels with zone filter")
    void testSearchParcels_WithZoneFilter() throws Exception {
        // GIVEN
        Page<ParcelResponseDTO> page = new PageImpl<>(
                Arrays.asList(responseDTO),
                PageRequest.of(0, 20),
                1
        );
        when(parcelService.searchParcels(
                isNull(), isNull(), eq("zone-1"), isNull(), isNull(),
                isNull(), isNull(), isNull(), any()
        )).thenReturn(page);

        // WHEN & THEN
        mockMvc.perform(get("/api/parcels/search")
                        .param("zoneId", "zone-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].zoneId").value("zone-1"));

        verify(parcelService).searchParcels(
                isNull(), isNull(), eq("zone-1"), isNull(), isNull(),
                isNull(), isNull(), isNull(), any()
        );
    }

    @Test
    @DisplayName("Should search parcels with city filter")
    void testSearchParcels_WithCityFilter() throws Exception {
        // GIVEN
        Page<ParcelResponseDTO> page = new PageImpl<>(
                Arrays.asList(responseDTO),
                PageRequest.of(0, 20),
                1
        );
        when(parcelService.searchParcels(
                isNull(), isNull(), isNull(), eq("Paris"), isNull(),
                isNull(), isNull(), isNull(), any()
        )).thenReturn(page);

        // WHEN & THEN
        mockMvc.perform(get("/api/parcels/search")
                        .param("destinationCity", "Paris"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].destinationCity").value("Paris"));

        verify(parcelService).searchParcels(
                isNull(), isNull(), isNull(), eq("Paris"), isNull(),
                isNull(), isNull(), isNull(), any()
        );
    }

    @Test
    @DisplayName("Should search parcels with delivery person filter")
    void testSearchParcels_WithDeliveryPersonFilter() throws Exception {
        // GIVEN
        Page<ParcelResponseDTO> page = new PageImpl<>(
                Arrays.asList(responseDTO),
                PageRequest.of(0, 20),
                1
        );
        when(parcelService.searchParcels(
                isNull(), isNull(), isNull(), isNull(), eq("delivery-1"),
                isNull(), isNull(), isNull(), any()
        )).thenReturn(page);

        // WHEN & THEN
        mockMvc.perform(get("/api/parcels/search")
                        .param("deliveryPersonId", "delivery-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].deliveryPersonId").value("delivery-1"));

        verify(parcelService).searchParcels(
                isNull(), isNull(), isNull(), isNull(), eq("delivery-1"),
                isNull(), isNull(), isNull(), any()
        );
    }

    @Test
    @DisplayName("Should search parcels with sender client filter")
    void testSearchParcels_WithSenderFilter() throws Exception {
        // GIVEN
        Page<ParcelResponseDTO> page = new PageImpl<>(
                Arrays.asList(responseDTO),
                PageRequest.of(0, 20),
                1
        );
        when(parcelService.searchParcels(
                isNull(), isNull(), isNull(), isNull(), isNull(),
                eq("sender-1"), isNull(), isNull(), any()
        )).thenReturn(page);

        // WHEN & THEN
        mockMvc.perform(get("/api/parcels/search")
                        .param("senderClientId", "sender-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].senderClientId").value("sender-1"));

        verify(parcelService).searchParcels(
                isNull(), isNull(), isNull(), isNull(), isNull(),
                eq("sender-1"), isNull(), isNull(), any()
        );
    }

    @Test
    @DisplayName("Should search parcels with recipient filter")
    void testSearchParcels_WithRecipientFilter() throws Exception {
        // GIVEN
        Page<ParcelResponseDTO> page = new PageImpl<>(
                Arrays.asList(responseDTO),
                PageRequest.of(0, 20),
                1
        );
        when(parcelService.searchParcels(
                isNull(), isNull(), isNull(), isNull(), isNull(),
                isNull(), eq("recipient-1"), isNull(), any()
        )).thenReturn(page);

        // WHEN & THEN
        mockMvc.perform(get("/api/parcels/search")
                        .param("recipientId", "recipient-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].recipientId").value("recipient-1"));

        verify(parcelService).searchParcels(
                isNull(), isNull(), isNull(), isNull(), isNull(),
                isNull(), eq("recipient-1"), isNull(), any()
        );
    }

    @Test
    @DisplayName("Should search parcels with unassignedOnly filter")
    void testSearchParcels_WithUnassignedFilter() throws Exception {
        // GIVEN
        ParcelResponseDTO unassignedParcel = new ParcelResponseDTO();
        unassignedParcel.setId("parcel-unassigned");
        unassignedParcel.setDeliveryPersonId(null);

        Page<ParcelResponseDTO> page = new PageImpl<>(
                Arrays.asList(unassignedParcel),
                PageRequest.of(0, 20),
                1
        );
        when(parcelService.searchParcels(
                isNull(), isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), eq(true), any()
        )).thenReturn(page);

        // WHEN & THEN
        mockMvc.perform(get("/api/parcels/search")
                        .param("unassignedOnly", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));

        verify(parcelService).searchParcels(
                isNull(), isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), eq(true), any()
        );
    }

    @Test
    @DisplayName("Should search parcels with multiple filters combined")
    void testSearchParcels_MultipleFilters() throws Exception {
        // GIVEN
        Page<ParcelResponseDTO> page = new PageImpl<>(
                Arrays.asList(responseDTO),
                PageRequest.of(0, 20),
                1
        );
        when(parcelService.searchParcels(
                eq(ParcelStatus.IN_TRANSIT), eq(ParcelPriority.URGENT), eq("zone-1"),
                eq("Paris"), isNull(), isNull(), isNull(), isNull(), any()
        )).thenReturn(page);

        // WHEN & THEN
        mockMvc.perform(get("/api/parcels/search")
                        .param("status", "IN_TRANSIT")
                        .param("priority", "URGENT")
                        .param("zoneId", "zone-1")
                        .param("destinationCity", "Paris"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));

        verify(parcelService).searchParcels(
                eq(ParcelStatus.IN_TRANSIT), eq(ParcelPriority.URGENT), eq("zone-1"),
                eq("Paris"), isNull(), isNull(), isNull(), isNull(), any()
        );
    }

    // ==================== Tests pour GET /api/parcels/filter ====================

    @Test
    @DisplayName("Should get parcels by status and priority")
    void testGetParcelsByStatusAndPriority_Success() throws Exception {
        // GIVEN
        List<ParcelResponseDTO> parcels = Arrays.asList(responseDTO);
        when(parcelService.findByStatusAndPriority(ParcelStatus.CREATED, ParcelPriority.NORMAL))
                .thenReturn(parcels);

        // WHEN & THEN
        mockMvc.perform(get("/api/parcels/filter")
                        .param("status", "CREATED")
                        .param("priority", "NORMAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("CREATED"))
                .andExpect(jsonPath("$[0].priority").value("NORMAL"));

        verify(parcelService).findByStatusAndPriority(ParcelStatus.CREATED, ParcelPriority.NORMAL);
    }

    // ==================== Tests pour GET /api/parcels/status/{status}/count ====================

    @Test
    @DisplayName("Should count parcels by status successfully")
    void testCountParcelsByStatus_Success() throws Exception {
        // GIVEN
        when(parcelService.countByStatus(ParcelStatus.IN_TRANSIT)).thenReturn(5L);

        // WHEN & THEN
        mockMvc.perform(get("/api/parcels/status/IN_TRANSIT/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));

        verify(parcelService).countByStatus(ParcelStatus.IN_TRANSIT);
    }

    @Test
    @DisplayName("Should return zero when no parcels have the status")
    void testCountParcelsByStatus_Zero() throws Exception {
        // GIVEN
        when(parcelService.countByStatus(ParcelStatus.DELIVERED)).thenReturn(0L);

        // WHEN & THEN
        mockMvc.perform(get("/api/parcels/status/DELIVERED/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));

        verify(parcelService).countByStatus(ParcelStatus.DELIVERED);
    }

    // ==================== Tests pour GET /api/parcels/sender/{senderClientId} ====================

    @Test
    @DisplayName("Should get parcels by sender client successfully")
    void testGetParcelsBySenderClient_Success() throws Exception {
        // GIVEN
        List<ParcelResponseDTO> parcels = Arrays.asList(responseDTO);
        when(parcelService.findBySenderClientId("sender-1")).thenReturn(parcels);

        // WHEN & THEN
        mockMvc.perform(get("/api/parcels/sender/sender-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].senderClientId").value("sender-1"));

        verify(parcelService).findBySenderClientId("sender-1");
    }

    // ==================== Tests pour GET /api/parcels/recipient/{recipientId} ====================

    @Test
    @DisplayName("Should get parcels by recipient successfully")
    void testGetParcelsByRecipient_Success() throws Exception {
        // GIVEN
        List<ParcelResponseDTO> parcels = Arrays.asList(responseDTO);
        when(parcelService.findByRecipientId("recipient-1")).thenReturn(parcels);

        // WHEN & THEN
        mockMvc.perform(get("/api/parcels/recipient/recipient-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].recipientId").value("recipient-1"));

        verify(parcelService).findByRecipientId("recipient-1");
    }

    // ==================== Tests pour GET /api/parcels/delivery-person/{deliveryPersonId} ====================

    @Test
    @DisplayName("Should get parcels by delivery person successfully")
    void testGetParcelsByDeliveryPerson_Success() throws Exception {
        // GIVEN
        List<ParcelResponseDTO> parcels = Arrays.asList(responseDTO);
        when(parcelService.findByDeliveryPersonId("delivery-1")).thenReturn(parcels);

        // WHEN & THEN
        mockMvc.perform(get("/api/parcels/delivery-person/delivery-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].deliveryPersonId").value("delivery-1"));

        verify(parcelService).findByDeliveryPersonId("delivery-1");
    }

    // ==================== Tests pour GET /api/parcels/zone/{zoneId} ====================

    @Test
    @DisplayName("Should get parcels by zone successfully")
    void testGetParcelsByZone_Success() throws Exception {
        // GIVEN
        List<ParcelResponseDTO> parcels = Arrays.asList(responseDTO);
        when(parcelService.findByZoneId("zone-1")).thenReturn(parcels);

        // WHEN & THEN
        mockMvc.perform(get("/api/parcels/zone/zone-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].zoneId").value("zone-1"));

        verify(parcelService).findByZoneId("zone-1");
    }

    // ==================== Tests pour GET /api/parcels/unassigned ====================

    @Test
    @DisplayName("Should get unassigned parcels successfully")
    void testGetUnassignedParcels_Success() throws Exception {
        // GIVEN
        ParcelResponseDTO unassignedParcel = new ParcelResponseDTO();
        unassignedParcel.setId("parcel-unassigned");
        unassignedParcel.setDeliveryPersonId(null);
        unassignedParcel.setZoneId(null);

        List<ParcelResponseDTO> parcels = Arrays.asList(unassignedParcel);
        when(parcelService.findUnassignedParcels()).thenReturn(parcels);

        // WHEN & THEN
        mockMvc.perform(get("/api/parcels/unassigned"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value("parcel-unassigned"));

        verify(parcelService).findUnassignedParcels();
    }

    @Test
    @DisplayName("Should return empty list when no unassigned parcels exist")
    void testGetUnassignedParcels_Empty() throws Exception {
        // GIVEN
        when(parcelService.findUnassignedParcels()).thenReturn(Arrays.asList());

        // WHEN & THEN
        mockMvc.perform(get("/api/parcels/unassigned"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(parcelService).findUnassignedParcels();
    }

    // ==================== Tests pour GET /api/parcels/high-priority-pending ====================

    @Test
    @DisplayName("Should get high priority pending parcels successfully")
    void testGetHighPriorityPending_Success() throws Exception {
        // GIVEN
        ParcelResponseDTO expressParcel = new ParcelResponseDTO();
        expressParcel.setId("parcel-express");
        expressParcel.setPriority(ParcelPriority.EXPRESS);
        expressParcel.setStatus(ParcelStatus.IN_TRANSIT);

        List<ParcelResponseDTO> parcels = Arrays.asList(expressParcel);
        when(parcelService.findHighPriorityPending()).thenReturn(parcels);

        // WHEN & THEN
        mockMvc.perform(get("/api/parcels/high-priority-pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].priority").value("EXPRESS"));

        verify(parcelService).findHighPriorityPending();
    }

    // ==================== Tests pour GET /api/parcels/city/{city} ====================

    @Test
    @DisplayName("Should get parcels by destination city successfully")
    void testGetParcelsByCity_Success() throws Exception {
        // GIVEN
        List<ParcelResponseDTO> parcels = Arrays.asList(responseDTO);
        when(parcelService.findByDestinationCity("Paris")).thenReturn(parcels);

        // WHEN & THEN
        mockMvc.perform(get("/api/parcels/city/Paris"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].destinationCity").value("Paris"));

        verify(parcelService).findByDestinationCity("Paris");
    }

    // ==================== Tests pour GET /api/parcels/count ====================

    @Test
    @DisplayName("Should count total parcels successfully")
    void testCountTotalParcels_Success() throws Exception {
        // GIVEN
        when(parcelService.countTotalParcels()).thenReturn(42L);

        // WHEN & THEN
        mockMvc.perform(get("/api/parcels/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("42"));

        verify(parcelService).countTotalParcels();
    }

    @Test
    @DisplayName("Should return zero when no parcels exist")
    void testCountTotalParcels_Zero() throws Exception {
        // GIVEN
        when(parcelService.countTotalParcels()).thenReturn(0L);

        // WHEN & THEN
        mockMvc.perform(get("/api/parcels/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));

        verify(parcelService).countTotalParcels();
    }

    // ==================== Tests pour GET /api/parcels/group-by/status ====================

    @Test
    @DisplayName("Should group parcels by status successfully")
    void testGroupByStatus_Success() throws Exception {
        // GIVEN
        Map<String, Long> groupedData = new HashMap<>();
        groupedData.put("CREATED", 10L);
        groupedData.put("IN_TRANSIT", 15L);
        groupedData.put("DELIVERED", 25L);
        groupedData.put("IN_STOCK", 8L);

        when(parcelService.groupByStatus()).thenReturn(groupedData);

        // WHEN & THEN
        mockMvc.perform(get("/api/parcels/group-by/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.CREATED").value(10))
                .andExpect(jsonPath("$.IN_TRANSIT").value(15))
                .andExpect(jsonPath("$.DELIVERED").value(25))
                .andExpect(jsonPath("$.IN_STOCK").value(8));

        verify(parcelService).groupByStatus();
    }

    @Test
    @DisplayName("Should return empty map when no parcels grouped by status")
    void testGroupByStatus_Empty() throws Exception {
        // GIVEN
        when(parcelService.groupByStatus()).thenReturn(new HashMap<>());

        // WHEN & THEN
        mockMvc.perform(get("/api/parcels/group-by/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", aMapWithSize(0)));

        verify(parcelService).groupByStatus();
    }

    // ==================== Tests pour GET /api/parcels/group-by/priority ====================

    @Test
    @DisplayName("Should group parcels by priority successfully")
    void testGroupByPriority_Success() throws Exception {
        // GIVEN
        Map<String, Long> groupedData = new HashMap<>();
        groupedData.put("NORMAL", 30L);
        groupedData.put("URGENT", 12L);
        groupedData.put("EXPRESS", 18L);

        when(parcelService.groupByPriority()).thenReturn(groupedData);

        // WHEN & THEN
        mockMvc.perform(get("/api/parcels/group-by/priority"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.NORMAL").value(30))
                .andExpect(jsonPath("$.URGENT").value(12))
                .andExpect(jsonPath("$.EXPRESS").value(18));

        verify(parcelService).groupByPriority();
    }

    // ==================== Tests pour GET /api/parcels/group-by/zone ====================

    @Test
    @DisplayName("Should group parcels by zone successfully")
    void testGroupByZone_Success() throws Exception {
        // GIVEN
        Map<String, Long> groupedData = new HashMap<>();
        groupedData.put("zone-1", 15L);
        groupedData.put("zone-2", 22L);
        groupedData.put("zone-3", 18L);

        when(parcelService.groupByZone()).thenReturn(groupedData);

        // WHEN & THEN
        mockMvc.perform(get("/api/parcels/group-by/zone"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['zone-1']").value(15))
                .andExpect(jsonPath("$['zone-2']").value(22))
                .andExpect(jsonPath("$['zone-3']").value(18));

        verify(parcelService).groupByZone();
    }

    // ==================== Tests pour GET /api/parcels/group-by/city ====================

    @Test
    @DisplayName("Should group parcels by city successfully")
    void testGroupByCity_Success() throws Exception {
        // GIVEN
        Map<String, Long> groupedData = new HashMap<>();
        groupedData.put("Paris", 35L);
        groupedData.put("Lyon", 28L);
        groupedData.put("Marseille", 22L);

        when(parcelService.groupByCity()).thenReturn(groupedData);

        // WHEN & THEN
        mockMvc.perform(get("/api/parcels/group-by/city"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Paris").value(35))
                .andExpect(jsonPath("$.Lyon").value(28))
                .andExpect(jsonPath("$.Marseille").value(22));

        verify(parcelService).groupByCity();
    }

    // ==================== Tests pour GET /api/parcels/{id}/history ====================

    @Test
    @DisplayName("Should get parcel delivery history successfully")
    void testGetParcelHistory_Success() throws Exception {
        // GIVEN
        DeliveryHistoryResponseDTO history1 = new DeliveryHistoryResponseDTO();
        history1.setId("history-1");
        history1.setParcelId("parcel-1");
        history1.setStatus(ParcelStatus.CREATED);
        history1.setComment("Parcel created and registered");
        history1.setChangedAt(LocalDateTime.now());

        DeliveryHistoryResponseDTO history2 = new DeliveryHistoryResponseDTO();
        history2.setId("history-2");
        history2.setParcelId("parcel-1");
        history2.setStatus(ParcelStatus.IN_STOCK);
        history2.setComment("Parcel received at warehouse");
        history2.setChangedAt(LocalDateTime.now());

        List<DeliveryHistoryResponseDTO> histories = Arrays.asList(history1, history2);
        when(parcelService.getParcelHistory("parcel-1")).thenReturn(histories);

        // WHEN & THEN
        mockMvc.perform(get("/api/parcels/parcel-1/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].status").value("CREATED"))
                .andExpect(jsonPath("$[0].comment").value("Parcel created and registered"))
                .andExpect(jsonPath("$[1].status").value("IN_STOCK"))
                .andExpect(jsonPath("$[1].comment").value("Parcel received at warehouse"));

        verify(parcelService).getParcelHistory("parcel-1");
    }

    @Test
    @DisplayName("Should return empty history when parcel has no history entries")
    void testGetParcelHistory_Empty() throws Exception {
        // GIVEN
        when(parcelService.getParcelHistory("parcel-1")).thenReturn(Arrays.asList());

        // WHEN & THEN
        mockMvc.perform(get("/api/parcels/parcel-1/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(parcelService).getParcelHistory("parcel-1");
    }

    @Test
    @DisplayName("Should return 404 when getting history for non-existent parcel")
    void testGetParcelHistory_NotFound() throws Exception {
        // GIVEN
        when(parcelService.getParcelHistory("parcel-999"))
                .thenThrow(new ResourceNotFoundException("Parcel", "id", "parcel-999"));

        // WHEN & THEN
        mockMvc.perform(get("/api/parcels/parcel-999/history"))
                .andExpect(status().isNotFound());

        verify(parcelService).getParcelHistory("parcel-999");
    }
}
