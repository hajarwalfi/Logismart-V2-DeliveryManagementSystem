package com.logismart.logismartv2.controller;

import com.logismart.logismartv2.dto.zone.ZoneCreateDTO;
import com.logismart.logismartv2.dto.zone.ZoneResponseDTO;
import com.logismart.logismartv2.dto.zone.ZoneStatsDTO;
import com.logismart.logismartv2.dto.zone.ZoneUpdateDTO;
import com.logismart.logismartv2.service.ZoneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/zones")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Zone Management", description = "APIs for managing delivery zones")
public class ZoneController {

    private final ZoneService zoneService;

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Create a new zone",
            description = "Creates a new delivery zone with name, postal code, and description"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Zone created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Zone with same name or postal code already exists")
    })
    public ResponseEntity<ZoneResponseDTO> createZone(
            @Valid @RequestBody ZoneCreateDTO zoneCreateDTO) {
        log.info("REST: Creating new zone with name: {}", zoneCreateDTO.getName());
        ZoneResponseDTO created = zoneService.create(zoneCreateDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Get zone by ID",
            description = "Retrieves a zone by its unique identifier"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Zone found"),
            @ApiResponse(responseCode = "404", description = "Zone not found")
    })
    public ResponseEntity<ZoneResponseDTO> getZoneById(
            @Parameter(description = "Zone ID", required = true)
            @PathVariable String id) {
        log.info("REST: Finding zone by ID: {}", id);
        ZoneResponseDTO zone = zoneService.findById(id);
        return ResponseEntity.ok(zone);
    }

    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Get all zones",
            description = "Retrieves a list of all delivery zones"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of zones retrieved successfully")
    })
    public ResponseEntity<List<ZoneResponseDTO>> getAllZones() {
        log.info("REST: Finding all zones");
        List<ZoneResponseDTO> zones = zoneService.findAll();
        return ResponseEntity.ok(zones);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Update a zone",
            description = "Updates an existing zone's information"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Zone updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Zone not found"),
            @ApiResponse(responseCode = "409", description = "Zone with same name already exists")
    })
    public ResponseEntity<ZoneResponseDTO> updateZone(
            @Parameter(description = "Zone ID", required = true)
            @PathVariable String id,
            @Valid @RequestBody ZoneUpdateDTO zoneUpdateDTO) {
        log.info("REST: Updating zone with ID: {}", id);

        
        zoneUpdateDTO.setId(id);

        ZoneResponseDTO updated = zoneService.update(zoneUpdateDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Delete a zone",
            description = "Deletes a zone by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Zone deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Zone not found")
    })
    public ResponseEntity<Void> deleteZone(
            @Parameter(description = "Zone ID", required = true)
            @PathVariable String id) {
        log.info("REST: Deleting zone with ID: {}", id);
        zoneService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Search zones by name",
            description = "Searches for zones by name (case-insensitive)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully")
    })
    public ResponseEntity<List<ZoneResponseDTO>> searchZones(
            @Parameter(description = "Search keyword", required = true)
            @RequestParam String keyword) {
        log.info("REST: Searching zones with keyword: {}", keyword);
        List<ZoneResponseDTO> zones = zoneService.searchByName(keyword);
        return ResponseEntity.ok(zones);
    }

    @GetMapping("/by-name/{name}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Get zone by name",
            description = "Retrieves a zone by its exact name"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Zone found"),
            @ApiResponse(responseCode = "404", description = "Zone not found")
    })
    public ResponseEntity<ZoneResponseDTO> getZoneByName(
            @Parameter(description = "Zone name", required = true)
            @PathVariable String name) {
        log.info("REST: Finding zone by name: {}", name);
        ZoneResponseDTO zone = zoneService.findByName(name);
        return ResponseEntity.ok(zone);
    }

    @GetMapping("/by-postal-code/{postalCode}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Get zone by postal code",
            description = "Retrieves a zone by its postal code"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Zone found"),
            @ApiResponse(responseCode = "404", description = "Zone not found")
    })
    public ResponseEntity<ZoneResponseDTO> getZoneByPostalCode(
            @Parameter(description = "Postal code", required = true)
            @PathVariable String postalCode) {
        log.info("REST: Finding zone by postal code: {}", postalCode);
        ZoneResponseDTO zone = zoneService.findByPostalCode(postalCode);
        return ResponseEntity.ok(zone);
    }

    @GetMapping("/{id}/delivery-persons/count")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Count delivery persons in zone",
            description = "Returns the number of delivery persons assigned to this zone"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Count retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Zone not found")
    })
    public ResponseEntity<Long> countDeliveryPersons(
            @Parameter(description = "Zone ID", required = true)
            @PathVariable String id) {
        log.info("REST: Counting delivery persons for zone ID: {}", id);
        Long count = zoneService.countDeliveryPersons(id);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/{id}/parcels/count")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Count parcels in zone",
            description = "Returns the number of parcels in this zone"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Count retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Zone not found")
    })
    public ResponseEntity<Long> countParcels(
            @Parameter(description = "Zone ID", required = true)
            @PathVariable String id) {
        log.info("REST: Counting parcels for zone ID: {}", id);
        Long count = zoneService.countParcels(id);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/{id}/stats")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Get zone statistics",
            description = "US-12: Calculates total parcels, total weight, in-transit parcels, delivered parcels, " +
                    "and unassigned parcels for a zone. Used by logistics manager to balance workload across zones."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Zone not found")
    })
    public ResponseEntity<ZoneStatsDTO> getStats(
            @Parameter(description = "Zone ID", required = true)
            @PathVariable String id) {
        log.info("REST: Getting statistics for zone ID: {}", id);
        ZoneStatsDTO stats = zoneService.getStats(id);
        return ResponseEntity.ok(stats);
    }
}
