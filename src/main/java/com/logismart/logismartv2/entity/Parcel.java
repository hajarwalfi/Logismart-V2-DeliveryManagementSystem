package com.logismart.logismartv2.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "parcel")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Parcel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    
    
    @Size(max = 255, message = "Description must not exceed 255 characters")
    @Column(name = "description", length = 255)
    private String description;

    
    
    
    @NotNull(message = "Weight is required")
    @DecimalMin(value = "0.01", message = "Weight must be at least 0.01 kg")
    @DecimalMax(value = "999.99", message = "Weight must not exceed 999.99 kg")
    @Column(name = "weight", nullable = false, precision = 10, scale = 2)
    private BigDecimal weight;

    
    
    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private ParcelStatus status;

    
    
    
    @NotNull(message = "Priority is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private ParcelPriority priority;

    
    
    @NotBlank(message = "Destination city is required")
    @Size(max = 100, message = "Destination city must not exceed 100 characters")
    @Column(name = "destination_city", nullable = false, length = 100)
    private String destinationCity;

    
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    

    
    
    @NotNull(message = "Sender client is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_client_id", nullable = false)
    private SenderClient senderClient;

    
    
    @NotNull(message = "Recipient is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private Recipient recipient;

    
    
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_person_id", nullable = true)
    private DeliveryPerson deliveryPerson;

    
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = true)
    private Zone zone;

    
    
    
    @OneToMany(mappedBy = "parcel", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<DeliveryHistory> deliveryHistories = new ArrayList<>();

    
    
    
    @OneToMany(mappedBy = "parcel", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ParcelProduct> parcelProducts = new ArrayList<>();

    public Parcel(String id, String description, BigDecimal weight, ParcelStatus status,
                  ParcelPriority priority, String destinationCity) {
        this.id = id;
        this.description = description;
        this.weight = weight;
        this.status = status;
        this.priority = priority;
        this.destinationCity = destinationCity;
        this.deliveryHistories = new ArrayList<>();
        this.parcelProducts = new ArrayList<>();
    }

    

    public String getFormattedWeight() {
        return String.format("%.2f kg", weight);
    }

    public boolean isDelivered() {
        return status == ParcelStatus.DELIVERED;
    }

    public boolean isInProgress() {
        return status != null && status.isInProgress();
    }

    public boolean isHighPriority() {
        return priority != null && priority.isHighPriority();
    }

    public boolean isAssignedToDeliveryPerson() {
        return deliveryPerson != null;
    }

    public boolean hasZone() {
        return zone != null;
    }

    public String getSenderName() {
        return senderClient != null ? senderClient.getFullName() : "Unknown Sender";
    }

    public String getRecipientName() {
        return recipient != null ? recipient.getFullName() : "Unknown Recipient";
    }

    public String getDeliveryPersonName() {
        return deliveryPerson != null ? deliveryPerson.getFullName() : "Unassigned";
    }

    public String getZoneName() {
        return zone != null ? zone.getName() : "No Zone";
    }

    public String getStatusDisplay() {
        return status != null ? status.getDisplayName() : "Unknown";
    }

    public String getPriorityDisplay() {
        return priority != null ? priority.getDisplayName() : "Unknown";
    }

    public int getProductCount() {
        return parcelProducts != null ? parcelProducts.size() : 0;
    }

    public BigDecimal getTotalValue() {
        if (parcelProducts == null || parcelProducts.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return parcelProducts.stream()
                .map(pp -> pp.getPrice().multiply(BigDecimal.valueOf(pp.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public String getFormattedTotalValue() {
        return String.format("%.2f MAD", getTotalValue());
    }

    public boolean hasDescription() {
        return description != null && !description.trim().isEmpty();
    }
}
