package com.logismart.logismartv2.dto.parcel;

import com.logismart.logismartv2.entity.ParcelPriority;
import com.logismart.logismartv2.entity.ParcelStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParcelUpdateDTO {

    @NotNull(message = "Parcel ID is required for update")
    private String id;

    @Size(min = 2, max = 500, message = "Description must be between 2 and 500 characters")
    private String description;

    @DecimalMin(value = "0.01", message = "Weight must be greater than 0")
    private BigDecimal weight;

    private ParcelPriority priority;

    @Size(min = 2, max = 100, message = "Destination city must be between 2 and 100 characters")
    private String destinationCity;

    private ParcelStatus status;

    private String deliveryPersonId;

    private String zoneId;
}
