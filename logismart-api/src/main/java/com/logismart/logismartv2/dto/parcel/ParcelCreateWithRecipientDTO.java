package com.logismart.logismartv2.dto.parcel;

import com.logismart.logismartv2.entity.ParcelPriority;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for creating a parcel with recipient info inline.
 * Used by CLIENT role - recipient is created automatically.
 * senderClientId is auto-filled from authenticated user.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParcelCreateWithRecipientDTO {

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

    @NotNull(message = "Recipient information is required")
    @Valid
    private RecipientInfo recipient;

    @NotEmpty(message = "At least one product is required")
    @Valid
    private List<ParcelProductItemDTO> products;

    /**
     * Nested class for recipient information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecipientInfo {
        @NotBlank(message = "First name is required")
        @Size(max = 50, message = "First name must not exceed 50 characters")
        private String firstName;

        @NotBlank(message = "Last name is required")
        @Size(max = 50, message = "Last name must not exceed 50 characters")
        private String lastName;

        @NotBlank(message = "Phone is required")
        @Size(max = 20, message = "Phone must not exceed 20 characters")
        private String phone;

        @NotBlank(message = "Recipient email is required")
        @Email(message = "Invalid email format")
        @Size(max = 100, message = "Email must not exceed 100 characters")
        private String email;

        @NotBlank(message = "Address is required")
        @Size(max = 255, message = "Address must not exceed 255 characters")
        private String address;
    }
}
