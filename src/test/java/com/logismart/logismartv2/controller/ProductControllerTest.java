package com.logismart.logismartv2.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logismart.logismartv2.dto.product.ProductCreateDTO;
import com.logismart.logismartv2.dto.product.ProductResponseDTO;
import com.logismart.logismartv2.dto.product.ProductUpdateDTO;
import com.logismart.logismartv2.exception.DuplicateResourceException;
import com.logismart.logismartv2.exception.ResourceNotFoundException;
import com.logismart.logismartv2.service.ProductService;
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
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitaires pour ProductController
 * Utilise MockMvc pour tester les endpoints REST
 */
@WebMvcTest(ProductController.class)
@DisplayName("ProductController Unit Tests")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    private ProductResponseDTO responseDTO;
    private ProductCreateDTO createDTO;
    private ProductUpdateDTO updateDTO;

    @BeforeEach
    void setUp() {
        responseDTO = new ProductResponseDTO();
        responseDTO.setId("product-1");
        responseDTO.setName("iPhone 15");
        responseDTO.setCategory("Electronics");
        responseDTO.setPrice(BigDecimal.valueOf(999.99));
        responseDTO.setWeight(BigDecimal.valueOf(0.5));

        createDTO = new ProductCreateDTO();
        createDTO.setName("iPhone 15");
        createDTO.setCategory("Electronics");
        createDTO.setPrice(BigDecimal.valueOf(999.99));
        createDTO.setWeight(BigDecimal.valueOf(0.5));

        updateDTO = new ProductUpdateDTO();
        updateDTO.setId("product-1");
        updateDTO.setName("iPhone 15 Pro");
        updateDTO.setCategory("Electronics");
        updateDTO.setPrice(BigDecimal.valueOf(1199.99));
        updateDTO.setWeight(BigDecimal.valueOf(0.55));
    }

    // ==================== Tests pour POST /api/products ====================

    @Test
    @DisplayName("Should create product successfully and return 201")
    void testCreateProduct_Success() throws Exception {
        // GIVEN
        when(productService.create(any(ProductCreateDTO.class))).thenReturn(responseDTO);

        // WHEN & THEN
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("product-1"))
                .andExpect(jsonPath("$.name").value("iPhone 15"))
                .andExpect(jsonPath("$.category").value("Electronics"))
                .andExpect(jsonPath("$.price").value(999.99));

        verify(productService).create(any(ProductCreateDTO.class));
    }

    @Test
    @DisplayName("Should return 409 when creating product with duplicate name")
    void testCreateProduct_DuplicateName() throws Exception {
        // GIVEN
        when(productService.create(any(ProductCreateDTO.class)))
                .thenThrow(new DuplicateResourceException("Product", "name", createDTO.getName()));

        // WHEN & THEN
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isConflict());

        verify(productService).create(any(ProductCreateDTO.class));
    }

    // ==================== Tests pour GET /api/products/{id} ====================

    @Test
    @DisplayName("Should get product by id successfully and return 200")
    void testGetProductById_Success() throws Exception {
        // GIVEN
        when(productService.findById("product-1")).thenReturn(responseDTO);

        // WHEN & THEN
        mockMvc.perform(get("/api/products/product-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("product-1"))
                .andExpect(jsonPath("$.name").value("iPhone 15"))
                .andExpect(jsonPath("$.price").value(999.99));

        verify(productService).findById("product-1");
    }

    @Test
    @DisplayName("Should return 404 when product not found")
    void testGetProductById_NotFound() throws Exception {
        // GIVEN
        when(productService.findById("product-999"))
                .thenThrow(new ResourceNotFoundException("Product", "id", "product-999"));

        // WHEN & THEN
        mockMvc.perform(get("/api/products/product-999"))
                .andExpect(status().isNotFound());

        verify(productService).findById("product-999");
    }

    // ==================== Tests pour GET /api/products ====================

    @Test
    @DisplayName("Should get all products successfully and return 200")
    void testGetAllProducts_Success() throws Exception {
        // GIVEN
        ProductResponseDTO product2 = new ProductResponseDTO();
        product2.setId("product-2");
        product2.setName("Samsung Galaxy");
        product2.setCategory("Electronics");
        product2.setPrice(BigDecimal.valueOf(899.99));

        List<ProductResponseDTO> products = Arrays.asList(responseDTO, product2);
        when(productService.findAll()).thenReturn(products);

        // WHEN & THEN
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value("product-1"))
                .andExpect(jsonPath("$[1].id").value("product-2"));

        verify(productService).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no products exist")
    void testGetAllProducts_EmptyList() throws Exception {
        // GIVEN
        when(productService.findAll()).thenReturn(Arrays.asList());

        // WHEN & THEN
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(productService).findAll();
    }

    // ==================== Tests pour PUT /api/products/{id} ====================

    @Test
    @DisplayName("Should update product successfully and return 200")
    void testUpdateProduct_Success() throws Exception {
        // GIVEN
        responseDTO.setName("iPhone 15 Pro");
        responseDTO.setPrice(BigDecimal.valueOf(1199.99));
        when(productService.update(any(ProductUpdateDTO.class))).thenReturn(responseDTO);

        // WHEN & THEN
        mockMvc.perform(put("/api/products/product-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("product-1"))
                .andExpect(jsonPath("$.name").value("iPhone 15 Pro"))
                .andExpect(jsonPath("$.price").value(1199.99));

        verify(productService).update(any(ProductUpdateDTO.class));
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent product")
    void testUpdateProduct_NotFound() throws Exception {
        // GIVEN
        when(productService.update(any(ProductUpdateDTO.class)))
                .thenThrow(new ResourceNotFoundException("Product", "id", "product-999"));

        // WHEN & THEN
        mockMvc.perform(put("/api/products/product-999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());

        verify(productService).update(any(ProductUpdateDTO.class));
    }

    // ==================== Tests pour DELETE /api/products/{id} ====================

    @Test
    @DisplayName("Should delete product successfully and return 204")
    void testDeleteProduct_Success() throws Exception {
        // GIVEN
        doNothing().when(productService).delete("product-1");

        // WHEN & THEN
        mockMvc.perform(delete("/api/products/product-1"))
                .andExpect(status().isNoContent());

        verify(productService).delete("product-1");
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent product")
    void testDeleteProduct_NotFound() throws Exception {
        // GIVEN
        doThrow(new ResourceNotFoundException("Product", "id", "product-999"))
                .when(productService).delete("product-999");

        // WHEN & THEN
        mockMvc.perform(delete("/api/products/product-999"))
                .andExpect(status().isNotFound());

        verify(productService).delete("product-999");
    }

    // ==================== Tests pour GET /api/products/category/{category} ====================

    @Test
    @DisplayName("Should get products by category successfully")
    void testGetProductsByCategory_Success() throws Exception {
        // GIVEN
        List<ProductResponseDTO> products = Arrays.asList(responseDTO);
        when(productService.findByCategory("Electronics")).thenReturn(products);

        // WHEN & THEN
        mockMvc.perform(get("/api/products/category/Electronics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].category").value("Electronics"));

        verify(productService).findByCategory("Electronics");
    }

    // ==================== Tests pour GET /api/products/search ====================

    @Test
    @DisplayName("Should search products by name successfully")
    void testSearchProducts_Success() throws Exception {
        // GIVEN
        List<ProductResponseDTO> products = Arrays.asList(responseDTO);
        when(productService.searchByName("iPhone")).thenReturn(products);

        // WHEN & THEN
        mockMvc.perform(get("/api/products/search")
                        .param("keyword", "iPhone"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("iPhone 15"));

        verify(productService).searchByName("iPhone");
    }

    // ==================== Tests pour GET /api/products/price-range ====================

    @Test
    @DisplayName("Should get products by price range successfully")
    void testGetProductsByPriceRange_Success() throws Exception {
        // GIVEN
        List<ProductResponseDTO> products = Arrays.asList(responseDTO);
        when(productService.findByPriceRange(any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(products);

        // WHEN & THEN
        mockMvc.perform(get("/api/products/price-range")
                        .param("min", "500")
                        .param("max", "1500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(productService).findByPriceRange(any(BigDecimal.class), any(BigDecimal.class));
    }

    // ==================== Tests pour GET /api/products/categories ====================

    @Test
    @DisplayName("Should get all categories successfully")
    void testGetAllCategories_Success() throws Exception {
        // GIVEN
        List<String> categories = Arrays.asList("Electronics", "Clothing", "Books");
        when(productService.findAllCategories()).thenReturn(categories);

        // WHEN & THEN
        mockMvc.perform(get("/api/products/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0]").value("Electronics"));

        verify(productService).findAllCategories();
    }
}
