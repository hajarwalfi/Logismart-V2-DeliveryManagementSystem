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
}
