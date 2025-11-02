package com.logismart.logismartv2.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "parcel_product")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParcelProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    
    
    @NotNull(message = "Parcel is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parcel_id", nullable = false)
    private Parcel parcel;

    
    
    @NotNull(message = "Product is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    
    
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    
    
    
    
    
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", message = "Price must be non-negative")
    @DecimalMax(value = "9999999.99", message = "Price must not exceed 9,999,999.99")
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    
    
    
    @CreationTimestamp
    @Column(name = "added_at", nullable = false, updatable = false)
    private LocalDateTime addedAt;

    public ParcelProduct(String id, Integer quantity, BigDecimal price, LocalDateTime addedAt) {
        this.id = id;
        this.quantity = quantity;
        this.price = price;
        this.addedAt = addedAt;
    }

    public ParcelProduct(Parcel parcel, Product product, Integer quantity, BigDecimal price) {
        this.parcel = parcel;
        this.product = product;
        this.quantity = quantity;
        this.price = price;
        
    }

    public BigDecimal getTotalPrice() {
        if (quantity == null || price == null) {
            return BigDecimal.ZERO;
        }
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    public String getFormattedTotalPrice() {
        return String.format("%.2f MAD", getTotalPrice());
    }

    public String getFormattedUnitPrice() {
        return String.format("%.2f MAD", price != null ? price : BigDecimal.ZERO);
    }

    public String getProductName() {
        return product != null ? product.getName() : "Unknown Product";
    }

    public String getParcelId() {
        return parcel != null ? parcel.getId() : null;
    }

    public String getProductId() {
        return product != null ? product.getId() : null;
    }

    public boolean isBulkItem() {
        return quantity != null && quantity > 1;
    }

    public String getLineItemSummary() {
        String productName = getProductName();
        String unitPrice = getFormattedUnitPrice();
        String totalPrice = getFormattedTotalPrice();

        if (quantity == 1) {
            return String.format("1x %s @ %s", productName, unitPrice);
        } else {
            return String.format("%dx %s @ %s each = %s total",
                    quantity, productName, unitPrice, totalPrice);
        }
    }
}
