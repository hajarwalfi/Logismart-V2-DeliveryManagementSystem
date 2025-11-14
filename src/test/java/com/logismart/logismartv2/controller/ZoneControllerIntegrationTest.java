package com.logismart.logismartv2.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logismart.logismartv2.dto.zone.ZoneCreateDTO;
import com.logismart.logismartv2.dto.zone.ZoneResponseDTO;
import com.logismart.logismartv2.repository.ZoneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration pour ZoneController
 * Ces tests vérifient le flux complet: Controller -> Service -> Repository -> Database
 * Utilise une base de données H2 en mémoire
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("ZoneController Integration Tests")
class ZoneControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ZoneRepository zoneRepository;

    @BeforeEach
    void setUp() {
        // Nettoyer la base de données avant chaque test
        zoneRepository.deleteAll();
    }

    @Test
    @DisplayName("Integration Test: Should create zone and retrieve it by ID (full flow)")
    void testCreateAndRetrieveZone_FullFlow() throws Exception {
        // GIVEN - Préparer les données de création
        ZoneCreateDTO createDTO = new ZoneCreateDTO();
        createDTO.setName("Zone Paris Centre");
        createDTO.setPostalCode("75001");

        // WHEN - Créer la zone via l'API REST
        MvcResult createResult = mockMvc.perform(post("/api/zones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Zone Paris Centre"))
                .andExpect(jsonPath("$.postalCode").value("75001"))
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        // Extraire l'ID de la zone créée
        String responseBody = createResult.getResponse().getContentAsString();
        ZoneResponseDTO createdZone = objectMapper.readValue(responseBody, ZoneResponseDTO.class);
        String zoneId = createdZone.getId();

        // THEN - Vérifier que la zone existe dans la base de données
        assertThat(zoneRepository.findById(zoneId)).isPresent();
        assertThat(zoneRepository.count()).isEqualTo(1);

        // AND - Récupérer la zone par son ID via l'API
        mockMvc.perform(get("/api/zones/" + zoneId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(zoneId))
                .andExpect(jsonPath("$.name").value("Zone Paris Centre"))
                .andExpect(jsonPath("$.postalCode").value("75001"));

        // AND - Vérifier que la zone apparaît dans la liste de toutes les zones
        mockMvc.perform(get("/api/zones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(zoneId));
    }

    @Test
    @DisplayName("Integration Test: Should prevent duplicate zone creation and return 409 Conflict")
    void testCreateDuplicateZone_ShouldReturnConflict() throws Exception {
        // GIVEN - Créer une première zone
        ZoneCreateDTO firstZone = new ZoneCreateDTO();
        firstZone.setName("Zone Lyon");
        firstZone.setPostalCode("69001");

        mockMvc.perform(post("/api/zones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstZone)))
                .andExpect(status().isCreated());

        // Vérifier que la zone existe dans la base
        assertThat(zoneRepository.count()).isEqualTo(1);

        // WHEN - Tenter de créer une zone avec le même nom
        ZoneCreateDTO duplicateName = new ZoneCreateDTO();
        duplicateName.setName("Zone Lyon"); // Même nom
        duplicateName.setPostalCode("69002"); // Code postal différent

        // THEN - Devrait retourner 409 Conflict
        mockMvc.perform(post("/api/zones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateName)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Zone with name 'Zone Lyon' already exists"));

        // AND - Vérifier qu'aucune zone supplémentaire n'a été créée
        assertThat(zoneRepository.count()).isEqualTo(1);

        // WHEN - Tenter de créer une zone avec le même code postal
        ZoneCreateDTO duplicatePostalCode = new ZoneCreateDTO();
        duplicatePostalCode.setName("Zone Lyon 2");
        duplicatePostalCode.setPostalCode("69001"); // Même code postal

        // THEN - Devrait aussi retourner 409 Conflict
        mockMvc.perform(post("/api/zones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicatePostalCode)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Zone with postalCode '69001' already exists"));

        // AND - Confirmer qu'il n'y a toujours qu'une seule zone
        assertThat(zoneRepository.count()).isEqualTo(1);
    }
}
