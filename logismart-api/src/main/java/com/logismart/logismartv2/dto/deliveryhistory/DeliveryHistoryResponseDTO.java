package com.logismart.logismartv2.dto.deliveryhistory;

import com.logismart.logismartv2.entity.ParcelStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryHistoryResponseDTO {

    private String id;

    private String parcelId;

    private ParcelStatus status;

    private String statusDisplay;

    private LocalDateTime changedAt;

    private String formattedChangedAt;

    private String comment;

    private Boolean hasComment;

    private String summary;

    private String detailedSummary;

    public DeliveryHistoryResponseDTO(String id, String parcelId, ParcelStatus status,
                                       LocalDateTime changedAt, String comment) {
        this.id = id;
        this.parcelId = parcelId;
        this.status = status;
        this.statusDisplay = status.getDisplayName();
        this.changedAt = changedAt;
        this.formattedChangedAt = formatDateTime(changedAt);
        this.comment = comment;
        this.hasComment = comment != null && !comment.trim().isEmpty();
        this.summary = String.format("%s at %s", statusDisplay, this.formattedChangedAt);
        this.detailedSummary = hasComment ? summary + " - " + comment : summary;
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "Unknown time";
        }
        java.time.format.DateTimeFormatter formatter =
                java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
        return dateTime.format(formatter);
    }
}
