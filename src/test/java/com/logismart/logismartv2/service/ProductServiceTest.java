package com.logismart.logismartv2.service;

import com.logismart.logismartv2.dto.product.ProductCreateDTO;
import com.logismart.logismartv2.dto.product.ProductResponseDTO;
import com.logismart.logismartv2.dto.product.ProductUpdateDTO;
import com.logismart.logismartv2.entity.Product;
import com.logismart.logismartv2.exception.DuplicateResourceException;
import com.logismart.logismartv2.exception.ResourceNotFoundException;
import com.logismart.logismartv2.mapper.ProductMapper;
import com.logismart.logismartv2.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private ProductCreateDTO createDTO;
    private ProductUpdateDTO updateDTO;
    private ProductResponseDTO responseDTO;

    @BeforeEach
    void setUp() {

        product = new Product();
        product.setId("1");
        product.setName("iPhone 15");
        product.setCategory("Electronics");
        product.setWeight(new BigDecimal("0.20"));
        product.setPrice(new BigDecimal("999.00"));

        createDTO = new ProductCreateDTO();
        createDTO.setName("iPhone 15");
        createDTO.setCategory("Electronics");
        createDTO.setWeight(new BigDecimal("0.20"));
        createDTO.setPrice(new BigDecimal("999.00"));

        updateDTO = new ProductUpdateDTO();
        updateDTO.setId("1");
        updateDTO.setName("iPhone 15 Pro");
        updateDTO.setCategory("Electronics");
        updateDTO.setWeight(new BigDecimal("0.25"));
        updateDTO.setPrice(new BigDecimal("1099.00"));

        responseDTO = new ProductResponseDTO();
        responseDTO.setId("1");
        responseDTO.setName("iPhone 15");
        responseDTO.setCategory("Electronics");
        responseDTO.setWeight(new BigDecimal("0.20"));
        responseDTO.setPrice(new BigDecimal("999.00"));
    }


    @Test
    @DisplayName("Should create product successfully when name is unique")
    void testCreate_Success() {
        // GIVEN - Préparer les données
        when(productRepository.existsByName(createDTO.getName())).thenReturn(false);
        when(productMapper.toEntity(createDTO)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponseDTO(product)).thenReturn(responseDTO);

        // WHEN - Exécuter la méthode
        ProductResponseDTO result = productService.create(createDTO);

        // THEN - Vérifier les résultats
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("iPhone 15");
        assertThat(result.getCategory()).isEqualTo("Electronics");

        // Vérifier que les méthodes mockées ont été appelées
        verify(productRepository).existsByName(createDTO.getName());
        verify(productMapper).toEntity(createDTO);
        verify(productRepository).save(product);
        verify(productMapper).toResponseDTO(product);
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when creating product with existing name")
    void testCreate_ThrowsException_WhenNameAlreadyExists() {
        // GIVEN
        when(productRepository.existsByName(createDTO.getName())).thenReturn(true);

        // WHEN & THEN - Vérifier qu'une exception est lancée
        assertThatThrownBy(() -> productService.create(createDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Product")
                .hasMessageContaining("name");

        // Vérifier que save() n'a jamais été appelé
        verify(productRepository).existsByName(createDTO.getName());
        verify(productRepository, never()).save(any());
    }

    // ==================== Tests pour findById() ====================

    @Test
    @DisplayName("Should find product by id successfully when product exists")
    void testFindById_Success() {
        // GIVEN
        when(productRepository.findById("1")).thenReturn(Optional.of(product));
        when(productMapper.toResponseDTO(product)).thenReturn(responseDTO);

        // WHEN
        ProductResponseDTO result = productService.findById("1");

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("1");
        assertThat(result.getName()).isEqualTo("iPhone 15");

        verify(productRepository).findById("1");
        verify(productMapper).toResponseDTO(product);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when product not found by id")
    void testFindById_ThrowsException_WhenProductNotFound() {
        // GIVEN
        when(productRepository.findById("999")).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> productService.findById("999"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product")
                .hasMessageContaining("id")
                .hasMessageContaining("999");

        verify(productRepository).findById("999");
        verify(productMapper, never()).toResponseDTO(any());
    }

    // ==================== Tests pour findAll() ====================

    @Test
    @DisplayName("Should return all products successfully")
    void testFindAll_Success() {
        // GIVEN
        Product product2 = new Product();
        product2.setId("2");
        product2.setName("Samsung Galaxy S24");

        ProductResponseDTO responseDTO2 = new ProductResponseDTO();
        responseDTO2.setId("2");
        responseDTO2.setName("Samsung Galaxy S24");

        List<Product> products = Arrays.asList(product, product2);
        List<ProductResponseDTO> responseDTOs = Arrays.asList(responseDTO, responseDTO2);

        when(productRepository.findAll()).thenReturn(products);
        when(productMapper.toResponseDTOList(products)).thenReturn(responseDTOs);

        // WHEN
        List<ProductResponseDTO> result = productService.findAll();

        // THEN
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("iPhone 15");
        assertThat(result.get(1).getName()).isEqualTo("Samsung Galaxy S24");

        verify(productRepository).findAll();
        verify(productMapper).toResponseDTOList(products);
    }

    // ==================== Tests pour update() ====================

    @Test
    @DisplayName("Should update product successfully when product exists and name is unique")
    void testUpdate_Success() {
        // GIVEN
        when(productRepository.findById(updateDTO.getId())).thenReturn(Optional.of(product));
        when(productRepository.existsByName(updateDTO.getName())).thenReturn(false);
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponseDTO(product)).thenReturn(responseDTO);

        // WHEN
        ProductResponseDTO result = productService.update(updateDTO);

        // THEN
        assertThat(result).isNotNull();

        verify(productRepository).findById(updateDTO.getId());
        verify(productMapper).updateEntityFromDTO(updateDTO, product);
        verify(productRepository).save(product);
        verify(productMapper).toResponseDTO(product);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent product")
    void testUpdate_ThrowsException_WhenProductNotFound() {
        // GIVEN
        when(productRepository.findById(updateDTO.getId())).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> productService.update(updateDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product")
                .hasMessageContaining("id");

        verify(productRepository).findById(updateDTO.getId());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when updating product with existing name")
    void testUpdate_ThrowsException_WhenNameAlreadyExists() {
        // GIVEN - Le produit existe avec un nom différent
        product.setName("Old Name");
        updateDTO.setName("Existing Name");

        when(productRepository.findById(updateDTO.getId())).thenReturn(Optional.of(product));
        when(productRepository.existsByName(updateDTO.getName())).thenReturn(true);

        // WHEN & THEN
        assertThatThrownBy(() -> productService.update(updateDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Product")
                .hasMessageContaining("name");

        verify(productRepository).findById(updateDTO.getId());
        verify(productRepository).existsByName(updateDTO.getName());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update product successfully when keeping the same name")
    void testUpdate_Success_WhenKeepingSameName() {
        // GIVEN - Le produit garde le même nom
        product.setName("iPhone 15");
        updateDTO.setName("iPhone 15");

        when(productRepository.findById(updateDTO.getId())).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponseDTO(product)).thenReturn(responseDTO);

        // WHEN
        ProductResponseDTO result = productService.update(updateDTO);

        // THEN
        assertThat(result).isNotNull();

        verify(productRepository).findById(updateDTO.getId());
        // existsByName ne doit PAS être appelé si le nom n'a pas changé
        verify(productRepository, never()).existsByName(anyString());
        verify(productRepository).save(product);
    }

    // ==================== Tests pour delete() ====================

    @Test
    @DisplayName("Should delete product successfully when product exists")
    void testDelete_Success() {
        // GIVEN
        when(productRepository.existsById("1")).thenReturn(true);
        doNothing().when(productRepository).deleteById("1");

        // WHEN
        productService.delete("1");

        // THEN
        verify(productRepository).existsById("1");
        verify(productRepository).deleteById("1");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent product")
    void testDelete_ThrowsException_WhenProductNotFound() {
        // GIVEN
        when(productRepository.existsById("999")).thenReturn(false);

        // WHEN & THEN
        assertThatThrownBy(() -> productService.delete("999"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product")
                .hasMessageContaining("id")
                .hasMessageContaining("999");

        verify(productRepository).existsById("999");
        verify(productRepository, never()).deleteById(anyString());
    }

    // ==================== Tests pour findByCategory() ====================

    @Test
    @DisplayName("Should find products by category successfully")
    void testFindByCategory_Success() {
        // GIVEN
        List<Product> products = Arrays.asList(product);
        List<ProductResponseDTO> responseDTOs = Arrays.asList(responseDTO);

        when(productRepository.findByCategory("Electronics")).thenReturn(products);
        when(productMapper.toResponseDTOList(products)).thenReturn(responseDTOs);

        // WHEN
        List<ProductResponseDTO> result = productService.findByCategory("Electronics");

        // THEN
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory()).isEqualTo("Electronics");

        verify(productRepository).findByCategory("Electronics");
        verify(productMapper).toResponseDTOList(products);
    }

    // ==================== Tests pour searchByName() ====================

    @Test
    @DisplayName("Should search products by name keyword successfully")
    void testSearchByName_Success() {
        // GIVEN
        List<Product> products = Arrays.asList(product);
        List<ProductResponseDTO> responseDTOs = Arrays.asList(responseDTO);

        when(productRepository.findByNameContainingIgnoreCase("iphone")).thenReturn(products);
        when(productMapper.toResponseDTOList(products)).thenReturn(responseDTOs);

        // WHEN
        List<ProductResponseDTO> result = productService.searchByName("iphone");

        // THEN
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).containsIgnoringCase("iphone");

        verify(productRepository).findByNameContainingIgnoreCase("iphone");
        verify(productMapper).toResponseDTOList(products);
    }

    // ==================== Tests pour findByPriceRange() ====================

    @Test
    @DisplayName("Should find products by price range successfully")
    void testFindByPriceRange_Success() {
        // GIVEN
        BigDecimal minPrice = new BigDecimal("500.00");
        BigDecimal maxPrice = new BigDecimal("1500.00");
        List<Product> products = Arrays.asList(product);
        List<ProductResponseDTO> responseDTOs = Arrays.asList(responseDTO);

        when(productRepository.findByPriceRange(minPrice, maxPrice)).thenReturn(products);
        when(productMapper.toResponseDTOList(products)).thenReturn(responseDTOs);

        // WHEN
        List<ProductResponseDTO> result = productService.findByPriceRange(minPrice, maxPrice);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        verify(productRepository).findByPriceRange(minPrice, maxPrice);
        verify(productMapper).toResponseDTOList(products);
    }

    // ==================== Tests pour findAllCategories() ====================

    @Test
    @DisplayName("Should find all categories successfully")
    void testFindAllCategories_Success() {
        // GIVEN
        List<String> categories = Arrays.asList("Electronics", "Books", "Clothing");
        when(productRepository.findAllDistinctCategories()).thenReturn(categories);

        // WHEN
        List<String> result = productService.findAllCategories();

        // THEN
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result).contains("Electronics", "Books", "Clothing");

        verify(productRepository).findAllDistinctCategories();
    }
}
