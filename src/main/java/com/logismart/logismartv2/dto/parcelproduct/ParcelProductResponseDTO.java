package com.logismart.logismartv2.dto.parcelproduct;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParcelProductResponseDTO {

    private String id;

    private String parcelId;

    private String productId;

    private String productName;

    private Integer quantity;

    private BigDecimal price;

    private String formattedUnitPrice;

    private BigDecimal totalPrice;

    private String formattedTotalPrice;

    private LocalDateTime addedAt;

    private Boolean isBulkItem;

    private String lineItemSummary;

    public ParcelProductResponseDTO(String id, String parcelId, String productId,
                                     String productName, Integer quantity,
                                     BigDecimal price, LocalDateTime addedAt) {
        this.id = id;
        this.parcelId = parcelId;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
        this.formattedUnitPrice = String.format("%.2f MAD", price);
        this.totalPrice = price.multiply(BigDecimal.valueOf(quantity));
        this.formattedTotalPrice = String.format("%.2f MAD", this.totalPrice);
        this.addedAt = addedAt;
        this.isBulkItem = quantity > 1;

        
        if (quantity == 1) {
            this.lineItemSummary = String.format("1x %s @ %s",
                    productName, formattedUnitPrice);
        } else {
            this.lineItemSummary = String.format("%dx %s @ %s each = %s total",
                    quantity, productName, formattedUnitPrice, formattedTotalPrice);
        }
    }
}
