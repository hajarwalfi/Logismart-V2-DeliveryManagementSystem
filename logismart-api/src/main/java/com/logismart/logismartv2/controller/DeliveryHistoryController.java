package com.logismart.logismartv2.controller;

import com.logismart.logismartv2.dto.deliveryhistory.DeliveryHistoryCreateDTO;
import com.logismart.logismartv2.dto.deliveryhistory.DeliveryHistoryResponseDTO;
import com.logismart.logismartv2.service.DeliveryHistoryService;
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
@RequestMapping("/api/delivery-history")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('MANAGER')")
@Tag(name = "Delivery History", description = "APIs for tracking parcel status history (audit trail)")
public class DeliveryHistoryController {

    private final DeliveryHistoryService deliveryHistoryService;

    @PostMapping
    @Operation(
            summary = "Create a delivery history entry",
            description = "Creates a new delivery history entry (typically done automatically on status change)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "History entry created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Parcel not found")
    })
    public ResponseEntity<DeliveryHistoryResponseDTO> createDeliveryHistory(
            @Valid @RequestBody DeliveryHistoryCreateDTO dto) {
        log.info("REST: Creating delivery history entry for parcel ID: {}", dto.getParcelId());
        DeliveryHistoryResponseDTO created = deliveryHistoryService.create(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get delivery history entry by ID",
            description = "Retrieves a delivery history entry by its unique identifier"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "History entry found"),
            @ApiResponse(responseCode = "404", description = "History entry not found")
    })
    public ResponseEntity<DeliveryHistoryResponseDTO> getDeliveryHistoryById(
            @Parameter(description = "History entry ID", required = true)
            @PathVariable String id) {
        log.info("REST: Finding delivery history by ID: {}", id);
        DeliveryHistoryResponseDTO history = deliveryHistoryService.findById(id);
        return ResponseEntity.ok(history);
    }

    @GetMapping
    @Operation(
            summary = "Get all delivery history entries",
            description = "Retrieves all delivery history entries (use with caution - can be large)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "History entries retrieved successfully")
    })
    public ResponseEntity<List<DeliveryHistoryResponseDTO>> getAllDeliveryHistory() {
        log.info("REST: Finding all delivery history entries");
        List<DeliveryHistoryResponseDTO> histories = deliveryHistoryService.findAll();
        return ResponseEntity.ok(histories);
    }

    @GetMapping("/parcel/{parcelId}")
    @Operation(
            summary = "Get history timeline for a parcel",
            description = "Retrieves all status changes for a parcel in chronological order"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "History timeline retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Parcel not found")
    })
    public ResponseEntity<List<DeliveryHistoryResponseDTO>> getHistoryByParcelId(
            @Parameter(description = "Parcel ID", required = true)
            @PathVariable String parcelId) {
        log.info("REST: Finding delivery history for parcel ID: {}", parcelId);
        List<DeliveryHistoryResponseDTO> histories = deliveryHistoryService.findByParcelId(parcelId);
        return ResponseEntity.ok(histories);
    }

    @GetMapping("/parcel/{parcelId}/latest")
    @Operation(
            summary = "Get latest history entry for a parcel",
            description = "Retrieves the most recent status change for a parcel"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Latest history entry found"),
            @ApiResponse(responseCode = "404", description = "Parcel not found or no history available")
    })
    public ResponseEntity<DeliveryHistoryResponseDTO> getLatestHistoryByParcelId(
            @Parameter(description = "Parcel ID", required = true)
            @PathVariable String parcelId) {
        log.info("REST: Finding latest delivery history for parcel ID: {}", parcelId);
        DeliveryHistoryResponseDTO history = deliveryHistoryService.findLatestByParcelId(parcelId);
        return ResponseEntity.ok(history);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a delivery history entry",
            description = "Deletes a history entry (WARNING: affects audit trail - use only for corrections)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "History entry deleted successfully"),
            @ApiResponse(responseCode = "404", description = "History entry not found")
    })
    public ResponseEntity<Void> deleteDeliveryHistory(
            @Parameter(description = "History entry ID", required = true)
            @PathVariable String id) {
        log.warn("REST: Deleting delivery history entry with ID: {} - This affects audit trail!", id);
        deliveryHistoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/parcel/{parcelId}/count")
    @Operation(
            summary = "Count history entries for a parcel",
            description = "Returns the number of status changes for a parcel"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Count retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Parcel not found")
    })
    public ResponseEntity<Long> countHistoryByParcelId(
            @Parameter(description = "Parcel ID", required = true)
            @PathVariable String parcelId) {
        log.info("REST: Counting history entries for parcel ID: {}", parcelId);
        Long count = deliveryHistoryService.countByParcelId(parcelId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/with-comments")
    @Operation(
            summary = "Get history entries with comments",
            description = "Retrieves history entries that have comments (for review/audit)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "History entries retrieved successfully")
    })
    public ResponseEntity<List<DeliveryHistoryResponseDTO>> getHistoryWithComments() {
        log.info("REST: Finding delivery history entries with comments");
        List<DeliveryHistoryResponseDTO> histories = deliveryHistoryService.findEntriesWithComments();
        return ResponseEntity.ok(histories);
    }

    @GetMapping("/deliveries/today/count")
    @Operation(
            summary = "Count deliveries completed today",
            description = "Returns the number of parcels marked as DELIVERED today"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
    })
    public ResponseEntity<Long> countDeliveriesToday() {
        log.info("REST: Counting deliveries completed today");
        Long count = deliveryHistoryService.countDeliveriesToday();
        return ResponseEntity.ok(count);
    }



    @GetMapping("/my-history")
    @PreAuthorize("hasRole('LIVREUR')")
    @Operation(
            summary = "Get my delivery history (delivery person only)",
            description = "Retrieves the complete delivery history for all parcels assigned to the authenticated delivery person. " +
                    "Shows the chronological timeline of status changes for all their parcels."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "History retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Delivery person profile not found")
    })
    public ResponseEntity<List<DeliveryHistoryResponseDTO>> getMyHistory(
            org.springframework.security.core.Authentication authentication) {
        log.info("REST: Getting delivery history for delivery person: {}", authentication.getName());

        String userId = ((com.logismart.security.entity.User) authentication.getPrincipal()).getId();
        List<DeliveryHistoryResponseDTO> history = deliveryHistoryService.findMyHistoryForDeliveryPerson(userId);

        return ResponseEntity.ok(history);
    }
}
