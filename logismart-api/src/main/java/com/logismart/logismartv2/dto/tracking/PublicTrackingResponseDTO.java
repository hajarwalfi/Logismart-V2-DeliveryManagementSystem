package com.logismart.logismartv2.dto.tracking;

import com.logismart.logismartv2.dto.deliveryhistory.DeliveryHistoryResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for public parcel tracking response.
 * Contains parcel info and delivery history - no sensitive data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicTrackingResponseDTO {

    private String parcelId;
    private String description;
    private String status;
    private String statusDisplay;
    private String priority;
    private String priorityDisplay;
    private BigDecimal weight;
    private String destinationCity;

    // Recipient info (only name, no contact details for privacy)
    private String recipientName;

    // Sender info (only company name)
    private String senderName;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime estimatedDelivery;

    // Delivery history
    private List<DeliveryHistoryResponseDTO> history;
}
