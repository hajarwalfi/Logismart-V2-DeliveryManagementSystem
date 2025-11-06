package com.logismart.logismartv2.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GlobalStatisticsDTO {

    private Long totalParcels;

    private BigDecimal totalWeight;

    private Long totalZones;

    private Long totalDeliveryPersons;

    private Long totalSenderClients;

    private Long totalRecipients;

    private Long totalProducts;

    private Map<String, Long> parcelsByStatus;

    private Map<String, Long> parcelsByPriority;

    private Long unassignedParcels;

    private Long highPriorityPending;

    private Double averageParcelsPerDeliveryPerson;

    private BigDecimal averageWeight;
}
