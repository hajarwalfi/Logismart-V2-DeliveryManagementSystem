package com.logismart.logismartv2.controller;

import com.logismart.logismartv2.dto.deliveryperson.DeliveryPersonCreateDTO;
import com.logismart.logismartv2.dto.deliveryperson.DeliveryPersonResponseDTO;
import com.logismart.logismartv2.dto.deliveryperson.DeliveryPersonStatsDTO;
import com.logismart.logismartv2.dto.deliveryperson.DeliveryPersonUpdateDTO;
import com.logismart.logismartv2.dto.parcel.ParcelResponseDTO;
import com.logismart.logismartv2.service.DeliveryPersonService;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/delivery-persons")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Delivery Person Management", description = "APIs for managing delivery personnel")
public class DeliveryPersonController {

    private final DeliveryPersonService deliveryPersonService;

    // ==================== LIVREUR SELF-SERVICE ENDPOINTS ====================

    @GetMapping("/me")
    @PreAuthorize("hasRole('LIVREUR')")
    @Operation(
            summary = "Get current delivery person profile",
            description = "Returns the profile information of the currently authenticated delivery person"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Delivery person profile not found for this user")
    })
    public ResponseEntity<DeliveryPersonResponseDTO> getMyProfile() {
        String userId = getCurrentUserId();
        log.info("REST: Getting profile for authenticated delivery person with user ID: {}", userId);
        DeliveryPersonResponseDTO profile = deliveryPersonService.findByUserId(userId);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/me/stats")
    @PreAuthorize("hasRole('LIVREUR')")
    @Operation(
            summary = "Get current delivery person statistics",
            description = "Returns statistics including total parcels, deliveries this month, success rate, and more"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Delivery person profile not found for this user")
    })
    public ResponseEntity<DeliveryPersonStatsDTO> getMyStats() {
        String userId = getCurrentUserId();
        log.info("REST: Getting statistics for authenticated delivery person with user ID: {}", userId);
        DeliveryPersonStatsDTO stats = deliveryPersonService.getStatsByUserId(userId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/me/history")
    @PreAuthorize("hasRole('LIVREUR')")
    @Operation(
            summary = "Get delivery history",
            description = "Returns the list of all parcels delivered by the current delivery person"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delivery history retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Delivery person profile not found for this user")
    })
    public ResponseEntity<List<ParcelResponseDTO>> getMyDeliveryHistory() {
        String userId = getCurrentUserId();
        log.info("REST: Getting delivery history for authenticated delivery person with user ID: {}", userId);
        List<ParcelResponseDTO> history = deliveryPersonService.getDeliveryHistoryByUserId(userId);
        return ResponseEntity.ok(history);
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    // ==================== MANAGER ENDPOINTS ====================

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Create a new delivery person",
            description = "Creates a new delivery person with unique phone number"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Delivery person created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Phone number already exists")
    })
    public ResponseEntity<DeliveryPersonResponseDTO> createDeliveryPerson(
            @Valid @RequestBody DeliveryPersonCreateDTO dto) {
        log.info("REST: Creating new delivery person with phone: {}", dto.getPhone());
        DeliveryPersonResponseDTO created = deliveryPersonService.create(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Get delivery person by ID",
            description = "Retrieves a delivery person by their unique identifier"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delivery person found"),
            @ApiResponse(responseCode = "404", description = "Delivery person not found")
    })
    public ResponseEntity<DeliveryPersonResponseDTO> getDeliveryPersonById(
            @Parameter(description = "Delivery person ID", required = true)
            @PathVariable String id) {
        log.info("REST: Finding delivery person by ID: {}", id);
        DeliveryPersonResponseDTO deliveryPerson = deliveryPersonService.findById(id);
        return ResponseEntity.ok(deliveryPerson);
    }

    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Get all delivery persons",
            description = "Retrieves a list of all delivery persons"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of delivery persons retrieved successfully")
    })
    public ResponseEntity<List<DeliveryPersonResponseDTO>> getAllDeliveryPersons() {
        log.info("REST: Finding all delivery persons");
        List<DeliveryPersonResponseDTO> deliveryPersons = deliveryPersonService.findAll();
        return ResponseEntity.ok(deliveryPersons);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Update a delivery person",
            description = "Updates an existing delivery person's information"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delivery person updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Delivery person not found"),
            @ApiResponse(responseCode = "409", description = "Phone number already exists")
    })
    public ResponseEntity<DeliveryPersonResponseDTO> updateDeliveryPerson(
            @Parameter(description = "Delivery person ID", required = true)
            @PathVariable String id,
            @Valid @RequestBody DeliveryPersonUpdateDTO dto) {
        log.info("REST: Updating delivery person with ID: {}", id);

        
        dto.setId(id);

        DeliveryPersonResponseDTO updated = deliveryPersonService.update(dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Delete a delivery person",
            description = "Deletes a delivery person by their ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Delivery person deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Delivery person not found")
    })
    public ResponseEntity<Void> deleteDeliveryPerson(
            @Parameter(description = "Delivery person ID", required = true)
            @PathVariable String id) {
        log.info("REST: Deleting delivery person with ID: {}", id);
        deliveryPersonService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/zone/{zoneId}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Get delivery persons by zone",
            description = "Retrieves all delivery persons assigned to a specific zone"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delivery persons retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Zone not found")
    })
    public ResponseEntity<List<DeliveryPersonResponseDTO>> getDeliveryPersonsByZone(
            @Parameter(description = "Zone ID", required = true)
            @PathVariable String zoneId) {
        log.info("REST: Finding delivery persons for zone ID: {}", zoneId);
        List<DeliveryPersonResponseDTO> deliveryPersons = deliveryPersonService.findByZone(zoneId);
        return ResponseEntity.ok(deliveryPersons);
    }

    @GetMapping("/unassigned")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Get unassigned delivery persons",
            description = "Retrieves delivery persons who are not assigned to any zone"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Unassigned delivery persons retrieved successfully")
    })
    public ResponseEntity<List<DeliveryPersonResponseDTO>> getUnassignedDeliveryPersons() {
        log.info("REST: Finding unassigned delivery persons");
        List<DeliveryPersonResponseDTO> deliveryPersons = deliveryPersonService.findUnassigned();
        return ResponseEntity.ok(deliveryPersons);
    }

    @GetMapping("/available")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Get available delivery persons",
            description = "Retrieves delivery persons who are not currently delivering any parcels"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Available delivery persons retrieved successfully")
    })
    public ResponseEntity<List<DeliveryPersonResponseDTO>> getAvailableDeliveryPersons() {
        log.info("REST: Finding available delivery persons");
        List<DeliveryPersonResponseDTO> deliveryPersons = deliveryPersonService.findAvailable();
        return ResponseEntity.ok(deliveryPersons);
    }

    @GetMapping("/available/zone/{zoneId}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Get available delivery persons in zone",
            description = "Retrieves delivery persons who are available and assigned to a specific zone"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Available delivery persons retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Zone not found")
    })
    public ResponseEntity<List<DeliveryPersonResponseDTO>> getAvailableDeliveryPersonsInZone(
            @Parameter(description = "Zone ID", required = true)
            @PathVariable String zoneId) {
        log.info("REST: Finding available delivery persons for zone ID: {}", zoneId);
        List<DeliveryPersonResponseDTO> deliveryPersons = deliveryPersonService.findAvailableInZone(zoneId);
        return ResponseEntity.ok(deliveryPersons);
    }

    @GetMapping("/{id}/parcels/active/count")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Count active parcels",
            description = "Returns the number of active (in-transit) parcels for this delivery person"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Count retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Delivery person not found")
    })
    public ResponseEntity<Long> countActiveParcels(
            @Parameter(description = "Delivery person ID", required = true)
            @PathVariable String id) {
        log.info("REST: Counting active parcels for delivery person ID: {}", id);
        Long count = deliveryPersonService.countActiveParcels(id);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/{id}/parcels/delivered/count")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Count delivered parcels",
            description = "Returns the total number of parcels delivered by this delivery person"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Count retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Delivery person not found")
    })
    public ResponseEntity<Long> countDeliveredParcels(
            @Parameter(description = "Delivery person ID", required = true)
            @PathVariable String id) {
        log.info("REST: Counting delivered parcels for delivery person ID: {}", id);
        Long count = deliveryPersonService.countDeliveredParcels(id);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/{id}/parcels/urgent")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Get urgent parcels for delivery person",
            description = "Retrieves all parcels with URGENT or EXPRESS priority assigned to this delivery person. " +
                    "Helps delivery personnel prioritize their deliveries."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Urgent parcels retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Delivery person not found")
    })
    public ResponseEntity<List<ParcelResponseDTO>> getUrgentParcels(
            @Parameter(description = "Delivery person ID", required = true)
            @PathVariable String id) {
        log.info("REST: Getting urgent parcels for delivery person ID: {}", id);
        List<ParcelResponseDTO> parcels = deliveryPersonService.findUrgentParcels(id);
        return ResponseEntity.ok(parcels);
    }

    @GetMapping("/{id}/stats")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Get delivery person statistics",
            description = "US-12: Calculates total parcels, total weight, active parcels, delivered parcels, " +
                    "and in-transit parcels for a delivery person. Used by logistics manager to balance workload."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Delivery person not found")
    })
    public ResponseEntity<DeliveryPersonStatsDTO> getStats(
            @Parameter(description = "Delivery person ID", required = true)
            @PathVariable String id) {
        log.info("REST: Getting statistics for delivery person ID: {}", id);
        DeliveryPersonStatsDTO stats = deliveryPersonService.getStats(id);
        return ResponseEntity.ok(stats);
    }
}
