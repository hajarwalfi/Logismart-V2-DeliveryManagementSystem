package com.logismart.logismartv2.dto.zone;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ZoneStatsDTO {

    private String zoneId;

    private String zoneName;

    private Long totalParcels;

    private Double totalWeight;

    private Long inTransitParcels;

    private Long deliveredParcels;

    private Long unassignedParcels;
}
