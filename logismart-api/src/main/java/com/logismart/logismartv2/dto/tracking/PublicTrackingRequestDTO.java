package com.logismart.logismartv2.dto.tracking;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for public parcel tracking request.
 * Recipients can track their parcels using parcel ID and their email.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicTrackingRequestDTO {

    @NotBlank(message = "Parcel ID is required")
    private String parcelId;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
}
