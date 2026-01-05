package com.logismart.logismartv2.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "delivery_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    
    
    
    @NotNull(message = "Parcel is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parcel_id", nullable = false)
    private Parcel parcel;

    
    
    
    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private ParcelStatus status;

    
    
    
    @CreationTimestamp
    @Column(name = "changed_at", nullable = false, updatable = false)
    private LocalDateTime changedAt;

    
    
    
    
    
    
    @Size(max = 1000, message = "Comment must not exceed 1000 characters")
    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    public DeliveryHistory(String id, ParcelStatus status, LocalDateTime changedAt, String comment) {
        this.id = id;
        this.status = status;
        this.changedAt = changedAt;
        this.comment = comment;
    }

    public DeliveryHistory(Parcel parcel, ParcelStatus status, String comment) {
        this.parcel = parcel;
        this.status = status;
        this.comment = comment;
        
    }

    public String getFormattedChangedAt() {
        if (changedAt == null) {
            return "Unknown time";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
        return changedAt.format(formatter);
    }

    public String getStatusDisplay() {
        return status != null ? status.getDisplayName() : "Unknown";
    }

    public boolean hasComment() {
        return comment != null && !comment.trim().isEmpty();
    }

    public String getParcelId() {
        return parcel != null ? parcel.getId() : null;
    }

    public String getSummary() {
        return String.format("%s at %s", getStatusDisplay(), getFormattedChangedAt());
    }

    public String getDetailedSummary() {
        String summary = getSummary();
        if (hasComment()) {
            summary += " - " + comment;
        }
        return summary;
    }
}
