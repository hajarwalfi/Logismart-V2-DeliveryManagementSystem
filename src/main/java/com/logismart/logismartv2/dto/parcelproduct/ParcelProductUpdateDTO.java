package com.logismart.logismartv2.dto.parcelproduct;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParcelProductUpdateDTO {

    @NotNull(message = "ParcelProduct ID is required for update")
    private String id;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", message = "Price must be non-negative")
    @DecimalMax(value = "9999999.99", message = "Price must not exceed 9,999,999.99 MAD")
    private BigDecimal price;
}
