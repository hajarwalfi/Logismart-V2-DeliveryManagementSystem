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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ParcelProductService {

    private final ParcelProductRepository parcelProductRepository;
    private final ParcelProductMapper parcelProductMapper;
    private final ParcelRepository parcelRepository;
    private final ProductRepository productRepository;

    public ParcelProductResponseDTO create(ParcelProductCreateDTO dto) {
        log.info("Creating parcel-product association: parcel ID {} with product ID {}",
                dto.getParcelId(), dto.getProductId());

        
        Parcel parcel = parcelRepository.findById(dto.getParcelId())
                .orElseThrow(() -> new ResourceNotFoundException("Parcel", "id", dto.getParcelId()));

        
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", dto.getProductId()));

        
        ParcelProduct parcelProduct = parcelProductMapper.toEntity(dto);
        parcelProduct.setParcel(parcel);
        parcelProduct.setProduct(product);

        
        ParcelProduct savedParcelProduct = parcelProductRepository.save(parcelProduct);
        log.info("Parcel-product association created successfully with ID: {}", savedParcelProduct.getId());

        return parcelProductMapper.toResponseDTO(savedParcelProduct);
    }

    @Transactional(readOnly = true)
    public ParcelProductResponseDTO findById(String id) {
        log.info("Finding parcel-product by ID: {}", id);

        ParcelProduct parcelProduct = parcelProductRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ParcelProduct", "id", id));

        return parcelProductMapper.toResponseDTO(parcelProduct);
    }

    @Transactional(readOnly = true)
    public List<ParcelProductResponseDTO> findAll() {
        log.info("Finding all parcel-product associations");
        List<ParcelProduct> parcelProducts = parcelProductRepository.findAllWithRelationships();
        return parcelProductMapper.toResponseDTOList(parcelProducts);
    }

    public ParcelProductResponseDTO update(ParcelProductUpdateDTO dto) {
        log.info("Updating parcel-product with ID: {}", dto.getId());

        ParcelProduct existingParcelProduct = parcelProductRepository.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("ParcelProduct", "id", dto.getId()));

        
        parcelProductMapper.updateEntityFromDTO(dto, existingParcelProduct);
        ParcelProduct updatedParcelProduct = parcelProductRepository.save(existingParcelProduct);
        log.info("Parcel-product updated successfully with ID: {}", updatedParcelProduct.getId());

        return parcelProductMapper.toResponseDTO(updatedParcelProduct);
    }

    public void delete(String id) {
        log.info("Deleting parcel-product with ID: {}", id);

        if (!parcelProductRepository.existsById(id)) {
            throw new ResourceNotFoundException("ParcelProduct", "id", id);
        }

        parcelProductRepository.deleteById(id);
        log.info("Parcel-product deleted successfully with ID: {}", id);
    }

    

    @Transactional(readOnly = true)
    public List<ParcelProductResponseDTO> findByParcelId(String parcelId) {
        log.info("Finding all products for parcel ID: {}", parcelId);

        
        if (!parcelRepository.existsById(parcelId)) {
            throw new ResourceNotFoundException("Parcel", "id", parcelId);
        }

        List<ParcelProduct> parcelProducts = parcelProductRepository.findByParcelIdWithProduct(parcelId);
        log.info("Found {} products for parcel ID: {}", parcelProducts.size(), parcelId);

        return parcelProductMapper.toResponseDTOList(parcelProducts);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateParcelTotalValue(String parcelId) {
        log.info("Calculating total value for parcel ID: {}", parcelId);

        if (!parcelRepository.existsById(parcelId)) {
            throw new ResourceNotFoundException("Parcel", "id", parcelId);
        }

        return parcelProductRepository.calculateTotalValueByParcelId(parcelId);
    }

    @Transactional(readOnly = true)
    public Long countProductsInParcel(String parcelId) {
        log.info("Counting products in parcel ID: {}", parcelId);

        if (!parcelRepository.existsById(parcelId)) {
            throw new ResourceNotFoundException("Parcel", "id", parcelId);
        }

        return parcelProductRepository.countByParcelId(parcelId);
    }

    

    @Transactional(readOnly = true)
    public List<ParcelProductResponseDTO> findByProductId(String productId) {
        log.info("Finding all parcels containing product ID: {}", productId);

        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product", "id", productId);
        }

        List<ParcelProduct> parcelProducts = parcelProductRepository.findByProductId(productId);
        log.info("Found {} parcels containing product ID: {}", parcelProducts.size(), productId);

        return parcelProductMapper.toResponseDTOList(parcelProducts);
    }

    @Transactional(readOnly = true)
    public Long calculateTotalQuantityShipped(String productId) {
        log.info("Calculating total quantity shipped for product ID: {}", productId);

        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product", "id", productId);
        }

        return parcelProductRepository.calculateTotalQuantityByProductId(productId);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateProductRevenue(String productId) {
        log.info("Calculating revenue for product ID: {}", productId);

        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product", "id", productId);
        }

        return parcelProductRepository.calculateTotalRevenueByProductId(productId);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateAveragePrice(String productId) {
        log.info("Calculating average price for product ID: {}", productId);

        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product", "id", productId);
        }

        return parcelProductRepository.calculateAveragePriceByProductId(productId);
    }

    

    @Transactional(readOnly = true)
    public List<ParcelProductResponseDTO> findBulkOrders(Integer minQuantity) {
        log.info("Finding bulk orders with minimum quantity: {}", minQuantity);
        List<ParcelProduct> bulkOrders = parcelProductRepository.findBulkOrders(minQuantity);
        return parcelProductMapper.toResponseDTOList(bulkOrders);
    }

    @Transactional(readOnly = true)
    public List<ParcelProductResponseDTO> findDiscountedProducts() {
        log.info("Finding products purchased at a discount");
        List<ParcelProduct> discountedProducts = parcelProductRepository.findDiscountedProducts();
        return parcelProductMapper.toResponseDTOList(discountedProducts);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateTotalRevenue() {
        log.info("Calculating total revenue across all parcels");
        return parcelProductRepository.calculateTotalRevenue();
    }

    @Transactional(readOnly = true)
    public Long calculateTotalItemsShipped() {
        log.info("Calculating total items shipped");
        return parcelProductRepository.calculateTotalItemsShipped();
    }

    @Transactional(readOnly = true)
    public Long countDistinctProductsShipped() {
        log.info("Counting distinct products shipped");
        return parcelProductRepository.countDistinctProducts();
    }
}
