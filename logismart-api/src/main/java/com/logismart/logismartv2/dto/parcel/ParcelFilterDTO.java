package com.logismart.logismartv2.dto.parcel;

import com.logismart.logismartv2.entity.ParcelPriority;
import com.logismart.logismartv2.entity.ParcelStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParcelFilterDTO {

    private ParcelStatus status;

    private ParcelPriority priority;

    private String zoneId;

    private String destinationCity;

    private String deliveryPersonId;

    private String senderClientId;

    private String recipientId;

    private LocalDateTime createdAfter;

    private LocalDateTime createdBefore;

    private Boolean unassignedOnly;
}
