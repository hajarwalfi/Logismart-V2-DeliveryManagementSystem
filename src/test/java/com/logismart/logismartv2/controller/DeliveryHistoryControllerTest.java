package com.logismart.logismartv2.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logismart.logismartv2.dto.deliveryhistory.DeliveryHistoryCreateDTO;
import com.logismart.logismartv2.dto.deliveryhistory.DeliveryHistoryResponseDTO;
import com.logismart.logismartv2.entity.ParcelStatus;
import com.logismart.logismartv2.exception.ResourceNotFoundException;
import com.logismart.logismartv2.service.DeliveryHistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitaires pour DeliveryHistoryController
 * Utilise MockMvc pour tester les endpoints REST
 */
@WebMvcTest(DeliveryHistoryController.class)
@DisplayName("DeliveryHistoryController Unit Tests")
class DeliveryHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DeliveryHistoryService deliveryHistoryService;

    private DeliveryHistoryResponseDTO responseDTO;
    private DeliveryHistoryCreateDTO createDTO;

    @BeforeEach
    void setUp() {
        responseDTO = new DeliveryHistoryResponseDTO();
        responseDTO.setId("history-1");
        responseDTO.setParcelId("parcel-1");
        responseDTO.setStatus(ParcelStatus.IN_TRANSIT);
        responseDTO.setStatusDisplay("In Transit");
        responseDTO.setChangedAt(LocalDateTime.now());
        responseDTO.setFormattedChangedAt("12 Nov 2024, 02:30 PM");
        responseDTO.setComment("Parcel is out for delivery");
        responseDTO.setHasComment(true);
        responseDTO.setSummary("In Transit at 12 Nov 2024, 02:30 PM");
        responseDTO.setDetailedSummary("In Transit at 12 Nov 2024, 02:30 PM - Parcel is out for delivery");

        createDTO = new DeliveryHistoryCreateDTO();
        createDTO.setParcelId("parcel-1");
        createDTO.setStatus(ParcelStatus.IN_TRANSIT);
        createDTO.setComment("Parcel is out for delivery");
    }

    // ==================== Tests pour POST /api/delivery-history ====================

    @Test
    @DisplayName("Should create delivery history successfully and return 201")
    void testCreateDeliveryHistory_Success() throws Exception {
        // GIVEN
        when(deliveryHistoryService.create(any(DeliveryHistoryCreateDTO.class))).thenReturn(responseDTO);

        // WHEN & THEN
        mockMvc.perform(post("/api/delivery-history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("history-1"))
                .andExpect(jsonPath("$.parcelId").value("parcel-1"))
                .andExpect(jsonPath("$.status").value("IN_TRANSIT"))
                .andExpect(jsonPath("$.statusDisplay").value("In Transit"))
                .andExpect(jsonPath("$.comment").value("Parcel is out for delivery"));

        verify(deliveryHistoryService).create(any(DeliveryHistoryCreateDTO.class));
    }

    @Test
    @DisplayName("Should return 400 when creating history with invalid data")
    void testCreateDeliveryHistory_InvalidData() throws Exception {
        // GIVEN
        DeliveryHistoryCreateDTO invalidDTO = new DeliveryHistoryCreateDTO();
        invalidDTO.setParcelId(null); // Missing required field
        invalidDTO.setStatus(ParcelStatus.CREATED);

        // WHEN & THEN
        mockMvc.perform(post("/api/delivery-history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 404 when parcel not found during creation")
    void testCreateDeliveryHistory_ParcelNotFound() throws Exception {
        // GIVEN
        when(deliveryHistoryService.create(any(DeliveryHistoryCreateDTO.class)))
                .thenThrow(new ResourceNotFoundException("Parcel", "id", "parcel-999"));

        // WHEN & THEN
        mockMvc.perform(post("/api/delivery-history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isNotFound());

        verify(deliveryHistoryService).create(any(DeliveryHistoryCreateDTO.class));
    }

    // ==================== Tests pour GET /api/delivery-history/{id} ====================

    @Test
    @DisplayName("Should get delivery history by id successfully and return 200")
    void testGetDeliveryHistoryById_Success() throws Exception {
        // GIVEN
        when(deliveryHistoryService.findById("history-1")).thenReturn(responseDTO);

        // WHEN & THEN
        mockMvc.perform(get("/api/delivery-history/history-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("history-1"))
                .andExpect(jsonPath("$.parcelId").value("parcel-1"))
                .andExpect(jsonPath("$.statusDisplay").value("In Transit"));

        verify(deliveryHistoryService).findById("history-1");
    }

    @Test
    @DisplayName("Should return 404 when history entry not found")
    void testGetDeliveryHistoryById_NotFound() throws Exception {
        // GIVEN
        when(deliveryHistoryService.findById("history-999"))
                .thenThrow(new ResourceNotFoundException("DeliveryHistory", "id", "history-999"));

        // WHEN & THEN
        mockMvc.perform(get("/api/delivery-history/history-999"))
                .andExpect(status().isNotFound());

        verify(deliveryHistoryService).findById("history-999");
    }

    // ==================== Tests pour GET /api/delivery-history ====================

    @Test
    @DisplayName("Should get all delivery history entries successfully and return 200")
    void testGetAllDeliveryHistory_Success() throws Exception {
        // GIVEN
        DeliveryHistoryResponseDTO history2 = new DeliveryHistoryResponseDTO();
        history2.setId("history-2");
        history2.setParcelId("parcel-2");
        history2.setStatus(ParcelStatus.DELIVERED);
        history2.setStatusDisplay("Delivered");
        history2.setChangedAt(LocalDateTime.now());
        history2.setFormattedChangedAt("11 Nov 2024, 05:15 PM");
        history2.setComment("Parcel delivered");
        history2.setHasComment(true);

        List<DeliveryHistoryResponseDTO> histories = Arrays.asList(responseDTO, history2);
        when(deliveryHistoryService.findAll()).thenReturn(histories);

        // WHEN & THEN
        mockMvc.perform(get("/api/delivery-history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value("history-1"))
                .andExpect(jsonPath("$[1].id").value("history-2"));

        verify(deliveryHistoryService).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no delivery history entries exist")
    void testGetAllDeliveryHistory_EmptyList() throws Exception {
        // GIVEN
        when(deliveryHistoryService.findAll()).thenReturn(Arrays.asList());

        // WHEN & THEN
        mockMvc.perform(get("/api/delivery-history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(deliveryHistoryService).findAll();
    }

    // ==================== Tests pour GET /api/delivery-history/parcel/{parcelId} ====================

    @Test
    @DisplayName("Should get history by parcel id successfully and return 200")
    void testGetHistoryByParcelId_Success() throws Exception {
        // GIVEN
        List<DeliveryHistoryResponseDTO> histories = Arrays.asList(responseDTO);
        when(deliveryHistoryService.findByParcelId("parcel-1")).thenReturn(histories);

        // WHEN & THEN
        mockMvc.perform(get("/api/delivery-history/parcel/parcel-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].parcelId").value("parcel-1"))
                .andExpect(jsonPath("$[0].status").value("IN_TRANSIT"));

        verify(deliveryHistoryService).findByParcelId("parcel-1");
    }

    @Test
    @DisplayName("Should return 404 when parcel not found")
    void testGetHistoryByParcelId_ParcelNotFound() throws Exception {
        // GIVEN
        when(deliveryHistoryService.findByParcelId("parcel-999"))
                .thenThrow(new ResourceNotFoundException("Parcel", "id", "parcel-999"));

        // WHEN & THEN
        mockMvc.perform(get("/api/delivery-history/parcel/parcel-999"))
                .andExpect(status().isNotFound());

        verify(deliveryHistoryService).findByParcelId("parcel-999");
    }

    @Test
    @DisplayName("Should return empty list when parcel has no history")
    void testGetHistoryByParcelId_EmptyHistory() throws Exception {
        // GIVEN
        when(deliveryHistoryService.findByParcelId("parcel-2")).thenReturn(Arrays.asList());

        // WHEN & THEN
        mockMvc.perform(get("/api/delivery-history/parcel/parcel-2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(deliveryHistoryService).findByParcelId("parcel-2");
    }

    // ==================== Tests pour GET /api/delivery-history/parcel/{parcelId}/latest ====================

    @Test
    @DisplayName("Should get latest history by parcel id successfully and return 200")
    void testGetLatestHistoryByParcelId_Success() throws Exception {
        // GIVEN
        when(deliveryHistoryService.findLatestByParcelId("parcel-1")).thenReturn(responseDTO);

        // WHEN & THEN
        mockMvc.perform(get("/api/delivery-history/parcel/parcel-1/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("history-1"))
                .andExpect(jsonPath("$.parcelId").value("parcel-1"))
                .andExpect(jsonPath("$.status").value("IN_TRANSIT"));

        verify(deliveryHistoryService).findLatestByParcelId("parcel-1");
    }

    @Test
    @DisplayName("Should return 404 when parcel not found for latest history")
    void testGetLatestHistoryByParcelId_ParcelNotFound() throws Exception {
        // GIVEN
        when(deliveryHistoryService.findLatestByParcelId("parcel-999"))
                .thenThrow(new ResourceNotFoundException("Parcel", "id", "parcel-999"));

        // WHEN & THEN
        mockMvc.perform(get("/api/delivery-history/parcel/parcel-999/latest"))
                .andExpect(status().isNotFound());

        verify(deliveryHistoryService).findLatestByParcelId("parcel-999");
    }

    // ==================== Tests pour DELETE /api/delivery-history/{id} ====================

    @Test
    @DisplayName("Should delete delivery history successfully and return 204")
    void testDeleteDeliveryHistory_Success() throws Exception {
        // GIVEN
        doNothing().when(deliveryHistoryService).delete("history-1");

        // WHEN & THEN
        mockMvc.perform(delete("/api/delivery-history/history-1"))
                .andExpect(status().isNoContent());

        verify(deliveryHistoryService).delete("history-1");
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent history entry")
    void testDeleteDeliveryHistory_NotFound() throws Exception {
        // GIVEN
        doThrow(new ResourceNotFoundException("DeliveryHistory", "id", "history-999"))
                .when(deliveryHistoryService).delete("history-999");

        // WHEN & THEN
        mockMvc.perform(delete("/api/delivery-history/history-999"))
                .andExpect(status().isNotFound());

        verify(deliveryHistoryService).delete("history-999");
    }

    // ==================== Tests pour GET /api/delivery-history/parcel/{parcelId}/count ====================

    @Test
    @DisplayName("Should count history entries for parcel successfully and return 200")
    void testCountHistoryByParcelId_Success() throws Exception {
        // GIVEN
        when(deliveryHistoryService.countByParcelId("parcel-1")).thenReturn(3L);

        // WHEN & THEN
        mockMvc.perform(get("/api/delivery-history/parcel/parcel-1/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));

        verify(deliveryHistoryService).countByParcelId("parcel-1");
    }

    @Test
    @DisplayName("Should return 404 when counting history for non-existent parcel")
    void testCountHistoryByParcelId_ParcelNotFound() throws Exception {
        // GIVEN
        when(deliveryHistoryService.countByParcelId("parcel-999"))
                .thenThrow(new ResourceNotFoundException("Parcel", "id", "parcel-999"));

        // WHEN & THEN
        mockMvc.perform(get("/api/delivery-history/parcel/parcel-999/count"))
                .andExpect(status().isNotFound());

        verify(deliveryHistoryService).countByParcelId("parcel-999");
    }

    @Test
    @DisplayName("Should return 0 count when parcel has no history")
    void testCountHistoryByParcelId_NoHistory() throws Exception {
        // GIVEN
        when(deliveryHistoryService.countByParcelId("parcel-2")).thenReturn(0L);

        // WHEN & THEN
        mockMvc.perform(get("/api/delivery-history/parcel/parcel-2/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));

        verify(deliveryHistoryService).countByParcelId("parcel-2");
    }

    // ==================== Tests pour GET /api/delivery-history/with-comments ====================

    @Test
    @DisplayName("Should get history entries with comments successfully and return 200")
    void testGetHistoryWithComments_Success() throws Exception {
        // GIVEN
        List<DeliveryHistoryResponseDTO> histories = Arrays.asList(responseDTO);
        when(deliveryHistoryService.findEntriesWithComments()).thenReturn(histories);

        // WHEN & THEN
        mockMvc.perform(get("/api/delivery-history/with-comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].hasComment").value(true))
                .andExpect(jsonPath("$[0].comment").value("Parcel is out for delivery"));

        verify(deliveryHistoryService).findEntriesWithComments();
    }

    @Test
    @DisplayName("Should return empty list when no history entries with comments exist")
    void testGetHistoryWithComments_EmptyList() throws Exception {
        // GIVEN
        when(deliveryHistoryService.findEntriesWithComments()).thenReturn(Arrays.asList());

        // WHEN & THEN
        mockMvc.perform(get("/api/delivery-history/with-comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(deliveryHistoryService).findEntriesWithComments();
    }

    // ==================== Tests pour GET /api/delivery-history/deliveries/today/count ====================

    @Test
    @DisplayName("Should count deliveries completed today successfully and return 200")
    void testCountDeliveriesToday_Success() throws Exception {
        // GIVEN
        when(deliveryHistoryService.countDeliveriesToday()).thenReturn(15L);

        // WHEN & THEN
        mockMvc.perform(get("/api/delivery-history/deliveries/today/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("15"));

        verify(deliveryHistoryService).countDeliveriesToday();
    }

    @Test
    @DisplayName("Should return 0 count when no deliveries completed today")
    void testCountDeliveriesToday_NoDeliveries() throws Exception {
        // GIVEN
        when(deliveryHistoryService.countDeliveriesToday()).thenReturn(0L);

        // WHEN & THEN
        mockMvc.perform(get("/api/delivery-history/deliveries/today/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));

        verify(deliveryHistoryService).countDeliveriesToday();
    }
}
