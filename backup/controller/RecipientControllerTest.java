package com.logismart.logismartv2.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logismart.logismartv2.dto.recipient.RecipientCreateDTO;
import com.logismart.logismartv2.dto.recipient.RecipientResponseDTO;
import com.logismart.logismartv2.dto.recipient.RecipientUpdateDTO;
import com.logismart.logismartv2.exception.ResourceNotFoundException;
import com.logismart.logismartv2.service.RecipientService;
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

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitaires pour RecipientController
 */
@WebMvcTest(RecipientController.class)
@DisplayName("RecipientController Unit Tests")
class RecipientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RecipientService recipientService;

    private RecipientResponseDTO responseDTO;
    private RecipientCreateDTO createDTO;
    private RecipientUpdateDTO updateDTO;

    @BeforeEach
    void setUp() {
        responseDTO = new RecipientResponseDTO();
        responseDTO.setId("recipient-1");
        responseDTO.setFirstName("Ahmed");
        responseDTO.setLastName("Benali");
        responseDTO.setPhone("0612345678");
        responseDTO.setEmail("ahmed.benali@example.com");
        responseDTO.setAddress("123 Rue Mohammed V, Casablanca");

        createDTO = new RecipientCreateDTO();
        createDTO.setFirstName("Ahmed");
        createDTO.setLastName("Benali");
        createDTO.setPhone("0612345678");
        createDTO.setEmail("ahmed.benali@example.com");
        createDTO.setAddress("123 Rue Mohammed V, Casablanca");

        updateDTO = new RecipientUpdateDTO();
        updateDTO.setId("recipient-1");
        updateDTO.setFirstName("Hassan");
        updateDTO.setLastName("Alami");
        updateDTO.setPhone("0698765432");
        updateDTO.setEmail("hassan.alami@example.com");
        updateDTO.setAddress("456 Boulevard Zerktouni, Rabat");
    }

    // ==================== Tests pour POST /api/recipients ====================

    @Test
    @DisplayName("Should create recipient successfully and return 201")
    void testCreateRecipient_Success() throws Exception {
        // GIVEN
        when(recipientService.create(any(RecipientCreateDTO.class))).thenReturn(responseDTO);

        // WHEN & THEN
        mockMvc.perform(post("/api/recipients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("recipient-1"))
                .andExpect(jsonPath("$.firstName").value("Ahmed"))
                .andExpect(jsonPath("$.phone").value("0612345678"));

        verify(recipientService).create(any(RecipientCreateDTO.class));
    }

    // ==================== Tests pour GET /api/recipients/{id} ====================

    @Test
    @DisplayName("Should get recipient by id successfully")
    void testGetRecipientById_Success() throws Exception {
        // GIVEN
        when(recipientService.findById("recipient-1")).thenReturn(responseDTO);

        // WHEN & THEN
        mockMvc.perform(get("/api/recipients/recipient-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("recipient-1"))
                .andExpect(jsonPath("$.firstName").value("Ahmed"));

        verify(recipientService).findById("recipient-1");
    }

    @Test
    @DisplayName("Should return 404 when recipient not found")
    void testGetRecipientById_NotFound() throws Exception {
        // GIVEN
        when(recipientService.findById("recipient-999"))
                .thenThrow(new ResourceNotFoundException("Recipient", "id", "recipient-999"));

        // WHEN & THEN
        mockMvc.perform(get("/api/recipients/recipient-999"))
                .andExpect(status().isNotFound());

        verify(recipientService).findById("recipient-999");
    }

    // ==================== Tests pour GET /api/recipients ====================

    @Test
    @DisplayName("Should get all recipients successfully")
    void testGetAllRecipients_Success() throws Exception {
        // GIVEN
        List<RecipientResponseDTO> recipients = Arrays.asList(responseDTO);
        when(recipientService.findAll()).thenReturn(recipients);

        // WHEN & THEN
        mockMvc.perform(get("/api/recipients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(recipientService).findAll();
    }

    // ==================== Tests pour PUT /api/recipients/{id} ====================

    @Test
    @DisplayName("Should update recipient successfully")
    void testUpdateRecipient_Success() throws Exception {
        // GIVEN
        responseDTO.setFirstName("Hassan");
        when(recipientService.update(any(RecipientUpdateDTO.class))).thenReturn(responseDTO);

        // WHEN & THEN
        mockMvc.perform(put("/api/recipients/recipient-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("recipient-1"));

        verify(recipientService).update(any(RecipientUpdateDTO.class));
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent recipient")
    void testUpdateRecipient_NotFound() throws Exception {
        // GIVEN
        when(recipientService.update(any(RecipientUpdateDTO.class)))
                .thenThrow(new ResourceNotFoundException("Recipient", "id", "recipient-999"));

        // WHEN & THEN
        mockMvc.perform(put("/api/recipients/recipient-999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());

        verify(recipientService).update(any(RecipientUpdateDTO.class));
    }

    // ==================== Tests pour DELETE /api/recipients/{id} ====================

    @Test
    @DisplayName("Should delete recipient successfully")
    void testDeleteRecipient_Success() throws Exception {
        // GIVEN
        doNothing().when(recipientService).delete("recipient-1");

        // WHEN & THEN
        mockMvc.perform(delete("/api/recipients/recipient-1"))
                .andExpect(status().isNoContent());

        verify(recipientService).delete("recipient-1");
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent recipient")
    void testDeleteRecipient_NotFound() throws Exception {
        // GIVEN
        doThrow(new ResourceNotFoundException("Recipient", "id", "recipient-999"))
                .when(recipientService).delete("recipient-999");

        // WHEN & THEN
        mockMvc.perform(delete("/api/recipients/recipient-999"))
                .andExpect(status().isNotFound());

        verify(recipientService).delete("recipient-999");
    }

    // ==================== Tests pour GET /api/recipients/search ====================

    @Test
    @DisplayName("Should search recipients by name successfully")
    void testSearchRecipients_Success() throws Exception {
        // GIVEN
        List<RecipientResponseDTO> recipients = Arrays.asList(responseDTO);
        when(recipientService.searchByName("Ahmed")).thenReturn(recipients);

        // WHEN & THEN
        mockMvc.perform(get("/api/recipients/search")
                        .param("keyword", "Ahmed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(recipientService).searchByName("Ahmed");
    }

    // ==================== Tests pour GET /api/recipients/with-email ====================

    @Test
    @DisplayName("Should get recipients with email successfully")
    void testGetRecipientsWithEmail_Success() throws Exception {
        // GIVEN
        List<RecipientResponseDTO> recipients = Arrays.asList(responseDTO);
        when(recipientService.findWithEmail()).thenReturn(recipients);

        // WHEN & THEN
        mockMvc.perform(get("/api/recipients/with-email"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(recipientService).findWithEmail();
    }

    // ==================== Tests pour GET /api/recipients/without-email ====================

    @Test
    @DisplayName("Should get recipients without email successfully")
    void testGetRecipientsWithoutEmail_Success() throws Exception {
        // GIVEN
        List<RecipientResponseDTO> recipients = Arrays.asList(responseDTO);
        when(recipientService.findWithoutEmail()).thenReturn(recipients);

        // WHEN & THEN
        mockMvc.perform(get("/api/recipients/without-email"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(recipientService).findWithoutEmail();
    }
}
