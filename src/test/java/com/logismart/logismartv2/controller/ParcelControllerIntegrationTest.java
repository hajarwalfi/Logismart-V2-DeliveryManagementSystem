package com.logismart.logismartv2.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logismart.logismartv2.dto.parcel.ParcelCreateDTO;
import com.logismart.logismartv2.dto.parcel.ParcelProductItemDTO;
import com.logismart.logismartv2.dto.parcel.ParcelResponseDTO;
import com.logismart.logismartv2.entity.*;
import com.logismart.logismartv2.repository.*;
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

import java.math.BigDecimal;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration pour ParcelController
 * Ces tests vérifient le flux complet avec toutes les relations (Sender, Recipient, Product)
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("ParcelController Integration Tests")
class ParcelControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ParcelRepository parcelRepository;

    @Autowired
    private SenderClientRepository senderClientRepository;

    @Autowired
    private RecipientRepository recipientRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ParcelProductRepository parcelProductRepository;

    private SenderClient testSender;
    private Recipient testRecipient;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        // Nettoyer la base de données
        parcelProductRepository.deleteAll();
        parcelRepository.deleteAll();
        productRepository.deleteAll();
        recipientRepository.deleteAll();
        senderClientRepository.deleteAll();

        // Créer les entités de test nécessaires (ne pas définir l'ID, JPA le génère automatiquement)
        testSender = new SenderClient();
        testSender.setFirstName("Ahmed");
        testSender.setLastName("Benali");
        testSender.setPhone("0612345678");
        testSender.setEmail("ahmed.benali@example.com");
        testSender.setAddress("123 Rue Mohammed V, Casablanca");
        testSender = senderClientRepository.save(testSender); // Récupérer l'entité avec l'ID généré

        testRecipient = new Recipient();
        testRecipient.setFirstName("Fatima");
        testRecipient.setLastName("Alami");
        testRecipient.setPhone("0698765432");
        testRecipient.setEmail("fatima.alami@example.com");
        testRecipient.setAddress("456 Avenue Hassan II, Rabat");
        testRecipient = recipientRepository.save(testRecipient); // Récupérer l'entité avec l'ID généré

        testProduct = new Product();
        testProduct.setName("Laptop Dell XPS 15");
        testProduct.setCategory("Electronics");
        testProduct.setWeight(new BigDecimal("2.5"));
        testProduct.setPrice(new BigDecimal("12000.00"));
        testProduct = productRepository.save(testProduct); // Récupérer l'entité avec l'ID généré
    }

    @Test
    @DisplayName("Integration Test: Should create parcel with all relationships (full flow)")
    void testCreateParcel_WithAllRelationships_Success() throws Exception {
        // GIVEN - Préparer un colis avec produits
        ParcelProductItemDTO productItem = new ParcelProductItemDTO();
        productItem.setProductId(testProduct.getId());
        productItem.setQuantity(2);
        productItem.setPrice(new BigDecimal("12000.00"));

        ParcelCreateDTO createDTO = new ParcelCreateDTO();
        createDTO.setDescription("Livraison urgente de laptops");
        createDTO.setWeight(new BigDecimal("5.0"));
        createDTO.setPriority(ParcelPriority.URGENT);
        createDTO.setDestinationCity("Rabat");
        createDTO.setSenderClientId(testSender.getId());
        createDTO.setRecipientId(testRecipient.getId());
        createDTO.setProducts(Arrays.asList(productItem));

        // WHEN - Créer le colis via l'API
        MvcResult createResult = mockMvc.perform(post("/api/parcels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.description").value("Livraison urgente de laptops"))
                .andExpect(jsonPath("$.weight").value(5.0))
                .andExpect(jsonPath("$.priority").value("URGENT"))
                .andExpect(jsonPath("$.destinationCity").value("Rabat"))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.senderClientName").value("Ahmed Benali"))
                .andExpect(jsonPath("$.recipientName").value("Fatima Alami"))
                .andReturn();

        // Extraire l'ID du colis créé
        String responseBody = createResult.getResponse().getContentAsString();
        ParcelResponseDTO createdParcel = objectMapper.readValue(responseBody, ParcelResponseDTO.class);
        String parcelId = createdParcel.getId();

        // THEN - Vérifier que le colis existe dans la base de données
        assertThat(parcelRepository.findById(parcelId)).isPresent();
        assertThat(parcelRepository.count()).isEqualTo(1);

        // AND - Vérifier que les associations produits ont été créées
        assertThat(parcelProductRepository.count()).isEqualTo(1);

        // AND - Récupérer le colis par son ID
        mockMvc.perform(get("/api/parcels/" + parcelId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(parcelId))
                .andExpect(jsonPath("$.description").value("Livraison urgente de laptops"))
                .andExpect(jsonPath("$.senderClientId").value(testSender.getId()))
                .andExpect(jsonPath("$.senderClientName").value("Ahmed Benali"))
                .andExpect(jsonPath("$.recipientId").value(testRecipient.getId()))
                .andExpect(jsonPath("$.recipientName").value("Fatima Alami"));
    }

    @Test
    @DisplayName("Integration Test: Should return 404 when creating parcel with non-existent sender")
    void testCreateParcel_WithNonExistentSender_ShouldFail() throws Exception {
        // GIVEN - Préparer un colis avec un sender inexistant
        ParcelProductItemDTO productItem = new ParcelProductItemDTO();
        productItem.setProductId(testProduct.getId());
        productItem.setQuantity(1);
        productItem.setPrice(new BigDecimal("12000.00"));

        ParcelCreateDTO createDTO = new ParcelCreateDTO();
        createDTO.setDescription("Test colis");
        createDTO.setWeight(new BigDecimal("2.5"));
        createDTO.setPriority(ParcelPriority.NORMAL);
        createDTO.setDestinationCity("Marrakech");
        createDTO.setSenderClientId("non-existent-sender-id"); // ID invalide
        createDTO.setRecipientId(testRecipient.getId());
        createDTO.setProducts(Arrays.asList(productItem));

        // WHEN & THEN - Devrait retourner 404 Not Found
        mockMvc.perform(post("/api/parcels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("SenderClient not found with id : 'non-existent-sender-id'"));

        // AND - Vérifier qu'aucun colis n'a été créé
        assertThat(parcelRepository.count()).isEqualTo(0);
        assertThat(parcelProductRepository.count()).isEqualTo(0);
    }
}
