package com.logismart.logismartv2.dto.parcel;

import com.logismart.logismartv2.entity.ParcelPriority;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParcelCreateDTO {

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @NotNull(message = "Weight is required")
    @DecimalMin(value = "0.01", message = "Weight must be at least 0.01 kg")
    @DecimalMax(value = "999.99", message = "Weight must not exceed 999.99 kg")
    private BigDecimal weight;

    @NotNull(message = "Priority is required")
    private ParcelPriority priority;

    @NotBlank(message = "Destination city is required")
    @Size(max = 100, message = "Destination city must not exceed 100 characters")
    private String destinationCity;

    // For CLIENT role: auto-filled by backend from authenticated user
    // For MANAGER role: must be provided in request
    private String senderClientId;

    @NotNull(message = "Recipient ID is required")
    private String recipientId;

    @NotEmpty(message = "At least one product is required")
    @Valid
    private List<ParcelProductItemDTO> products;
}
