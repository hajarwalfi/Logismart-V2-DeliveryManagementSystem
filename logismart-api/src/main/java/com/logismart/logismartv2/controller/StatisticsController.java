package com.logismart.logismartv2.controller;

import com.logismart.logismartv2.dto.statistics.DeliveryPersonStatisticsDTO;
import com.logismart.logismartv2.dto.statistics.GlobalStatisticsDTO;
import com.logismart.logismartv2.dto.statistics.ZoneStatisticsDTO;
import com.logismart.logismartv2.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('MANAGER')")
@Tag(name = "Statistics & Analytics", description = "APIs for system statistics and analytics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/global")
    @Operation(
            summary = "Get global system statistics",
            description = "Returns an overview of the entire delivery system including counts, " +
                    "parcels by status/priority, and key metrics"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    })
    public ResponseEntity<GlobalStatisticsDTO> getGlobalStatistics() {
        log.info("REST: Getting global system statistics");
        GlobalStatisticsDTO stats = statisticsService.getGlobalStatistics();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/delivery-person/{id}")
    @Operation(
            summary = "Get delivery person statistics",
            description = "Returns detailed statistics for a specific delivery person including " +
                    "parcel count, total weight, status breakdown, and delivery rate"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Delivery person not found")
    })
    public ResponseEntity<DeliveryPersonStatisticsDTO> getDeliveryPersonStatistics(
            @Parameter(description = "Delivery person ID", required = true)
            @PathVariable String id) {
        log.info("REST: Getting statistics for delivery person ID: {}", id);
        DeliveryPersonStatisticsDTO stats = statisticsService.getDeliveryPersonStatistics(id);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/delivery-person")
    @Operation(
            summary = "Get statistics for all delivery persons",
            description = "Returns statistics for all delivery persons. " +
                    "Useful for workload comparison and balancing"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    })
    public ResponseEntity<List<DeliveryPersonStatisticsDTO>> getAllDeliveryPersonStatistics() {
        log.info("REST: Getting statistics for all delivery persons");
        List<DeliveryPersonStatisticsDTO> stats = statisticsService.getAllDeliveryPersonStatistics();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/zone/{id}")
    @Operation(
            summary = "Get zone statistics",
            description = "Returns detailed statistics for a specific zone including " +
                    "parcel count, total weight, delivery person count, and breakdowns"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Zone not found")
    })
    public ResponseEntity<ZoneStatisticsDTO> getZoneStatistics(
            @Parameter(description = "Zone ID", required = true)
            @PathVariable String id) {
        log.info("REST: Getting statistics for zone ID: {}", id);
        ZoneStatisticsDTO stats = statisticsService.getZoneStatistics(id);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/zone")
    @Operation(
            summary = "Get statistics for all zones",
            description = "Returns statistics for all zones. " +
                    "Useful for zone comparison and resource allocation"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    })
    public ResponseEntity<List<ZoneStatisticsDTO>> getAllZoneStatistics() {
        log.info("REST: Getting statistics for all zones");
        List<ZoneStatisticsDTO> stats = statisticsService.getAllZoneStatistics();
        return ResponseEntity.ok(stats);
    }
}
