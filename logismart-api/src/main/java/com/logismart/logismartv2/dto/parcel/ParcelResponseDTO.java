package com.logismart.logismartv2.dto.parcel;

import com.logismart.logismartv2.entity.ParcelPriority;
import com.logismart.logismartv2.entity.ParcelStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParcelResponseDTO {

    

    private String id;

    private String description;

    private BigDecimal weight;

    private String formattedWeight;

    private ParcelStatus status;

    private String statusDisplay;

    private ParcelPriority priority;

    private String priorityDisplay;

    private String destinationCity;

    private LocalDateTime createdAt;

    

    private String senderClientId;

    private String senderClientName;

    private String recipientId;

    private String recipientName;

    private String recipientPhone;

    private String recipientEmail;

    private String recipientAddress;

    private String deliveryPersonId;

    private String deliveryPersonName;

    private String zoneId;

    private String zoneName;

    

    private BigDecimal totalValue;

    private String formattedTotalValue;

    private Integer productCount;

    

    private Boolean isDelivered;

    private Boolean isInProgress;

    private Boolean isHighPriority;

    private Boolean isAssignedToDeliveryPerson;

    public ParcelResponseDTO(String id, String description, BigDecimal weight,
                              ParcelStatus status, ParcelPriority priority,
                              String destinationCity, LocalDateTime createdAt,
                              String senderClientId, String senderClientName,
                              String recipientId, String recipientName,
                              String deliveryPersonId, String deliveryPersonName,
                              String zoneId, String zoneName,
                              BigDecimal totalValue, Integer productCount) {
        this.id = id;
        this.description = description;
        this.weight = weight;
        this.formattedWeight = String.format("%.2f kg", weight);
        this.status = status;
        this.statusDisplay = status.getDisplayName();
        this.priority = priority;
        this.priorityDisplay = priority.getDisplayName();
        this.destinationCity = destinationCity;
        this.createdAt = createdAt;
        this.senderClientId = senderClientId;
        this.senderClientName = senderClientName;
        this.recipientId = recipientId;
        this.recipientName = recipientName;
        this.deliveryPersonId = deliveryPersonId;
        this.deliveryPersonName = deliveryPersonName;
        this.zoneId = zoneId;
        this.zoneName = zoneName;
        this.totalValue = totalValue;
        this.formattedTotalValue = String.format("%.2f MAD", totalValue);
        this.productCount = productCount;
        this.isDelivered = status == ParcelStatus.DELIVERED;
        this.isInProgress = status.isInProgress();
        this.isHighPriority = priority.isHighPriority();
        this.isAssignedToDeliveryPerson = deliveryPersonId != null;
    }
}
