package com.logismart.logismartv2.dto.deliveryhistory;

import com.logismart.logismartv2.entity.ParcelStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryHistoryCreateDTO {

    @NotNull(message = "Parcel ID is required")
    private String parcelId;

    @NotNull(message = "Status is required")
    private ParcelStatus status;

    @Size(max = 1000, message = "Comment must not exceed 1000 characters")
    private String comment;
}
