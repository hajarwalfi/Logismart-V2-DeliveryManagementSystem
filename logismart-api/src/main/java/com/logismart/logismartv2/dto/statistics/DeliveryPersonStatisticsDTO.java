package com.logismart.logismartv2.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryPersonStatisticsDTO {

    private String deliveryPersonId;

    private String deliveryPersonName;

    private String zoneName;

    private Long totalParcels;

    private BigDecimal totalWeight;

    private BigDecimal averageWeight;

    private Long parcelsCreated;

    private Long parcelsCollected;

    private Long parcelsInStock;

    private Long parcelsInTransit;

    private Long parcelsDelivered;

    private Double deliveryRate;
}
