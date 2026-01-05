package com.logismart.logismartv2.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotBlank(message = "Product name is required")
    @Size(max = 150, message = "Product name must not exceed 150 characters")
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    
    
    @Size(max = 100, message = "Category must not exceed 100 characters")
    @Column(name = "category", nullable = true, length = 100)
    private String category;

    
    
    
    @NotNull(message = "Weight is required")
    @DecimalMin(value = "0.01", message = "Weight must be at least 0.01 kg")
    @DecimalMax(value = "999.99", message = "Weight must not exceed 999.99 kg")
    @Column(name = "weight", nullable = false, precision = 10, scale = 2)
    private BigDecimal weight;

    
    
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be at least 0.01 MAD")
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    
    
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ParcelProduct> parcelProducts = new ArrayList<>();

    public Product(String id, String name, String category, BigDecimal weight, BigDecimal price) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.weight = weight;
        this.price = price;
        this.parcelProducts = new ArrayList<>();
    }

    public String getFormattedWeight() {
        return String.format("%.2f kg", weight);
    }

    public String getFormattedPrice() {
        return String.format("%.2f MAD", price);
    }

    public boolean hasCategory() {
        return category != null && !category.trim().isEmpty();
    }
}
