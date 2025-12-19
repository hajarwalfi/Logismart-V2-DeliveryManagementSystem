package com.logismart.logismartv2.controller;

import com.logismart.logismartv2.dto.deliveryhistory.DeliveryHistoryResponseDTO;
import com.logismart.logismartv2.dto.parcel.ParcelCreateDTO;
import com.logismart.logismartv2.dto.parcel.ParcelResponseDTO;
import com.logismart.logismartv2.dto.parcel.ParcelUpdateDTO;
import com.logismart.logismartv2.entity.ParcelPriority;
import com.logismart.logismartv2.entity.ParcelStatus;
import com.logismart.logismartv2.exception.BadRequestException;
import com.logismart.logismartv2.service.ParcelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/parcels")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Parcel Management", description = "APIs for managing parcels (central entity connecting all others)")
public class ParcelController {

    private final ParcelService parcelService;

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'CLIENT')")
    @Operation(
            summary = "Create a new parcel",
            description = "Creates a new parcel with sender, recipient, and products. " +
                    "Automatically creates product associations and initial delivery history."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Parcel created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Sender, Recipient, or Product not found")
    })
    public ResponseEntity<ParcelResponseDTO> createParcel(
            @Valid @RequestBody ParcelCreateDTO dto) {
        log.info("REST: Creating new parcel for sender ID: {} to recipient ID: {}",
                dto.getSenderClientId(), dto.getRecipientId());
        ParcelResponseDTO created = parcelService.create(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'CLIENT')")
    @Operation(
            summary = "Get parcel by ID",
            description = "Retrieves a parcel by its unique identifier with all relationships loaded"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Parcel found"),
            @ApiResponse(responseCode = "404", description = "Parcel not found")
    })
    public ResponseEntity<ParcelResponseDTO> getParcelById(
            @Parameter(description = "Parcel ID", required = true)
            @PathVariable String id) {
        log.info("REST: Finding parcel by ID: {}", id);
        ParcelResponseDTO parcel = parcelService.findById(id);
        return ResponseEntity.ok(parcel);
    }

    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Get all parcels with pagination",
            description = "Retrieves a paginated list of all parcels. " +
                    "Supports pagination (page, size) and sorting (sort=field,direction). " +
                    "Example: /api/parcels?page=0&size=10&sort=createdAt,desc"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Parcels retrieved successfully")
    })
    public ResponseEntity<Page<ParcelResponseDTO>> getAllParcels(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            @Parameter(description = "Pagination parameters (page, size, sort)", required = false)
            Pageable pageable) {
        log.info("REST: Finding all parcels with pagination: page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());
        Page<ParcelResponseDTO> parcels = parcelService.findAll(pageable);
        return ResponseEntity.ok(parcels);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Update a parcel",
            description = "Updates parcel information (description, weight, priority, destinationCity), " +
                    "parcel status, or assigns delivery person/zone. " +
                    "All fields are optional - provide only what needs updating. " +
                    "Status changes automatically create delivery history entries."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Parcel updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Parcel, DeliveryPerson, or Zone not found")
    })
    public ResponseEntity<ParcelResponseDTO> updateParcel(
            @Parameter(description = "Parcel ID", required = true)
            @PathVariable String id,
            @Valid @RequestBody ParcelUpdateDTO dto) {
        log.info("REST: Updating parcel with ID: {}", id);

        
        dto.setId(id);

        ParcelResponseDTO updated = parcelService.update(dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Delete a parcel",
            description = "Deletes a parcel (cascades to products and history)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Parcel deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Parcel not found")
    })
    public ResponseEntity<Void> deleteParcel(
            @Parameter(description = "Parcel ID", required = true)
            @PathVariable String id) {
        log.info("REST: Deleting parcel with ID: {}", id);
        parcelService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Get parcels by status",
            description = "Retrieves all parcels with a specific status"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Parcels retrieved successfully")
    })
    public ResponseEntity<List<ParcelResponseDTO>> getParcelsByStatus(
            @Parameter(description = "Parcel status (CREATED, COLLECTED, IN_STOCK, IN_TRANSIT, DELIVERED)", required = true)
            @PathVariable ParcelStatus status) {
        log.info("REST: Finding parcels with status: {}", status);
        List<ParcelResponseDTO> parcels = parcelService.findByStatus(status);
        return ResponseEntity.ok(parcels);
    }

    @GetMapping("/priority/{priority}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Get parcels by priority",
            description = "Retrieves all parcels with a specific priority level"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Parcels retrieved successfully")
    })
    public ResponseEntity<List<ParcelResponseDTO>> getParcelsByPriority(
            @Parameter(description = "Parcel priority (NORMAL, URGENT, EXPRESS)", required = true)
            @PathVariable ParcelPriority priority) {
        log.info("REST: Finding parcels with priority: {}", priority);
        List<ParcelResponseDTO> parcels = parcelService.findByPriority(priority);
        return ResponseEntity.ok(parcels);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Advanced parcel search with pagination",
            description = "Search parcels with multiple optional filters: status, priority, zoneId, " +
                    "destinationCity, deliveryPersonId, senderClientId, recipientId, unassignedOnly. " +
                    "All filters are optional. Supports pagination and sorting. " +
                    "Example: /api/parcels/search?status=IN_TRANSIT&priority=EXPRESS&page=0&size=10&sort=createdAt,desc"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Parcels retrieved successfully")
    })
    public ResponseEntity<Page<ParcelResponseDTO>> searchParcels(
            @Parameter(description = "Filter by status (optional)")
            @RequestParam(required = false) ParcelStatus status,
            @Parameter(description = "Filter by priority (optional)")
            @RequestParam(required = false) ParcelPriority priority,
            @Parameter(description = "Filter by zone ID (optional)")
            @RequestParam(required = false) String zoneId,
            @Parameter(description = "Filter by destination city (optional)")
            @RequestParam(required = false) String destinationCity,
            @Parameter(description = "Filter by delivery person ID (optional)")
            @RequestParam(required = false) String deliveryPersonId,
            @Parameter(description = "Filter by sender client ID (optional)")
            @RequestParam(required = false) String senderClientId,
            @Parameter(description = "Filter by recipient ID (optional)")
            @RequestParam(required = false) String recipientId,
            @Parameter(description = "Show only unassigned parcels (optional)")
            @RequestParam(required = false) Boolean unassignedOnly,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            @Parameter(description = "Pagination parameters (page, size, sort)")
            Pageable pageable) {

        log.info("REST: Searching parcels with filters - status: {}, priority: {}, zoneId: {}, city: {}, deliveryPersonId: {}, senderClientId: {}, recipientId: {}, unassignedOnly: {}",
                status, priority, zoneId, destinationCity, deliveryPersonId, senderClientId, recipientId, unassignedOnly);

        Page<ParcelResponseDTO> parcels = parcelService.searchParcels(
                status, priority, zoneId, destinationCity, deliveryPersonId,
                senderClientId, recipientId, unassignedOnly, pageable);

        return ResponseEntity.ok(parcels);
    }

    @GetMapping("/filter")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Get parcels by status and priority",
            description = "Retrieves parcels matching both status and priority criteria. " +
                    "DEPRECATED: Use /api/parcels/search instead for advanced filtering with pagination."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Parcels retrieved successfully")
    })
    public ResponseEntity<List<ParcelResponseDTO>> getParcelsByStatusAndPriority(
            @Parameter(description = "Parcel status", required = true)
            @RequestParam ParcelStatus status,
            @Parameter(description = "Parcel priority", required = true)
            @RequestParam ParcelPriority priority) {
        log.info("REST: Finding parcels with status: {} and priority: {}", status, priority);
        List<ParcelResponseDTO> parcels = parcelService.findByStatusAndPriority(status, priority);
        return ResponseEntity.ok(parcels);
    }

    @GetMapping("/status/{status}/count")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Count parcels by status",
            description = "Returns the number of parcels with a specific status"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
    })
    public ResponseEntity<Long> countParcelsByStatus(
            @Parameter(description = "Parcel status", required = true)
            @PathVariable ParcelStatus status) {
        log.info("REST: Counting parcels with status: {}", status);
        Long count = parcelService.countByStatus(status);
        return ResponseEntity.ok(count);
    }

    

    @GetMapping("/sender/{senderClientId}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Get parcels by sender client",
            description = "Retrieves all parcels sent by a specific client"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Parcels retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Sender client not found")
    })
    public ResponseEntity<List<ParcelResponseDTO>> getParcelsBySenderClient(
            @Parameter(description = "Sender client ID", required = true)
            @PathVariable String senderClientId) {
        log.info("REST: Finding parcels for sender client ID: {}", senderClientId);
        List<ParcelResponseDTO> parcels = parcelService.findBySenderClientId(senderClientId);
        return ResponseEntity.ok(parcels);
    }

    @GetMapping("/recipient/{recipientId}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Get parcels by recipient",
            description = "Retrieves all parcels for a specific recipient"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Parcels retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Recipient not found")
    })
    public ResponseEntity<List<ParcelResponseDTO>> getParcelsByRecipient(
            @Parameter(description = "Recipient ID", required = true)
            @PathVariable String recipientId) {
        log.info("REST: Finding parcels for recipient ID: {}", recipientId);
        List<ParcelResponseDTO> parcels = parcelService.findByRecipientId(recipientId);
        return ResponseEntity.ok(parcels);
    }

    @GetMapping("/delivery-person/{deliveryPersonId}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Get parcels by delivery person",
            description = "Retrieves all parcels assigned to a specific delivery person"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Parcels retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Delivery person not found")
    })
    public ResponseEntity<List<ParcelResponseDTO>> getParcelsByDeliveryPerson(
            @Parameter(description = "Delivery person ID", required = true)
            @PathVariable String deliveryPersonId) {
        log.info("REST: Finding parcels for delivery person ID: {}", deliveryPersonId);
        List<ParcelResponseDTO> parcels = parcelService.findByDeliveryPersonId(deliveryPersonId);
        return ResponseEntity.ok(parcels);
    }

    @GetMapping("/zone/{zoneId}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Get parcels by zone",
            description = "Retrieves all parcels in a specific delivery zone"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Parcels retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Zone not found")
    })
    public ResponseEntity<List<ParcelResponseDTO>> getParcelsByZone(
            @Parameter(description = "Zone ID", required = true)
            @PathVariable String zoneId) {
        log.info("REST: Finding parcels for zone ID: {}", zoneId);
        List<ParcelResponseDTO> parcels = parcelService.findByZoneId(zoneId);
        return ResponseEntity.ok(parcels);
    }

    

    @GetMapping("/unassigned")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Get unassigned parcels",
            description = "Retrieves parcels not yet assigned to a delivery person"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Unassigned parcels retrieved successfully")
    })
    public ResponseEntity<List<ParcelResponseDTO>> getUnassignedParcels() {
        log.info("REST: Finding unassigned parcels");
        List<ParcelResponseDTO> parcels = parcelService.findUnassignedParcels();
        return ResponseEntity.ok(parcels);
    }

    @GetMapping("/high-priority-pending")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Get high priority pending parcels",
            description = "Retrieves EXPRESS priority parcels (same/next day delivery) that are not yet delivered"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "High priority (EXPRESS) parcels retrieved successfully")
    })
    public ResponseEntity<List<ParcelResponseDTO>> getHighPriorityPending() {
        log.info("REST: Finding high priority (EXPRESS) pending parcels");
        List<ParcelResponseDTO> parcels = parcelService.findHighPriorityPending();
        return ResponseEntity.ok(parcels);
    }

    @GetMapping("/city/{city}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Get parcels by destination city",
            description = "Retrieves parcels going to a specific city (case-insensitive)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Parcels retrieved successfully")
    })
    public ResponseEntity<List<ParcelResponseDTO>> getParcelsByCity(
            @Parameter(description = "Destination city name", required = true)
            @PathVariable String city) {
        log.info("REST: Finding parcels for destination city: {}", city);
        List<ParcelResponseDTO> parcels = parcelService.findByDestinationCity(city);
        return ResponseEntity.ok(parcels);
    }

    @GetMapping("/count")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Count total parcels",
            description = "Returns the total number of parcels in the system"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
    })
    public ResponseEntity<Long> countTotalParcels() {
        log.info("REST: Counting total parcels");
        Long count = parcelService.countTotalParcels();
        return ResponseEntity.ok(count);
    }

    

    @GetMapping("/group-by/status")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Group parcels by status",
            description = "Returns a synthetic view of parcels grouped by status. " +
                    "Useful for dashboard overview."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Grouping retrieved successfully")
    })
    public ResponseEntity<Map<String, Long>> groupByStatus() {
        log.info("REST: Grouping parcels by status");
        Map<String, Long> grouped = parcelService.groupByStatus();
        return ResponseEntity.ok(grouped);
    }

    @GetMapping("/group-by/priority")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Group parcels by priority",
            description = "Returns a synthetic view of parcels grouped by priority. " +
                    "Useful for identifying urgent workload."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Grouping retrieved successfully")
    })
    public ResponseEntity<Map<String, Long>> groupByPriority() {
        log.info("REST: Grouping parcels by priority");
        Map<String, Long> grouped = parcelService.groupByPriority();
        return ResponseEntity.ok(grouped);
    }

    @GetMapping("/group-by/zone")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Group parcels by zone",
            description = "Returns a synthetic view of parcels grouped by zone. " +
                    "Useful for resource allocation and zone comparison."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Grouping retrieved successfully")
    })
    public ResponseEntity<Map<String, Long>> groupByZone() {
        log.info("REST: Grouping parcels by zone");
        Map<String, Long> grouped = parcelService.groupByZone();
        return ResponseEntity.ok(grouped);
    }

    @GetMapping("/group-by/city")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Group parcels by destination city",
            description = "Returns a synthetic view of parcels grouped by destination city. " +
                    "Useful for geographic distribution analysis."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Grouping retrieved successfully")
    })
    public ResponseEntity<Map<String, Long>> groupByCity() {
        log.info("REST: Grouping parcels by city");
        Map<String, Long> grouped = parcelService.groupByCity();
        return ResponseEntity.ok(grouped);
    }

    

    @GetMapping("/{id}/history")
    @PreAuthorize("hasAnyRole('MANAGER', 'CLIENT')")
    @Operation(
            summary = "Get parcel delivery history",
            description = "Retrieves the complete chronological timeline of all status changes for a parcel. " +
                    "Shows when each status transition occurred with timestamps and comments."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "History retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Parcel not found")
    })
    public ResponseEntity<List<DeliveryHistoryResponseDTO>> getParcelHistory(
            @Parameter(description = "Parcel ID", required = true)
            @PathVariable String id) {
        log.info("REST: Getting delivery history for parcel ID: {}", id);
        List<DeliveryHistoryResponseDTO> history = parcelService.getParcelHistory(id);
        return ResponseEntity.ok(history);
    }



    @GetMapping("/my-parcels")
    @PreAuthorize("hasAnyRole('LIVREUR', 'CLIENT')")
    @Operation(
            summary = "Get my parcels (role-based)",
            description = "Retrieves parcels based on the authenticated user's role: " +
                    "LIVREUR: Returns parcels assigned to them. " +
                    "CLIENT: Returns parcels they have sent."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Parcels retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User profile not found")
    })
    public ResponseEntity<List<ParcelResponseDTO>> getMyParcels(
            org.springframework.security.core.Authentication authentication) {
        log.info("REST: Getting parcels for user: {}", authentication.getName());


        var authorities = authentication.getAuthorities();
        String userId = ((com.logismart.security.entity.User) authentication.getPrincipal()).getId();

        List<ParcelResponseDTO> parcels;


        if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_LIVREUR"))) {
            log.info("User is LIVREUR, fetching assigned parcels");
            parcels = parcelService.findMyParcelsForDeliveryPerson(userId);
        } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT"))) {
            log.info("User is CLIENT, fetching sent parcels");
            parcels = parcelService.findMyParcelsForClient(userId);
        } else {
            throw new BadRequestException("Invalid role for this endpoint");
        }

        return ResponseEntity.ok(parcels);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('LIVREUR')")
    @Operation(
            summary = "Update parcel status (delivery person only)",
            description = "Allows a delivery person to update the status of a parcel assigned to them. " +
                    "Only parcels assigned to the authenticated delivery person can be updated. " +
                    "Automatically creates a delivery history entry."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Parcel not assigned to this delivery person"),
            @ApiResponse(responseCode = "404", description = "Parcel not found")
    })
    public ResponseEntity<ParcelResponseDTO> updateParcelStatus(
            @Parameter(description = "Parcel ID", required = true)
            @PathVariable String id,
            @Parameter(description = "New status", required = true)
            @RequestParam ParcelStatus status,
            org.springframework.security.core.Authentication authentication) {
        log.info("REST: Delivery person updating parcel {} status to {}", id, status);

        String userId = ((com.logismart.security.entity.User) authentication.getPrincipal()).getId();
        ParcelResponseDTO updated = parcelService.updateParcelStatusForDeliveryPerson(id, status, userId);

        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}/tracking")
    @PreAuthorize("hasAnyRole('MANAGER', 'CLIENT', 'LIVREUR')")
    @Operation(
            summary = "Track parcel (get history)",
            description = "Retrieves the tracking information (delivery history) for a parcel. " +
                    "Clients can track their own parcels, delivery persons can track assigned parcels, " +
                    "and managers can track any parcel."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tracking information retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - parcel not owned by you"),
            @ApiResponse(responseCode = "404", description = "Parcel not found")
    })
    public ResponseEntity<List<DeliveryHistoryResponseDTO>> trackParcel(
            @Parameter(description = "Parcel ID", required = true)
            @PathVariable String id,
            org.springframework.security.core.Authentication authentication) {
        log.info("REST: Tracking parcel ID: {} for user: {}", id, authentication.getName());

        var authorities = authentication.getAuthorities();
        String userId = ((com.logismart.security.entity.User) authentication.getPrincipal()).getId();


        boolean hasAccess = authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"));


        if (!hasAccess && authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT"))) {
            hasAccess = parcelService.isParcelOwnedByClient(id, userId);
            if (!hasAccess) {
                throw new BadRequestException("You can only track your own parcels");
            }
        }


        if (!hasAccess && authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_LIVREUR"))) {
            hasAccess = parcelService.isParcelAssignedToDeliveryPerson(id, userId);
            if (!hasAccess) {
                throw new BadRequestException("You can only track parcels assigned to you");
            }
        }

        List<DeliveryHistoryResponseDTO> history = parcelService.getParcelHistory(id);
        return ResponseEntity.ok(history);
    }
}
