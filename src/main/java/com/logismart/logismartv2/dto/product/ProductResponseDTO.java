package com.logismart.logismartv2.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDTO {

    private String id;

    private String name;

    private String category;

    private Boolean hasCategory;

    private BigDecimal weight;

    private String formattedWeight;

    private BigDecimal price;

    private String formattedPrice;

    public ProductResponseDTO(String id, String name, String category,
                               BigDecimal weight, BigDecimal price) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.hasCategory = category != null && !category.trim().isEmpty();
        this.weight = weight;
        this.formattedWeight = String.format("%.2f kg", weight);
        this.price = price;
        this.formattedPrice = String.format("%.2f MAD", price);
    }
}
