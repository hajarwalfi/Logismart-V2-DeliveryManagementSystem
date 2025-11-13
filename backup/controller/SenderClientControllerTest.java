package com.logismart.logismartv2.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logismart.logismartv2.dto.parcel.ParcelResponseDTO;
import com.logismart.logismartv2.dto.senderclient.SenderClientCreateDTO;
import com.logismart.logismartv2.dto.senderclient.SenderClientResponseDTO;
import com.logismart.logismartv2.dto.senderclient.SenderClientUpdateDTO;
import com.logismart.logismartv2.entity.ParcelStatus;
import com.logismart.logismartv2.exception.DuplicateResourceException;
import com.logismart.logismartv2.exception.ResourceNotFoundException;
import com.logismart.logismartv2.service.ParcelService;
import com.logismart.logismartv2.service.SenderClientService;
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
 * Tests unitaires pour SenderClientController
 * Utilise MockMvc pour tester les endpoints REST
 */
@WebMvcTest(SenderClientController.class)
@DisplayName("SenderClientController Unit Tests")
class SenderClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SenderClientService senderClientService;

    @MockBean
    private ParcelService parcelService;

    private SenderClientResponseDTO responseDTO;
    private SenderClientCreateDTO createDTO;
    private SenderClientUpdateDTO updateDTO;

    @BeforeEach
    void setUp() {
        responseDTO = new SenderClientResponseDTO();
        responseDTO.setId("client-1");
        responseDTO.setFirstName("John");
        responseDTO.setLastName("Doe");
        responseDTO.setFullName("John Doe");
        responseDTO.setEmail("john.doe@example.com");
        responseDTO.setPhone("555-1234");
        responseDTO.setAddress("123 Main St, City");

        createDTO = new SenderClientCreateDTO();
        createDTO.setFirstName("John");
        createDTO.setLastName("Doe");
        createDTO.setEmail("john.doe@example.com");
        createDTO.setPhone("555-1234");
        createDTO.setAddress("123 Main St, City");

        updateDTO = new SenderClientUpdateDTO();
        updateDTO.setId("client-1");
        updateDTO.setFirstName("Jane");
        updateDTO.setLastName("Smith");
        updateDTO.setEmail("jane.smith@example.com");
        updateDTO.setPhone("555-5678");
        updateDTO.setAddress("456 Oak Ave, Town");
    }

    // ==================== Tests pour POST /api/sender-clients ====================

    @Test
    @DisplayName("Should create sender client successfully and return 201")
    void testCreateSenderClient_Success() throws Exception {
        // GIVEN
        when(senderClientService.create(any(SenderClientCreateDTO.class))).thenReturn(responseDTO);

        // WHEN & THEN
        mockMvc.perform(post("/api/sender-clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("client-1"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        verify(senderClientService).create(any(SenderClientCreateDTO.class));
    }

    @Test
    @DisplayName("Should return 409 when creating sender client with duplicate email")
    void testCreateSenderClient_DuplicateEmail() throws Exception {
        // GIVEN
        when(senderClientService.create(any(SenderClientCreateDTO.class)))
                .thenThrow(new DuplicateResourceException("SenderClient", "email", createDTO.getEmail()));

        // WHEN & THEN
        mockMvc.perform(post("/api/sender-clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isConflict());

        verify(senderClientService).create(any(SenderClientCreateDTO.class));
    }

    // ==================== Tests pour GET /api/sender-clients/{id} ====================

    @Test
    @DisplayName("Should get sender client by id successfully and return 200")
    void testGetSenderClientById_Success() throws Exception {
        // GIVEN
        when(senderClientService.findById("client-1")).thenReturn(responseDTO);

        // WHEN & THEN
        mockMvc.perform(get("/api/sender-clients/client-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("client-1"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        verify(senderClientService).findById("client-1");
    }

    @Test
    @DisplayName("Should return 404 when sender client not found")
    void testGetSenderClientById_NotFound() throws Exception {
        // GIVEN
        when(senderClientService.findById("client-999"))
                .thenThrow(new ResourceNotFoundException("SenderClient", "id", "client-999"));

        // WHEN & THEN
        mockMvc.perform(get("/api/sender-clients/client-999"))
                .andExpect(status().isNotFound());

        verify(senderClientService).findById("client-999");
    }

    // ==================== Tests pour GET /api/sender-clients ====================

    @Test
    @DisplayName("Should get all sender clients successfully and return 200")
    void testGetAllSenderClients_Success() throws Exception {
        // GIVEN
        SenderClientResponseDTO client2 = new SenderClientResponseDTO();
        client2.setId("client-2");
        client2.setFirstName("Jane");
        client2.setLastName("Smith");
        client2.setFullName("Jane Smith");
        client2.setEmail("jane.smith@example.com");
        client2.setPhone("555-5678");
        client2.setAddress("456 Oak Ave, Town");

        List<SenderClientResponseDTO> clients = Arrays.asList(responseDTO, client2);
        when(senderClientService.findAll()).thenReturn(clients);

        // WHEN & THEN
        mockMvc.perform(get("/api/sender-clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value("client-1"))
                .andExpect(jsonPath("$[1].id").value("client-2"));

        verify(senderClientService).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no sender clients exist")
    void testGetAllSenderClients_EmptyList() throws Exception {
        // GIVEN
        when(senderClientService.findAll()).thenReturn(Arrays.asList());

        // WHEN & THEN
        mockMvc.perform(get("/api/sender-clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(senderClientService).findAll();
    }

    // ==================== Tests pour PUT /api/sender-clients/{id} ====================

    @Test
    @DisplayName("Should update sender client successfully and return 200")
    void testUpdateSenderClient_Success() throws Exception {
        // GIVEN
        responseDTO.setFirstName("Jane");
        responseDTO.setLastName("Smith");
        responseDTO.setEmail("jane.smith@example.com");
        responseDTO.setFullName("Jane Smith");
        when(senderClientService.update(any(SenderClientUpdateDTO.class))).thenReturn(responseDTO);

        // WHEN & THEN
        mockMvc.perform(put("/api/sender-clients/client-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("client-1"))
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.email").value("jane.smith@example.com"));

        verify(senderClientService).update(any(SenderClientUpdateDTO.class));
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent sender client")
    void testUpdateSenderClient_NotFound() throws Exception {
        // GIVEN
        when(senderClientService.update(any(SenderClientUpdateDTO.class)))
                .thenThrow(new ResourceNotFoundException("SenderClient", "id", "client-999"));

        // WHEN & THEN
        mockMvc.perform(put("/api/sender-clients/client-999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());

        verify(senderClientService).update(any(SenderClientUpdateDTO.class));
    }

    // ==================== Tests pour DELETE /api/sender-clients/{id} ====================

    @Test
    @DisplayName("Should delete sender client successfully and return 204")
    void testDeleteSenderClient_Success() throws Exception {
        // GIVEN
        doNothing().when(senderClientService).delete("client-1");

        // WHEN & THEN
        mockMvc.perform(delete("/api/sender-clients/client-1"))
                .andExpect(status().isNoContent());

        verify(senderClientService).delete("client-1");
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent sender client")
    void testDeleteSenderClient_NotFound() throws Exception {
        // GIVEN
        doThrow(new ResourceNotFoundException("SenderClient", "id", "client-999"))
                .when(senderClientService).delete("client-999");

        // WHEN & THEN
        mockMvc.perform(delete("/api/sender-clients/client-999"))
                .andExpect(status().isNotFound());

        verify(senderClientService).delete("client-999");
    }

    // ==================== Tests pour GET /api/sender-clients/by-email/{email} ====================

    @Test
    @DisplayName("Should get sender client by email successfully")
    void testGetSenderClientByEmail_Success() throws Exception {
        // GIVEN
        when(senderClientService.findByEmail("john.doe@example.com")).thenReturn(responseDTO);

        // WHEN & THEN
        mockMvc.perform(get("/api/sender-clients/by-email/john.doe@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("client-1"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        verify(senderClientService).findByEmail("john.doe@example.com");
    }

    @Test
    @DisplayName("Should return 404 when sender client with given email not found")
    void testGetSenderClientByEmail_NotFound() throws Exception {
        // GIVEN
        when(senderClientService.findByEmail("nonexistent@example.com"))
                .thenThrow(new ResourceNotFoundException("SenderClient", "email", "nonexistent@example.com"));

        // WHEN & THEN
        mockMvc.perform(get("/api/sender-clients/by-email/nonexistent@example.com"))
                .andExpect(status().isNotFound());

        verify(senderClientService).findByEmail("nonexistent@example.com");
    }

    // ==================== Tests pour GET /api/sender-clients/search ====================

    @Test
    @DisplayName("Should search sender clients by name successfully")
    void testSearchSenderClients_Success() throws Exception {
        // GIVEN
        List<SenderClientResponseDTO> clients = Arrays.asList(responseDTO);
        when(senderClientService.searchByName("John")).thenReturn(clients);

        // WHEN & THEN
        mockMvc.perform(get("/api/sender-clients/search")
                        .param("keyword", "John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].firstName").value("John"));

        verify(senderClientService).searchByName("John");
    }

    @Test
    @DisplayName("Should return empty list when no matching sender clients found")
    void testSearchSenderClients_EmptyResult() throws Exception {
        // GIVEN
        when(senderClientService.searchByName("NonExistent")).thenReturn(Arrays.asList());

        // WHEN & THEN
        mockMvc.perform(get("/api/sender-clients/search")
                        .param("keyword", "NonExistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(senderClientService).searchByName("NonExistent");
    }

    // ==================== Tests pour GET /api/sender-clients/{id}/parcels/count ====================

    @Test
    @DisplayName("Should count parcels for sender client successfully")
    void testCountParcels_Success() throws Exception {
        // GIVEN
        when(senderClientService.countParcels("client-1")).thenReturn(5L);

        // WHEN & THEN
        mockMvc.perform(get("/api/sender-clients/client-1/parcels/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));

        verify(senderClientService).countParcels("client-1");
    }

    @Test
    @DisplayName("Should return 404 when counting parcels for non-existent sender client")
    void testCountParcels_NotFound() throws Exception {
        // GIVEN
        when(senderClientService.countParcels("client-999"))
                .thenThrow(new ResourceNotFoundException("SenderClient", "id", "client-999"));

        // WHEN & THEN
        mockMvc.perform(get("/api/sender-clients/client-999/parcels/count"))
                .andExpect(status().isNotFound());

        verify(senderClientService).countParcels("client-999");
    }

    // ==================== Tests pour GET /api/sender-clients/{id}/parcels/in-progress ====================

    @Test
    @DisplayName("Should get in-progress parcels for sender client successfully")
    void testGetInProgressParcels_Success() throws Exception {
        // GIVEN
        ParcelResponseDTO parcel1 = new ParcelResponseDTO();
        parcel1.setId("parcel-1");
        parcel1.setStatus(ParcelStatus.IN_TRANSIT);

        ParcelResponseDTO parcel2 = new ParcelResponseDTO();
        parcel2.setId("parcel-2");
        parcel2.setStatus(ParcelStatus.COLLECTED);

        List<ParcelResponseDTO> parcels = Arrays.asList(parcel1, parcel2);
        when(parcelService.findInProgressBySenderClientId("client-1")).thenReturn(parcels);

        // WHEN & THEN
        mockMvc.perform(get("/api/sender-clients/client-1/parcels/in-progress"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value("parcel-1"))
                .andExpect(jsonPath("$[1].id").value("parcel-2"));

        verify(parcelService).findInProgressBySenderClientId("client-1");
    }

    @Test
    @DisplayName("Should return empty list when no in-progress parcels found")
    void testGetInProgressParcels_EmptyList() throws Exception {
        // GIVEN
        when(parcelService.findInProgressBySenderClientId("client-1")).thenReturn(Arrays.asList());

        // WHEN & THEN
        mockMvc.perform(get("/api/sender-clients/client-1/parcels/in-progress"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(parcelService).findInProgressBySenderClientId("client-1");
    }

    // ==================== Tests pour GET /api/sender-clients/{id}/parcels/delivered ====================

    @Test
    @DisplayName("Should get delivered parcels for sender client successfully")
    void testGetDeliveredParcels_Success() throws Exception {
        // GIVEN
        ParcelResponseDTO parcel1 = new ParcelResponseDTO();
        parcel1.setId("parcel-3");
        parcel1.setStatus(ParcelStatus.DELIVERED);

        List<ParcelResponseDTO> parcels = Arrays.asList(parcel1);
        when(parcelService.findDeliveredBySenderClientId("client-1")).thenReturn(parcels);

        // WHEN & THEN
        mockMvc.perform(get("/api/sender-clients/client-1/parcels/delivered"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value("parcel-3"))
                .andExpect(jsonPath("$[0].status").value("DELIVERED"));

        verify(parcelService).findDeliveredBySenderClientId("client-1");
    }

    @Test
    @DisplayName("Should return empty list when no delivered parcels found")
    void testGetDeliveredParcels_EmptyList() throws Exception {
        // GIVEN
        when(parcelService.findDeliveredBySenderClientId("client-1")).thenReturn(Arrays.asList());

        // WHEN & THEN
        mockMvc.perform(get("/api/sender-clients/client-1/parcels/delivered"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(parcelService).findDeliveredBySenderClientId("client-1");
    }
}
