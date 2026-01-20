package com.logismart.logismartv2.dto.deliveryperson;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryPersonStatsDTO {

    private String deliveryPersonId;

    private String deliveryPersonName;

    private Long totalParcels;

    private Double totalWeight;

    private Long activeParcels;

    private Long deliveredParcels;

    private Long inTransitParcels;

    // Monthly statistics
    private Long deliveredThisMonth;

    private Long totalThisMonth;

    // Success rate (percentage of delivered parcels)
    private Double successRate;

    // Average deliveries per day this month
    private Double avgDeliveriesPerDay;

    // Collected parcels (status = COLLECTED)
    private Long collectedParcels;

    // Parcels in stock
    private Long inStockParcels;

    // Constructor without new fields for backward compatibility
    public DeliveryPersonStatsDTO(String deliveryPersonId, String deliveryPersonName,
                                   Long totalParcels, Double totalWeight, Long activeParcels,
                                   Long deliveredParcels, Long inTransitParcels) {
        this.deliveryPersonId = deliveryPersonId;
        this.deliveryPersonName = deliveryPersonName;
        this.totalParcels = totalParcels;
        this.totalWeight = totalWeight;
        this.activeParcels = activeParcels;
        this.deliveredParcels = deliveredParcels;
        this.inTransitParcels = inTransitParcels;
    }
}
