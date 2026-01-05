package com.logismart.logismartv2.controller;

import com.logismart.logismartv2.dto.parcelproduct.ParcelProductCreateDTO;
import com.logismart.logismartv2.dto.parcelproduct.ParcelProductResponseDTO;
import com.logismart.logismartv2.dto.parcelproduct.ParcelProductUpdateDTO;
import com.logismart.logismartv2.service.ParcelProductService;
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
@RequestMapping("/api/parcel-products")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('MANAGER')")
@Tag(name = "Parcel-Product Association", description = "APIs for managing products within parcels and sales analytics")
public class ParcelProductController {

    private final ParcelProductService parcelProductService;

    @PostMapping
    @Operation(
            summary = "Create a parcel-product association",
            description = "Adds a product to an existing parcel (typically done during parcel creation)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Association created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Parcel or Product not found")
    })
    public ResponseEntity<ParcelProductResponseDTO> createParcelProduct(
            @Valid @RequestBody ParcelProductCreateDTO dto) {
        log.info("REST: Creating parcel-product association");
        ParcelProductResponseDTO created = parcelProductService.create(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get parcel-product by ID",
            description = "Retrieves a parcel-product association by its unique identifier"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Association found"),
            @ApiResponse(responseCode = "404", description = "Association not found")
    })
    public ResponseEntity<ParcelProductResponseDTO> getParcelProductById(
            @Parameter(description = "Parcel-product ID", required = true)
            @PathVariable String id) {
        log.info("REST: Finding parcel-product by ID: {}", id);
        ParcelProductResponseDTO parcelProduct = parcelProductService.findById(id);
        return ResponseEntity.ok(parcelProduct);
    }

    @GetMapping
    @Operation(
            summary = "Get all parcel-product associations",
            description = "Retrieves all parcel-product associations"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Associations retrieved successfully")
    })
    public ResponseEntity<List<ParcelProductResponseDTO>> getAllParcelProducts() {
        log.info("REST: Finding all parcel-product associations");
        List<ParcelProductResponseDTO> parcelProducts = parcelProductService.findAll();
        return ResponseEntity.ok(parcelProducts);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update a parcel-product association",
            description = "Updates quantity or price for a product in a parcel"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Association updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Association not found")
    })
    public ResponseEntity<ParcelProductResponseDTO> updateParcelProduct(
            @Parameter(description = "Parcel-product ID", required = true)
            @PathVariable String id,
            @Valid @RequestBody ParcelProductUpdateDTO dto) {
        log.info("REST: Updating parcel-product with ID: {}", id);

        
        dto.setId(id);

        ParcelProductResponseDTO updated = parcelProductService.update(dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a parcel-product association",
            description = "Removes a product from a parcel"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Association deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Association not found")
    })
    public ResponseEntity<Void> deleteParcelProduct(
            @Parameter(description = "Parcel-product ID", required = true)
            @PathVariable String id) {
        log.info("REST: Deleting parcel-product with ID: {}", id);
        parcelProductService.delete(id);
        return ResponseEntity.noContent().build();
    }

    

    @GetMapping("/parcel/{parcelId}")
    @Operation(
            summary = "Get products in a parcel",
            description = "Retrieves all products included in a specific parcel"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Parcel not found")
    })
    public ResponseEntity<List<ParcelProductResponseDTO>> getProductsByParcelId(
            @Parameter(description = "Parcel ID", required = true)
            @PathVariable String parcelId) {
        log.info("REST: Finding products for parcel ID: {}", parcelId);
        List<ParcelProductResponseDTO> products = parcelProductService.findByParcelId(parcelId);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/parcel/{parcelId}/total-value")
    @Operation(
            summary = "Calculate parcel total value",
            description = "Returns the total value of all products in a parcel"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Total value calculated successfully"),
            @ApiResponse(responseCode = "404", description = "Parcel not found")
    })
    public ResponseEntity<BigDecimal> calculateParcelTotalValue(
            @Parameter(description = "Parcel ID", required = true)
            @PathVariable String parcelId) {
        log.info("REST: Calculating total value for parcel ID: {}", parcelId);
        BigDecimal totalValue = parcelProductService.calculateParcelTotalValue(parcelId);
        return ResponseEntity.ok(totalValue);
    }

    @GetMapping("/parcel/{parcelId}/count")
    @Operation(
            summary = "Count products in parcel",
            description = "Returns the number of different products in a parcel"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Count retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Parcel not found")
    })
    public ResponseEntity<Long> countProductsInParcel(
            @Parameter(description = "Parcel ID", required = true)
            @PathVariable String parcelId) {
        log.info("REST: Counting products in parcel ID: {}", parcelId);
        Long count = parcelProductService.countProductsInParcel(parcelId);
        return ResponseEntity.ok(count);
    }

    

    @GetMapping("/product/{productId}")
    @Operation(
            summary = "Get parcels containing a product",
            description = "Retrieves all parcels that include a specific product"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Parcels retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<List<ParcelProductResponseDTO>> getParcelsByProductId(
            @Parameter(description = "Product ID", required = true)
            @PathVariable String productId) {
        log.info("REST: Finding parcels containing product ID: {}", productId);
        List<ParcelProductResponseDTO> parcels = parcelProductService.findByProductId(productId);
        return ResponseEntity.ok(parcels);
    }

    @GetMapping("/product/{productId}/total-quantity")
    @Operation(
            summary = "Calculate total quantity shipped",
            description = "Returns the total quantity of a product shipped across all parcels"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Total quantity calculated successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<Long> calculateTotalQuantityShipped(
            @Parameter(description = "Product ID", required = true)
            @PathVariable String productId) {
        log.info("REST: Calculating total quantity shipped for product ID: {}", productId);
        Long totalQuantity = parcelProductService.calculateTotalQuantityShipped(productId);
        return ResponseEntity.ok(totalQuantity);
    }

    @GetMapping("/product/{productId}/revenue")
    @Operation(
            summary = "Calculate product revenue",
            description = "Returns the total revenue generated by a product"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Revenue calculated successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<BigDecimal> calculateProductRevenue(
            @Parameter(description = "Product ID", required = true)
            @PathVariable String productId) {
        log.info("REST: Calculating revenue for product ID: {}", productId);
        BigDecimal revenue = parcelProductService.calculateProductRevenue(productId);
        return ResponseEntity.ok(revenue);
    }

    @GetMapping("/product/{productId}/average-price")
    @Operation(
            summary = "Calculate average price",
            description = "Returns the average price paid for a product (useful for discount analysis)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Average price calculated successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<BigDecimal> calculateAveragePrice(
            @Parameter(description = "Product ID", required = true)
            @PathVariable String productId) {
        log.info("REST: Calculating average price for product ID: {}", productId);
        BigDecimal avgPrice = parcelProductService.calculateAveragePrice(productId);
        return ResponseEntity.ok(avgPrice);
    }

    

    @GetMapping("/analytics/bulk-orders")
    @Operation(
            summary = "Get bulk orders",
            description = "Retrieves orders with quantity above a threshold"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bulk orders retrieved successfully")
    })
    public ResponseEntity<List<ParcelProductResponseDTO>> getBulkOrders(
            @Parameter(description = "Minimum quantity threshold", required = true)
            @RequestParam Integer minQuantity) {
        log.info("REST: Finding bulk orders with min quantity: {}", minQuantity);
        List<ParcelProductResponseDTO> bulkOrders = parcelProductService.findBulkOrders(minQuantity);
        return ResponseEntity.ok(bulkOrders);
    }

    @GetMapping("/analytics/discounted")
    @Operation(
            summary = "Get discounted products",
            description = "Retrieves products purchased at a price lower than catalog price"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Discounted products retrieved successfully")
    })
    public ResponseEntity<List<ParcelProductResponseDTO>> getDiscountedProducts() {
        log.info("REST: Finding discounted products");
        List<ParcelProductResponseDTO> discounted = parcelProductService.findDiscountedProducts();
        return ResponseEntity.ok(discounted);
    }

    @GetMapping("/analytics/total-revenue")
    @Operation(
            summary = "Get total revenue",
            description = "Returns the total revenue across all parcels"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Total revenue calculated successfully")
    })
    public ResponseEntity<BigDecimal> getTotalRevenue() {
        log.info("REST: Calculating total revenue");
        BigDecimal totalRevenue = parcelProductService.calculateTotalRevenue();
        return ResponseEntity.ok(totalRevenue);
    }

    @GetMapping("/analytics/total-items")
    @Operation(
            summary = "Get total items shipped",
            description = "Returns the total number of items shipped across all parcels"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Total items calculated successfully")
    })
    public ResponseEntity<Long> getTotalItemsShipped() {
        log.info("REST: Calculating total items shipped");
        Long totalItems = parcelProductService.calculateTotalItemsShipped();
        return ResponseEntity.ok(totalItems);
    }

    @GetMapping("/analytics/distinct-products/count")
    @Operation(
            summary = "Count distinct products shipped",
            description = "Returns the number of different products that have been shipped"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
    })
    public ResponseEntity<Long> countDistinctProductsShipped() {
        log.info("REST: Counting distinct products shipped");
        Long count = parcelProductService.countDistinctProductsShipped();
        return ResponseEntity.ok(count);
    }
}
