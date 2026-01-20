package com.logismart.logismartv2.controller;

import com.logismart.logismartv2.dto.product.ProductCreateDTO;
import com.logismart.logismartv2.dto.product.ProductResponseDTO;
import com.logismart.logismartv2.dto.product.ProductUpdateDTO;
import com.logismart.logismartv2.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product Catalog", description = "APIs for managing product catalog")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Create a new product",
            description = "Creates a new product in the catalog with unique name"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Product created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Product with same name already exists")
    })
    public ResponseEntity<ProductResponseDTO> createProduct(
            @Valid @RequestBody ProductCreateDTO dto) {
        log.info("REST: Creating new product with name: {}", dto.getName());
        ProductResponseDTO created = productService.create(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'CLIENT')")
    @Operation(
            summary = "Get product by ID",
            description = "Retrieves a product by its unique identifier"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product found"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ProductResponseDTO> getProductById(
            @Parameter(description = "Product ID", required = true)
            @PathVariable String id) {
        log.info("REST: Finding product by ID: {}", id);
        ProductResponseDTO product = productService.findById(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'CLIENT')")
    @Operation(
            summary = "Get all products",
            description = "Retrieves a list of all products in the catalog. Accessible by MANAGER and CLIENT."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of products retrieved successfully")
    })
    public ResponseEntity<List<ProductResponseDTO>> getAllProducts() {
        log.info("REST: Finding all products");
        List<ProductResponseDTO> products = productService.findAll();
        return ResponseEntity.ok(products);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Update a product",
            description = "Updates an existing product's information"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "409", description = "Product with same name already exists")
    })
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @Parameter(description = "Product ID", required = true)
            @PathVariable String id,
            @Valid @RequestBody ProductUpdateDTO dto) {
        log.info("REST: Updating product with ID: {}", id);

        
        dto.setId(id);

        ProductResponseDTO updated = productService.update(dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Delete a product",
            description = "Deletes a product by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "Product ID", required = true)
            @PathVariable String id) {
        log.info("REST: Deleting product with ID: {}", id);
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/category/{category}")
    @PreAuthorize("hasAnyRole('MANAGER', 'CLIENT')")
    @Operation(
            summary = "Get products by category",
            description = "Retrieves all products in a specific category"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products retrieved successfully")
    })
    public ResponseEntity<List<ProductResponseDTO>> getProductsByCategory(
            @Parameter(description = "Product category", required = true)
            @PathVariable String category) {
        log.info("REST: Finding products by category: {}", category);
        List<ProductResponseDTO> products = productService.findByCategory(category);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('MANAGER', 'CLIENT')")
    @Operation(
            summary = "Search products by name",
            description = "Searches for products by name (case-insensitive)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully")
    })
    public ResponseEntity<List<ProductResponseDTO>> searchProducts(
            @Parameter(description = "Search keyword", required = true)
            @RequestParam String keyword) {
        log.info("REST: Searching products with keyword: {}", keyword);
        List<ProductResponseDTO> products = productService.searchByName(keyword);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/price-range")
    @PreAuthorize("hasAnyRole('MANAGER', 'CLIENT')")
    @Operation(
            summary = "Get products by price range",
            description = "Retrieves products within a specified price range"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products retrieved successfully")
    })
    public ResponseEntity<List<ProductResponseDTO>> getProductsByPriceRange(
            @Parameter(description = "Minimum price", required = true)
            @RequestParam BigDecimal min,
            @Parameter(description = "Maximum price", required = true)
            @RequestParam BigDecimal max) {
        log.info("REST: Finding products in price range: {} - {}", min, max);
        List<ProductResponseDTO> products = productService.findByPriceRange(min, max);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/categories")
    @PreAuthorize("hasAnyRole('MANAGER', 'CLIENT')")
    @Operation(
            summary = "Get all product categories",
            description = "Retrieves a list of all distinct product categories"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categories retrieved successfully")
    })
    public ResponseEntity<List<String>> getAllCategories() {
        log.info("REST: Finding all product categories");
        List<String> categories = productService.findAllCategories();
        return ResponseEntity.ok(categories);
    }
}
