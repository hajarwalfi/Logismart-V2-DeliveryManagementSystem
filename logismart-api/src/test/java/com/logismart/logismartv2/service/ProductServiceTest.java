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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Product Service Tests")
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
        product.setId("product-1");
        product.setName("Laptop HP");
        product.setCategory("Electronics");
        product.setPrice(BigDecimal.valueOf(15000));
        product.setWeight(BigDecimal.valueOf(2.5));

        createDTO = new ProductCreateDTO();
        createDTO.setName("Laptop HP");
        createDTO.setCategory("Electronics");
        createDTO.setPrice(BigDecimal.valueOf(15000));
        createDTO.setWeight(BigDecimal.valueOf(2.5));

        updateDTO = new ProductUpdateDTO();
        updateDTO.setId("product-1");
        updateDTO.setName("Laptop HP Updated");
        updateDTO.setCategory("Electronics");
        updateDTO.setPrice(BigDecimal.valueOf(16000));
        updateDTO.setWeight(BigDecimal.valueOf(2.5));

        responseDTO = new ProductResponseDTO();
        responseDTO.setId("product-1");
        responseDTO.setName("Laptop HP");
        responseDTO.setCategory("Electronics");
        responseDTO.setPrice(BigDecimal.valueOf(15000));
    }

    @Test
    @DisplayName("Should create product successfully")
    void testCreateProduct_Success() {
        // Given
        when(productRepository.existsByName("Laptop HP")).thenReturn(false);
        when(productMapper.toEntity(createDTO)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponseDTO(product)).thenReturn(responseDTO);

        // When
        ProductResponseDTO result = productService.create(createDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Laptop HP");
        assertThat(result.getCategory()).isEqualTo("Electronics");
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("Should throw exception when creating duplicate product")
    void testCreateProduct_Duplicate() {
        // Given
        when(productRepository.existsByName("Laptop HP")).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> productService.create(createDTO))
                .isInstanceOf(DuplicateResourceException.class);

        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should find product by ID successfully")
    void testFindById_Success() {
        // Given
        when(productRepository.findById("product-1")).thenReturn(Optional.of(product));
        when(productMapper.toResponseDTO(product)).thenReturn(responseDTO);

        // When
        ProductResponseDTO result = productService.findById("product-1");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("product-1");
        verify(productRepository).findById("product-1");
    }

    @Test
    @DisplayName("Should throw exception when product not found")
    void testFindById_NotFound() {
        // Given
        when(productRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> productService.findById("invalid-id"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should find all products successfully")
    void testFindAll_Success() {
        // Given
        Product product2 = new Product();
        product2.setId("product-2");
        product2.setName("Phone");

        List<Product> products = Arrays.asList(product, product2);
        when(productRepository.findAll()).thenReturn(products);
        when(productMapper.toResponseDTOList(products)).thenReturn(Arrays.asList(responseDTO, new ProductResponseDTO()));

        // When
        List<ProductResponseDTO> result = productService.findAll();

        // Then
        assertThat(result).hasSize(2);
        verify(productRepository).findAll();
    }

    @Test
    @DisplayName("Should update product successfully")
    void testUpdateProduct_Success() {
        // Given
        when(productRepository.findById("product-1")).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponseDTO(product)).thenReturn(responseDTO);

        // When
        ProductResponseDTO result = productService.update(updateDTO);

        // Then
        assertThat(result).isNotNull();
        verify(productMapper).updateEntityFromDTO(updateDTO, product);
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("Should delete product successfully")
    void testDeleteProduct_Success() {
        // Given
        when(productRepository.existsById("product-1")).thenReturn(true);

        // When
        productService.delete("product-1");

        // Then
        verify(productRepository).deleteById("product-1");
    }

    @Test
    @DisplayName("Should find products by category")
    void testFindByCategory_Success() {
        // Given
        List<Product> products = Arrays.asList(product);
        when(productRepository.findByCategory("Electronics")).thenReturn(products);
        when(productMapper.toResponseDTOList(products)).thenReturn(Arrays.asList(responseDTO));

        // When
        List<ProductResponseDTO> result = productService.findByCategory("Electronics");

        // Then
        assertThat(result).hasSize(1);
        verify(productRepository).findByCategory("Electronics");
    }

    @Test
    @DisplayName("Should search products by name")
    void testSearchByName_Success() {
        // Given
        List<Product> products = Arrays.asList(product);
        when(productRepository.findByNameContainingIgnoreCase("laptop")).thenReturn(products);
        when(productMapper.toResponseDTOList(products)).thenReturn(Arrays.asList(responseDTO));

        // When
        List<ProductResponseDTO> result = productService.searchByName("laptop");

        // Then
        assertThat(result).hasSize(1);
        verify(productRepository).findByNameContainingIgnoreCase("laptop");
    }
}
