package com.logismart.logismartv2.service;

import com.logismart.logismartv2.dto.product.ProductCreateDTO;
import com.logismart.logismartv2.dto.product.ProductResponseDTO;
import com.logismart.logismartv2.dto.product.ProductUpdateDTO;
import com.logismart.logismartv2.entity.Product;
import com.logismart.logismartv2.exception.DuplicateResourceException;
import com.logismart.logismartv2.exception.ResourceNotFoundException;
import com.logismart.logismartv2.mapper.ProductMapper;
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
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductResponseDTO create(ProductCreateDTO dto) {
        log.info("Creating new product with name: {}", dto.getName());

        if (productRepository.existsByName(dto.getName())) {
            throw new DuplicateResourceException("Product", "name", dto.getName());
        }

        Product product = productMapper.toEntity(dto);
        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with ID: {}", savedProduct.getId());

        return productMapper.toResponseDTO(savedProduct);
    }

    @Transactional(readOnly = true)
    public ProductResponseDTO findById(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return productMapper.toResponseDTO(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> findAll() {
        return productMapper.toResponseDTOList(productRepository.findAll());
    }

    public ProductResponseDTO update(ProductUpdateDTO dto) {
        Product existingProduct = productRepository.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", dto.getId()));

        if (!existingProduct.getName().equals(dto.getName()) &&
                productRepository.existsByName(dto.getName())) {
            throw new DuplicateResourceException("Product", "name", dto.getName());
        }

        productMapper.updateEntityFromDTO(dto, existingProduct);
        Product updatedProduct = productRepository.save(existingProduct);

        return productMapper.toResponseDTO(updatedProduct);
    }

    public void delete(String id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product", "id", id);
        }
        productRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> findByCategory(String category) {
        return productMapper.toResponseDTOList(productRepository.findByCategory(category));
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> searchByName(String keyword) {
        return productMapper.toResponseDTOList(productRepository.findByNameContainingIgnoreCase(keyword));
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return productMapper.toResponseDTOList(productRepository.findByPriceRange(minPrice, maxPrice));
    }

    @Transactional(readOnly = true)
    public List<String> findAllCategories() {
        return productRepository.findAllDistinctCategories();
    }
}
