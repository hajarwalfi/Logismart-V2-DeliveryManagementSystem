package com.logismart.logismartv2.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logismart.logismartv2.dto.parcelproduct.ParcelProductCreateDTO;
import com.logismart.logismartv2.dto.parcelproduct.ParcelProductResponseDTO;
import com.logismart.logismartv2.dto.parcelproduct.ParcelProductUpdateDTO;
import com.logismart.logismartv2.exception.ResourceNotFoundException;
import com.logismart.logismartv2.service.ParcelProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitaires pour ParcelProductController
 * Utilise MockMvc pour tester les endpoints REST
 */
@WebMvcTest(ParcelProductController.class)
@DisplayName("ParcelProductController Unit Tests")
class ParcelProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ParcelProductService parcelProductService;

    private ParcelProductResponseDTO responseDTO;
    private ParcelProductCreateDTO createDTO;
    private ParcelProductUpdateDTO updateDTO;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        responseDTO = new ParcelProductResponseDTO();
        responseDTO.setId("pp-1");
        responseDTO.setParcelId("parcel-1");
        responseDTO.setProductId("product-1");
        responseDTO.setProductName("iPhone 15");
        responseDTO.setQuantity(2);
        responseDTO.setPrice(BigDecimal.valueOf(999.99));
        responseDTO.setFormattedUnitPrice("999.99 MAD");
        responseDTO.setTotalPrice(BigDecimal.valueOf(1999.98));
        responseDTO.setFormattedTotalPrice("1999.98 MAD");
        responseDTO.setAddedAt(now);
        responseDTO.setIsBulkItem(true);
        responseDTO.setLineItemSummary("2x iPhone 15 @ 999.99 MAD each = 1999.98 MAD total");

        createDTO = new ParcelProductCreateDTO();
        createDTO.setParcelId("parcel-1");
        createDTO.setProductId("product-1");
        createDTO.setQuantity(2);
        createDTO.setPrice(BigDecimal.valueOf(999.99));

        updateDTO = new ParcelProductUpdateDTO();
        updateDTO.setId("pp-1");
        updateDTO.setQuantity(3);
        updateDTO.setPrice(BigDecimal.valueOf(899.99));
    }

    // ==================== Tests pour POST /api/parcel-products ====================

    @Test
    @DisplayName("Should create parcel-product successfully and return 201")
    void testCreateParcelProduct_Success() throws Exception {
        // GIVEN
        when(parcelProductService.create(any(ParcelProductCreateDTO.class))).thenReturn(responseDTO);

        // WHEN & THEN
        mockMvc.perform(post("/api/parcel-products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("pp-1"))
                .andExpect(jsonPath("$.parcelId").value("parcel-1"))
                .andExpect(jsonPath("$.productId").value("product-1"))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.price").value(999.99));

        verify(parcelProductService).create(any(ParcelProductCreateDTO.class));
    }

    @Test
    @DisplayName("Should return 404 when creating parcel-product with non-existent parcel")
    void testCreateParcelProduct_ParcelNotFound() throws Exception {
        // GIVEN
        when(parcelProductService.create(any(ParcelProductCreateDTO.class)))
                .thenThrow(new ResourceNotFoundException("Parcel", "id", "parcel-999"));

        // WHEN & THEN
        mockMvc.perform(post("/api/parcel-products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isNotFound());

        verify(parcelProductService).create(any(ParcelProductCreateDTO.class));
    }

    @Test
    @DisplayName("Should return 404 when creating parcel-product with non-existent product")
    void testCreateParcelProduct_ProductNotFound() throws Exception {
        // GIVEN
        when(parcelProductService.create(any(ParcelProductCreateDTO.class)))
                .thenThrow(new ResourceNotFoundException("Product", "id", "product-999"));

        // WHEN & THEN
        mockMvc.perform(post("/api/parcel-products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isNotFound());

        verify(parcelProductService).create(any(ParcelProductCreateDTO.class));
    }

    // ==================== Tests pour GET /api/parcel-products/{id} ====================

    @Test
    @DisplayName("Should get parcel-product by id successfully and return 200")
    void testGetParcelProductById_Success() throws Exception {
        // GIVEN
        when(parcelProductService.findById("pp-1")).thenReturn(responseDTO);

        // WHEN & THEN
        mockMvc.perform(get("/api/parcel-products/pp-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("pp-1"))
                .andExpect(jsonPath("$.parcelId").value("parcel-1"))
                .andExpect(jsonPath("$.productName").value("iPhone 15"))
                .andExpect(jsonPath("$.quantity").value(2));

        verify(parcelProductService).findById("pp-1");
    }

    @Test
    @DisplayName("Should return 404 when parcel-product not found")
    void testGetParcelProductById_NotFound() throws Exception {
        // GIVEN
        when(parcelProductService.findById("pp-999"))
                .thenThrow(new ResourceNotFoundException("ParcelProduct", "id", "pp-999"));

        // WHEN & THEN
        mockMvc.perform(get("/api/parcel-products/pp-999"))
                .andExpect(status().isNotFound());

        verify(parcelProductService).findById("pp-999");
    }

    // ==================== Tests pour GET /api/parcel-products ====================

    @Test
    @DisplayName("Should get all parcel-products successfully and return 200")
    void testGetAllParcelProducts_Success() throws Exception {
        // GIVEN
        ParcelProductResponseDTO response2 = new ParcelProductResponseDTO();
        response2.setId("pp-2");
        response2.setParcelId("parcel-1");
        response2.setProductId("product-2");
        response2.setProductName("Samsung Galaxy");
        response2.setQuantity(1);
        response2.setPrice(BigDecimal.valueOf(899.99));

        List<ParcelProductResponseDTO> responses = Arrays.asList(responseDTO, response2);
        when(parcelProductService.findAll()).thenReturn(responses);

        // WHEN & THEN
        mockMvc.perform(get("/api/parcel-products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value("pp-1"))
                .andExpect(jsonPath("$[1].id").value("pp-2"));

        verify(parcelProductService).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no parcel-products exist")
    void testGetAllParcelProducts_EmptyList() throws Exception {
        // GIVEN
        when(parcelProductService.findAll()).thenReturn(Arrays.asList());

        // WHEN & THEN
        mockMvc.perform(get("/api/parcel-products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(parcelProductService).findAll();
    }

    // ==================== Tests pour PUT /api/parcel-products/{id} ====================

    @Test
    @DisplayName("Should update parcel-product successfully and return 200")
    void testUpdateParcelProduct_Success() throws Exception {
        // GIVEN
        responseDTO.setQuantity(3);
        responseDTO.setPrice(BigDecimal.valueOf(899.99));
        when(parcelProductService.update(any(ParcelProductUpdateDTO.class))).thenReturn(responseDTO);

        // WHEN & THEN
        mockMvc.perform(put("/api/parcel-products/pp-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("pp-1"))
                .andExpect(jsonPath("$.quantity").value(3))
                .andExpect(jsonPath("$.price").value(899.99));

        verify(parcelProductService).update(any(ParcelProductUpdateDTO.class));
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent parcel-product")
    void testUpdateParcelProduct_NotFound() throws Exception {
        // GIVEN
        when(parcelProductService.update(any(ParcelProductUpdateDTO.class)))
                .thenThrow(new ResourceNotFoundException("ParcelProduct", "id", "pp-999"));

        // WHEN & THEN
        mockMvc.perform(put("/api/parcel-products/pp-999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());

        verify(parcelProductService).update(any(ParcelProductUpdateDTO.class));
    }

    // ==================== Tests pour DELETE /api/parcel-products/{id} ====================

    @Test
    @DisplayName("Should delete parcel-product successfully and return 204")
    void testDeleteParcelProduct_Success() throws Exception {
        // GIVEN
        doNothing().when(parcelProductService).delete("pp-1");

        // WHEN & THEN
        mockMvc.perform(delete("/api/parcel-products/pp-1"))
                .andExpect(status().isNoContent());

        verify(parcelProductService).delete("pp-1");
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent parcel-product")
    void testDeleteParcelProduct_NotFound() throws Exception {
        // GIVEN
        doThrow(new ResourceNotFoundException("ParcelProduct", "id", "pp-999"))
                .when(parcelProductService).delete("pp-999");

        // WHEN & THEN
        mockMvc.perform(delete("/api/parcel-products/pp-999"))
                .andExpect(status().isNotFound());

        verify(parcelProductService).delete("pp-999");
    }

    // ==================== Tests pour GET /api/parcel-products/parcel/{parcelId} ====================

    @Test
    @DisplayName("Should get products by parcel id successfully")
    void testGetProductsByParcelId_Success() throws Exception {
        // GIVEN
        List<ParcelProductResponseDTO> responses = Arrays.asList(responseDTO);
        when(parcelProductService.findByParcelId("parcel-1")).thenReturn(responses);

        // WHEN & THEN
        mockMvc.perform(get("/api/parcel-products/parcel/parcel-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].parcelId").value("parcel-1"));

        verify(parcelProductService).findByParcelId("parcel-1");
    }

    @Test
    @DisplayName("Should return 404 when parcel not found for products query")
    void testGetProductsByParcelId_ParcelNotFound() throws Exception {
        // GIVEN
        when(parcelProductService.findByParcelId("parcel-999"))
                .thenThrow(new ResourceNotFoundException("Parcel", "id", "parcel-999"));

        // WHEN & THEN
        mockMvc.perform(get("/api/parcel-products/parcel/parcel-999"))
                .andExpect(status().isNotFound());

        verify(parcelProductService).findByParcelId("parcel-999");
    }

    // ==================== Tests pour GET /api/parcel-products/parcel/{parcelId}/total-value ====================

    @Test
    @DisplayName("Should calculate parcel total value successfully")
    void testCalculateParcelTotalValue_Success() throws Exception {
        // GIVEN
        when(parcelProductService.calculateParcelTotalValue("parcel-1"))
                .thenReturn(BigDecimal.valueOf(1999.98));

        // WHEN & THEN
        mockMvc.perform(get("/api/parcel-products/parcel/parcel-1/total-value"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(1999.98));

        verify(parcelProductService).calculateParcelTotalValue("parcel-1");
    }

    @Test
    @DisplayName("Should return 404 when calculating total value for non-existent parcel")
    void testCalculateParcelTotalValue_ParcelNotFound() throws Exception {
        // GIVEN
        when(parcelProductService.calculateParcelTotalValue("parcel-999"))
                .thenThrow(new ResourceNotFoundException("Parcel", "id", "parcel-999"));

        // WHEN & THEN
        mockMvc.perform(get("/api/parcel-products/parcel/parcel-999/total-value"))
                .andExpect(status().isNotFound());

        verify(parcelProductService).calculateParcelTotalValue("parcel-999");
    }

    // ==================== Tests pour GET /api/parcel-products/parcel/{parcelId}/count ====================

    @Test
    @DisplayName("Should count products in parcel successfully")
    void testCountProductsInParcel_Success() throws Exception {
        // GIVEN
        when(parcelProductService.countProductsInParcel("parcel-1")).thenReturn(2L);

        // WHEN & THEN
        mockMvc.perform(get("/api/parcel-products/parcel/parcel-1/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(2));

        verify(parcelProductService).countProductsInParcel("parcel-1");
    }

    @Test
    @DisplayName("Should return 404 when counting products for non-existent parcel")
    void testCountProductsInParcel_ParcelNotFound() throws Exception {
        // GIVEN
        when(parcelProductService.countProductsInParcel("parcel-999"))
                .thenThrow(new ResourceNotFoundException("Parcel", "id", "parcel-999"));

        // WHEN & THEN
        mockMvc.perform(get("/api/parcel-products/parcel/parcel-999/count"))
                .andExpect(status().isNotFound());

        verify(parcelProductService).countProductsInParcel("parcel-999");
    }

    // ==================== Tests pour GET /api/parcel-products/product/{productId} ====================

    @Test
    @DisplayName("Should get parcels by product id successfully")
    void testGetParcelsByProductId_Success() throws Exception {
        // GIVEN
        List<ParcelProductResponseDTO> responses = Arrays.asList(responseDTO);
        when(parcelProductService.findByProductId("product-1")).thenReturn(responses);

        // WHEN & THEN
        mockMvc.perform(get("/api/parcel-products/product/product-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].productId").value("product-1"));

        verify(parcelProductService).findByProductId("product-1");
    }

    @Test
    @DisplayName("Should return 404 when product not found for parcels query")
    void testGetParcelsByProductId_ProductNotFound() throws Exception {
        // GIVEN
        when(parcelProductService.findByProductId("product-999"))
                .thenThrow(new ResourceNotFoundException("Product", "id", "product-999"));

        // WHEN & THEN
        mockMvc.perform(get("/api/parcel-products/product/product-999"))
                .andExpect(status().isNotFound());

        verify(parcelProductService).findByProductId("product-999");
    }

    // ==================== Tests pour GET /api/parcel-products/product/{productId}/total-quantity ====================

    @Test
    @DisplayName("Should calculate total quantity shipped successfully")
    void testCalculateTotalQuantityShipped_Success() throws Exception {
        // GIVEN
        when(parcelProductService.calculateTotalQuantityShipped("product-1")).thenReturn(50L);

        // WHEN & THEN
        mockMvc.perform(get("/api/parcel-products/product/product-1/total-quantity"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(50));

        verify(parcelProductService).calculateTotalQuantityShipped("product-1");
    }

    @Test
    @DisplayName("Should return 404 when calculating quantity for non-existent product")
    void testCalculateTotalQuantityShipped_ProductNotFound() throws Exception {
        // GIVEN
        when(parcelProductService.calculateTotalQuantityShipped("product-999"))
                .thenThrow(new ResourceNotFoundException("Product", "id", "product-999"));

        // WHEN & THEN
        mockMvc.perform(get("/api/parcel-products/product/product-999/total-quantity"))
                .andExpect(status().isNotFound());

        verify(parcelProductService).calculateTotalQuantityShipped("product-999");
    }

    // ==================== Tests pour GET /api/parcel-products/product/{productId}/revenue ====================

    @Test
    @DisplayName("Should calculate product revenue successfully")
    void testCalculateProductRevenue_Success() throws Exception {
        // GIVEN
        when(parcelProductService.calculateProductRevenue("product-1"))
                .thenReturn(BigDecimal.valueOf(49999.50));

        // WHEN & THEN
        mockMvc.perform(get("/api/parcel-products/product/product-1/revenue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(49999.50));

        verify(parcelProductService).calculateProductRevenue("product-1");
    }

    @Test
    @DisplayName("Should return 404 when calculating revenue for non-existent product")
    void testCalculateProductRevenue_ProductNotFound() throws Exception {
        // GIVEN
        when(parcelProductService.calculateProductRevenue("product-999"))
                .thenThrow(new ResourceNotFoundException("Product", "id", "product-999"));

        // WHEN & THEN
        mockMvc.perform(get("/api/parcel-products/product/product-999/revenue"))
                .andExpect(status().isNotFound());

        verify(parcelProductService).calculateProductRevenue("product-999");
    }

    // ==================== Tests pour GET /api/parcel-products/product/{productId}/average-price ====================

    @Test
    @DisplayName("Should calculate average price successfully")
    void testCalculateAveragePrice_Success() throws Exception {
        // GIVEN
        when(parcelProductService.calculateAveragePrice("product-1"))
                .thenReturn(BigDecimal.valueOf(999.99));

        // WHEN & THEN
        mockMvc.perform(get("/api/parcel-products/product/product-1/average-price"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(999.99));

        verify(parcelProductService).calculateAveragePrice("product-1");
    }

    @Test
    @DisplayName("Should return 404 when calculating average price for non-existent product")
    void testCalculateAveragePrice_ProductNotFound() throws Exception {
        // GIVEN
        when(parcelProductService.calculateAveragePrice("product-999"))
                .thenThrow(new ResourceNotFoundException("Product", "id", "product-999"));

        // WHEN & THEN
        mockMvc.perform(get("/api/parcel-products/product/product-999/average-price"))
                .andExpect(status().isNotFound());

        verify(parcelProductService).calculateAveragePrice("product-999");
    }

    // ==================== Tests pour GET /api/parcel-products/analytics/bulk-orders ====================

    @Test
    @DisplayName("Should get bulk orders successfully")
    void testGetBulkOrders_Success() throws Exception {
        // GIVEN
        List<ParcelProductResponseDTO> responses = Arrays.asList(responseDTO);
        when(parcelProductService.findBulkOrders(5)).thenReturn(responses);

        // WHEN & THEN
        mockMvc.perform(get("/api/parcel-products/analytics/bulk-orders")
                        .param("minQuantity", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].quantity").value(2));

        verify(parcelProductService).findBulkOrders(5);
    }

    @Test
    @DisplayName("Should return empty list when no bulk orders found")
    void testGetBulkOrders_EmptyList() throws Exception {
        // GIVEN
        when(parcelProductService.findBulkOrders(100)).thenReturn(Arrays.asList());

        // WHEN & THEN
        mockMvc.perform(get("/api/parcel-products/analytics/bulk-orders")
                        .param("minQuantity", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(parcelProductService).findBulkOrders(100);
    }

    // ==================== Tests pour GET /api/parcel-products/analytics/discounted ====================

    @Test
    @DisplayName("Should get discounted products successfully")
    void testGetDiscountedProducts_Success() throws Exception {
        // GIVEN
        List<ParcelProductResponseDTO> responses = Arrays.asList(responseDTO);
        when(parcelProductService.findDiscountedProducts()).thenReturn(responses);

        // WHEN & THEN
        mockMvc.perform(get("/api/parcel-products/analytics/discounted"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(parcelProductService).findDiscountedProducts();
    }

    @Test
    @DisplayName("Should return empty list when no discounted products found")
    void testGetDiscountedProducts_EmptyList() throws Exception {
        // GIVEN
        when(parcelProductService.findDiscountedProducts()).thenReturn(Arrays.asList());

        // WHEN & THEN
        mockMvc.perform(get("/api/parcel-products/analytics/discounted"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(parcelProductService).findDiscountedProducts();
    }

    // ==================== Tests pour GET /api/parcel-products/analytics/total-revenue ====================

    @Test
    @DisplayName("Should get total revenue successfully")
    void testGetTotalRevenue_Success() throws Exception {
        // GIVEN
        when(parcelProductService.calculateTotalRevenue())
                .thenReturn(BigDecimal.valueOf(500000.00));

        // WHEN & THEN
        mockMvc.perform(get("/api/parcel-products/analytics/total-revenue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(500000.00));

        verify(parcelProductService).calculateTotalRevenue();
    }

    // ==================== Tests pour GET /api/parcel-products/analytics/total-items ====================

    @Test
    @DisplayName("Should get total items shipped successfully")
    void testGetTotalItemsShipped_Success() throws Exception {
        // GIVEN
        when(parcelProductService.calculateTotalItemsShipped()).thenReturn(1000L);

        // WHEN & THEN
        mockMvc.perform(get("/api/parcel-products/analytics/total-items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(1000));

        verify(parcelProductService).calculateTotalItemsShipped();
    }

    // ==================== Tests pour GET /api/parcel-products/analytics/distinct-products/count ====================

    @Test
    @DisplayName("Should count distinct products shipped successfully")
    void testCountDistinctProductsShipped_Success() throws Exception {
        // GIVEN
        when(parcelProductService.countDistinctProductsShipped()).thenReturn(150L);

        // WHEN & THEN
        mockMvc.perform(get("/api/parcel-products/analytics/distinct-products/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(150));

        verify(parcelProductService).countDistinctProductsShipped();
    }
}
