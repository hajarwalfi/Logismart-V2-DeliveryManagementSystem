package com.logismart.logismartv2.service;

import com.logismart.logismartv2.dto.parcelproduct.ParcelProductCreateDTO;
import com.logismart.logismartv2.dto.parcelproduct.ParcelProductResponseDTO;
import com.logismart.logismartv2.dto.parcelproduct.ParcelProductUpdateDTO;
import com.logismart.logismartv2.entity.Parcel;
import com.logismart.logismartv2.entity.ParcelProduct;
import com.logismart.logismartv2.entity.Product;
import com.logismart.logismartv2.exception.ResourceNotFoundException;
import com.logismart.logismartv2.mapper.ParcelProductMapper;
import com.logismart.logismartv2.repository.ParcelProductRepository;
import com.logismart.logismartv2.repository.ParcelRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ParcelProductService Unit Tests")
class ParcelProductServiceTest {

    @Mock
    private ParcelProductRepository parcelProductRepository;

    @Mock
    private ParcelProductMapper parcelProductMapper;

    @Mock
    private ParcelRepository parcelRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ParcelProductService parcelProductService;

    private ParcelProduct parcelProduct;
    private ParcelProductCreateDTO createDTO;
    private ParcelProductUpdateDTO updateDTO;
    private ParcelProductResponseDTO responseDTO;
    private Parcel parcel;
    private Product product;

    @BeforeEach
    void setUp() {
        parcel = new Parcel();
        parcel.setId("parcel-1");

        product = new Product();
        product.setId("product-1");
        product.setName("iPhone 15");
        product.setPrice(new BigDecimal("999.00"));

        parcelProduct = new ParcelProduct();
        parcelProduct.setId("pp-1");
        parcelProduct.setParcel(parcel);
        parcelProduct.setProduct(product);
        parcelProduct.setQuantity(2);
        parcelProduct.setPrice(new BigDecimal("999.00"));

        createDTO = new ParcelProductCreateDTO();
        createDTO.setParcelId("parcel-1");
        createDTO.setProductId("product-1");
        createDTO.setQuantity(2);
        createDTO.setPrice(new BigDecimal("999.00"));

        updateDTO = new ParcelProductUpdateDTO();
        updateDTO.setId("pp-1");
        updateDTO.setQuantity(3);
        updateDTO.setPrice(new BigDecimal("899.00"));

        responseDTO = new ParcelProductResponseDTO();
        responseDTO.setId("pp-1");
        responseDTO.setParcelId("parcel-1");
        responseDTO.setProductId("product-1");
        responseDTO.setQuantity(2);
        responseDTO.setPrice(new BigDecimal("999.00"));
    }

    // ==================== Tests pour create() ====================

    @Test
    @DisplayName("Should create parcel-product association successfully when parcel and product exist")
    void testCreate_Success() {
        // GIVEN
        when(parcelRepository.findById("parcel-1")).thenReturn(Optional.of(parcel));
        when(productRepository.findById("product-1")).thenReturn(Optional.of(product));
        when(parcelProductMapper.toEntity(createDTO)).thenReturn(parcelProduct);
        when(parcelProductRepository.save(parcelProduct)).thenReturn(parcelProduct);
        when(parcelProductMapper.toResponseDTO(parcelProduct)).thenReturn(responseDTO);

        // WHEN
        ParcelProductResponseDTO result = parcelProductService.create(createDTO);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getParcelId()).isEqualTo("parcel-1");
        assertThat(result.getProductId()).isEqualTo("product-1");
        assertThat(result.getQuantity()).isEqualTo(2);

        verify(parcelRepository).findById("parcel-1");
        verify(productRepository).findById("product-1");
        verify(parcelProductRepository).save(parcelProduct);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when creating with non-existent parcel")
    void testCreate_ParcelNotFound() {
        // GIVEN
        when(parcelRepository.findById("parcel-999")).thenReturn(Optional.empty());
        createDTO.setParcelId("parcel-999");

        // WHEN & THEN
        assertThatThrownBy(() -> parcelProductService.create(createDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Parcel");

        verify(parcelRepository).findById("parcel-999");
        verify(productRepository, never()).findById(any());
        verify(parcelProductRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when creating with non-existent product")
    void testCreate_ProductNotFound() {
        // GIVEN
        when(parcelRepository.findById("parcel-1")).thenReturn(Optional.of(parcel));
        when(productRepository.findById("product-999")).thenReturn(Optional.empty());
        createDTO.setProductId("product-999");

        // WHEN & THEN
        assertThatThrownBy(() -> parcelProductService.create(createDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product");

        verify(parcelRepository).findById("parcel-1");
        verify(productRepository).findById("product-999");
        verify(parcelProductRepository, never()).save(any());
    }

    // ==================== Tests pour findById() ====================

    @Test
    @DisplayName("Should find parcel-product by id successfully")
    void testFindById_Success() {
        // GIVEN
        when(parcelProductRepository.findById("pp-1")).thenReturn(Optional.of(parcelProduct));
        when(parcelProductMapper.toResponseDTO(parcelProduct)).thenReturn(responseDTO);

        // WHEN
        ParcelProductResponseDTO result = parcelProductService.findById("pp-1");

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("pp-1");

        verify(parcelProductRepository).findById("pp-1");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when parcel-product not found")
    void testFindById_NotFound() {
        // GIVEN
        when(parcelProductRepository.findById("pp-999")).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> parcelProductService.findById("pp-999"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("ParcelProduct");

        verify(parcelProductRepository).findById("pp-999");
    }

    // ==================== Tests pour findAll() ====================

    @Test
    @DisplayName("Should return all parcel-product associations successfully")
    void testFindAll_Success() {
        // GIVEN
        List<ParcelProduct> parcelProducts = Arrays.asList(parcelProduct);
        List<ParcelProductResponseDTO> responseDTOs = Arrays.asList(responseDTO);

        when(parcelProductRepository.findAllWithRelationships()).thenReturn(parcelProducts);
        when(parcelProductMapper.toResponseDTOList(parcelProducts)).thenReturn(responseDTOs);

        // WHEN
        List<ParcelProductResponseDTO> result = parcelProductService.findAll();

        // THEN
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        verify(parcelProductRepository).findAllWithRelationships();
    }

    // ==================== Tests pour update() ====================

    @Test
    @DisplayName("Should update parcel-product successfully")
    void testUpdate_Success() {
        // GIVEN
        when(parcelProductRepository.findById(updateDTO.getId())).thenReturn(Optional.of(parcelProduct));
        when(parcelProductRepository.save(parcelProduct)).thenReturn(parcelProduct);
        when(parcelProductMapper.toResponseDTO(parcelProduct)).thenReturn(responseDTO);

        // WHEN
        ParcelProductResponseDTO result = parcelProductService.update(updateDTO);

        // THEN
        assertThat(result).isNotNull();

        verify(parcelProductRepository).findById("pp-1");
        verify(parcelProductMapper).updateEntityFromDTO(updateDTO, parcelProduct);
        verify(parcelProductRepository).save(parcelProduct);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent parcel-product")
    void testUpdate_NotFound() {
        // GIVEN
        when(parcelProductRepository.findById(updateDTO.getId())).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> parcelProductService.update(updateDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("ParcelProduct");

        verify(parcelProductRepository, never()).save(any());
    }

    // ==================== Tests pour delete() ====================

    @Test
    @DisplayName("Should delete parcel-product successfully")
    void testDelete_Success() {
        // GIVEN
        when(parcelProductRepository.existsById("pp-1")).thenReturn(true);
        doNothing().when(parcelProductRepository).deleteById("pp-1");

        // WHEN
        parcelProductService.delete("pp-1");

        // THEN
        verify(parcelProductRepository).existsById("pp-1");
        verify(parcelProductRepository).deleteById("pp-1");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent parcel-product")
    void testDelete_NotFound() {
        // GIVEN
        when(parcelProductRepository.existsById("pp-999")).thenReturn(false);

        // WHEN & THEN
        assertThatThrownBy(() -> parcelProductService.delete("pp-999"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(parcelProductRepository, never()).deleteById(any());
    }

    // ==================== Tests pour findByParcelId() ====================

    @Test
    @DisplayName("Should find products by parcel id successfully")
    void testFindByParcelId_Success() {
        // GIVEN
        List<ParcelProduct> parcelProducts = Arrays.asList(parcelProduct);
        List<ParcelProductResponseDTO> responseDTOs = Arrays.asList(responseDTO);

        when(parcelRepository.existsById("parcel-1")).thenReturn(true);
        when(parcelProductRepository.findByParcelIdWithProduct("parcel-1")).thenReturn(parcelProducts);
        when(parcelProductMapper.toResponseDTOList(parcelProducts)).thenReturn(responseDTOs);

        // WHEN
        List<ParcelProductResponseDTO> result = parcelProductService.findByParcelId("parcel-1");

        // THEN
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        verify(parcelRepository).existsById("parcel-1");
        verify(parcelProductRepository).findByParcelIdWithProduct("parcel-1");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when finding by non-existent parcel")
    void testFindByParcelId_ParcelNotFound() {
        // GIVEN
        when(parcelRepository.existsById("parcel-999")).thenReturn(false);

        // WHEN & THEN
        assertThatThrownBy(() -> parcelProductService.findByParcelId("parcel-999"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Parcel");

        verify(parcelRepository).existsById("parcel-999");
        verify(parcelProductRepository, never()).findByParcelIdWithProduct(any());
    }

    // ==================== Tests pour calculateParcelTotalValue() ====================

    @Test
    @DisplayName("Should calculate parcel total value successfully")
    void testCalculateParcelTotalValue_Success() {
        // GIVEN
        when(parcelRepository.existsById("parcel-1")).thenReturn(true);
        when(parcelProductRepository.calculateTotalValueByParcelId("parcel-1"))
                .thenReturn(new BigDecimal("1998.00"));

        // WHEN
        BigDecimal result = parcelProductService.calculateParcelTotalValue("parcel-1");

        // THEN
        assertThat(result).isEqualTo(new BigDecimal("1998.00"));

        verify(parcelRepository).existsById("parcel-1");
        verify(parcelProductRepository).calculateTotalValueByParcelId("parcel-1");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when calculating value for non-existent parcel")
    void testCalculateParcelTotalValue_ParcelNotFound() {
        // GIVEN
        when(parcelRepository.existsById("parcel-999")).thenReturn(false);

        // WHEN & THEN
        assertThatThrownBy(() -> parcelProductService.calculateParcelTotalValue("parcel-999"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Parcel");

        verify(parcelProductRepository, never()).calculateTotalValueByParcelId(any());
    }

    // ==================== Tests pour countProductsInParcel() ====================

    @Test
    @DisplayName("Should count products in parcel successfully")
    void testCountProductsInParcel_Success() {
        // GIVEN
        when(parcelRepository.existsById("parcel-1")).thenReturn(true);
        when(parcelProductRepository.countByParcelId("parcel-1")).thenReturn(3L);

        // WHEN
        Long result = parcelProductService.countProductsInParcel("parcel-1");

        // THEN
        assertThat(result).isEqualTo(3L);

        verify(parcelRepository).existsById("parcel-1");
        verify(parcelProductRepository).countByParcelId("parcel-1");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when counting products for non-existent parcel")
    void testCountProductsInParcel_ParcelNotFound() {
        // GIVEN
        when(parcelRepository.existsById("parcel-999")).thenReturn(false);

        // WHEN & THEN
        assertThatThrownBy(() -> parcelProductService.countProductsInParcel("parcel-999"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Parcel");

        verify(parcelProductRepository, never()).countByParcelId(any());
    }

    // ==================== Tests pour findByProductId() ====================

    @Test
    @DisplayName("Should find parcels by product id successfully")
    void testFindByProductId_Success() {
        // GIVEN
        List<ParcelProduct> parcelProducts = Arrays.asList(parcelProduct);
        List<ParcelProductResponseDTO> responseDTOs = Arrays.asList(responseDTO);

        when(productRepository.existsById("product-1")).thenReturn(true);
        when(parcelProductRepository.findByProductId("product-1")).thenReturn(parcelProducts);
        when(parcelProductMapper.toResponseDTOList(parcelProducts)).thenReturn(responseDTOs);

        // WHEN
        List<ParcelProductResponseDTO> result = parcelProductService.findByProductId("product-1");

        // THEN
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        verify(productRepository).existsById("product-1");
        verify(parcelProductRepository).findByProductId("product-1");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when finding by non-existent product")
    void testFindByProductId_ProductNotFound() {
        // GIVEN
        when(productRepository.existsById("product-999")).thenReturn(false);

        // WHEN & THEN
        assertThatThrownBy(() -> parcelProductService.findByProductId("product-999"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product");

        verify(productRepository).existsById("product-999");
        verify(parcelProductRepository, never()).findByProductId(any());
    }

    // ==================== Tests pour calculateTotalQuantityShipped() ====================

    @Test
    @DisplayName("Should calculate total quantity shipped for product successfully")
    void testCalculateTotalQuantityShipped_Success() {
        // GIVEN
        when(productRepository.existsById("product-1")).thenReturn(true);
        when(parcelProductRepository.calculateTotalQuantityByProductId("product-1")).thenReturn(50L);

        // WHEN
        Long result = parcelProductService.calculateTotalQuantityShipped("product-1");

        // THEN
        assertThat(result).isEqualTo(50L);

        verify(productRepository).existsById("product-1");
        verify(parcelProductRepository).calculateTotalQuantityByProductId("product-1");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when calculating quantity for non-existent product")
    void testCalculateTotalQuantityShipped_ProductNotFound() {
        // GIVEN
        when(productRepository.existsById("product-999")).thenReturn(false);

        // WHEN & THEN
        assertThatThrownBy(() -> parcelProductService.calculateTotalQuantityShipped("product-999"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product");

        verify(parcelProductRepository, never()).calculateTotalQuantityByProductId(any());
    }

    // ==================== Tests pour calculateProductRevenue() ====================

    @Test
    @DisplayName("Should calculate product revenue successfully")
    void testCalculateProductRevenue_Success() {
        // GIVEN
        when(productRepository.existsById("product-1")).thenReturn(true);
        when(parcelProductRepository.calculateTotalRevenueByProductId("product-1"))
                .thenReturn(new BigDecimal("49950.00"));

        // WHEN
        BigDecimal result = parcelProductService.calculateProductRevenue("product-1");

        // THEN
        assertThat(result).isEqualTo(new BigDecimal("49950.00"));

        verify(productRepository).existsById("product-1");
        verify(parcelProductRepository).calculateTotalRevenueByProductId("product-1");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when calculating revenue for non-existent product")
    void testCalculateProductRevenue_ProductNotFound() {
        // GIVEN
        when(productRepository.existsById("product-999")).thenReturn(false);

        // WHEN & THEN
        assertThatThrownBy(() -> parcelProductService.calculateProductRevenue("product-999"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product");

        verify(parcelProductRepository, never()).calculateTotalRevenueByProductId(any());
    }

    // ==================== Tests pour calculateAveragePrice() ====================

    @Test
    @DisplayName("Should calculate average price for product successfully")
    void testCalculateAveragePrice_Success() {
        // GIVEN
        when(productRepository.existsById("product-1")).thenReturn(true);
        when(parcelProductRepository.calculateAveragePriceByProductId("product-1"))
                .thenReturn(new BigDecimal("950.00"));

        // WHEN
        BigDecimal result = parcelProductService.calculateAveragePrice("product-1");

        // THEN
        assertThat(result).isEqualTo(new BigDecimal("950.00"));

        verify(productRepository).existsById("product-1");
        verify(parcelProductRepository).calculateAveragePriceByProductId("product-1");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when calculating average price for non-existent product")
    void testCalculateAveragePrice_ProductNotFound() {
        // GIVEN
        when(productRepository.existsById("product-999")).thenReturn(false);

        // WHEN & THEN
        assertThatThrownBy(() -> parcelProductService.calculateAveragePrice("product-999"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product");

        verify(parcelProductRepository, never()).calculateAveragePriceByProductId(any());
    }

    // ==================== Tests pour findBulkOrders() ====================

    @Test
    @DisplayName("Should find bulk orders successfully")
    void testFindBulkOrders_Success() {
        // GIVEN
        List<ParcelProduct> bulkOrders = Arrays.asList(parcelProduct);
        List<ParcelProductResponseDTO> responseDTOs = Arrays.asList(responseDTO);

        when(parcelProductRepository.findBulkOrders(10)).thenReturn(bulkOrders);
        when(parcelProductMapper.toResponseDTOList(bulkOrders)).thenReturn(responseDTOs);

        // WHEN
        List<ParcelProductResponseDTO> result = parcelProductService.findBulkOrders(10);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        verify(parcelProductRepository).findBulkOrders(10);
    }

    // ==================== Tests pour findDiscountedProducts() ====================

    @Test
    @DisplayName("Should find discounted products successfully")
    void testFindDiscountedProducts_Success() {
        // GIVEN
        List<ParcelProduct> discounted = Arrays.asList(parcelProduct);
        List<ParcelProductResponseDTO> responseDTOs = Arrays.asList(responseDTO);

        when(parcelProductRepository.findDiscountedProducts()).thenReturn(discounted);
        when(parcelProductMapper.toResponseDTOList(discounted)).thenReturn(responseDTOs);

        // WHEN
        List<ParcelProductResponseDTO> result = parcelProductService.findDiscountedProducts();

        // THEN
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        verify(parcelProductRepository).findDiscountedProducts();
    }

    // ==================== Tests pour calculateTotalRevenue() ====================

    @Test
    @DisplayName("Should calculate total revenue successfully")
    void testCalculateTotalRevenue_Success() {
        // GIVEN
        when(parcelProductRepository.calculateTotalRevenue()).thenReturn(new BigDecimal("500000.00"));

        // WHEN
        BigDecimal result = parcelProductService.calculateTotalRevenue();

        // THEN
        assertThat(result).isEqualTo(new BigDecimal("500000.00"));

        verify(parcelProductRepository).calculateTotalRevenue();
    }

    // ==================== Tests pour calculateTotalItemsShipped() ====================

    @Test
    @DisplayName("Should calculate total items shipped successfully")
    void testCalculateTotalItemsShipped_Success() {
        // GIVEN
        when(parcelProductRepository.calculateTotalItemsShipped()).thenReturn(1500L);

        // WHEN
        Long result = parcelProductService.calculateTotalItemsShipped();

        // THEN
        assertThat(result).isEqualTo(1500L);

        verify(parcelProductRepository).calculateTotalItemsShipped();
    }

    // ==================== Tests pour countDistinctProductsShipped() ====================

    @Test
    @DisplayName("Should count distinct products shipped successfully")
    void testCountDistinctProductsShipped_Success() {
        // GIVEN
        when(parcelProductRepository.countDistinctProducts()).thenReturn(25L);

        // WHEN
        Long result = parcelProductService.countDistinctProductsShipped();

        // THEN
        assertThat(result).isEqualTo(25L);

        verify(parcelProductRepository).countDistinctProducts();
    }
}
