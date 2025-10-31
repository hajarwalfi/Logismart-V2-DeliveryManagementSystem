package com.logismart.logismartv2.controller;

import com.logismart.logismartv2.dto.parcel.ParcelResponseDTO;
import com.logismart.logismartv2.dto.senderclient.SenderClientCreateDTO;
import com.logismart.logismartv2.dto.senderclient.SenderClientResponseDTO;
import com.logismart.logismartv2.dto.senderclient.SenderClientUpdateDTO;
import com.logismart.logismartv2.service.ParcelService;
import com.logismart.logismartv2.service.SenderClientService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sender-clients")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Sender Client Management", description = "APIs for managing sender clients (businesses/individuals who send parcels)")
public class SenderClientController {

    private final SenderClientService senderClientService;
    private final ParcelService parcelService;

    @PostMapping
    @Operation(
            summary = "Create a new sender client",
            description = "Creates a new sender client with unique email address"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Sender client created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    public ResponseEntity<SenderClientResponseDTO> createSenderClient(
            @Valid @RequestBody SenderClientCreateDTO dto) {
        log.info("REST: Creating new sender client with email: {}", dto.getEmail());
        SenderClientResponseDTO created = senderClientService.create(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get sender client by ID",
            description = "Retrieves a sender client by their unique identifier"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sender client found"),
            @ApiResponse(responseCode = "404", description = "Sender client not found")
    })
    public ResponseEntity<SenderClientResponseDTO> getSenderClientById(
            @Parameter(description = "Sender client ID", required = true)
            @PathVariable String id) {
        log.info("REST: Finding sender client by ID: {}", id);
        SenderClientResponseDTO client = senderClientService.findById(id);
        return ResponseEntity.ok(client);
    }

    @GetMapping
    @Operation(
            summary = "Get all sender clients",
            description = "Retrieves a list of all sender clients"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of sender clients retrieved successfully")
    })
    public ResponseEntity<List<SenderClientResponseDTO>> getAllSenderClients() {
        log.info("REST: Finding all sender clients");
        List<SenderClientResponseDTO> clients = senderClientService.findAll();
        return ResponseEntity.ok(clients);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update a sender client",
            description = "Updates sender client information (firstName, lastName, email, phone, address). " +
                    "All fields are optional - provide only what needs correcting. " +
                    "Supports partial updates for error correction (US-8)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sender client updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Sender client not found"),
            @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    public ResponseEntity<SenderClientResponseDTO> updateSenderClient(
            @Parameter(description = "Sender client ID", required = true)
            @PathVariable String id,
            @Valid @RequestBody SenderClientUpdateDTO dto) {
        log.info("REST: Updating sender client with ID: {}", id);

        
        dto.setId(id);

        SenderClientResponseDTO updated = senderClientService.update(dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a sender client",
            description = "Deletes a sender client by their ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Sender client deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Sender client not found")
    })
    public ResponseEntity<Void> deleteSenderClient(
            @Parameter(description = "Sender client ID", required = true)
            @PathVariable String id) {
        log.info("REST: Deleting sender client with ID: {}", id);
        senderClientService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-email/{email}")
    @Operation(
            summary = "Get sender client by email",
            description = "Retrieves a sender client by their email address"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sender client found"),
            @ApiResponse(responseCode = "404", description = "Sender client not found")
    })
    public ResponseEntity<SenderClientResponseDTO> getSenderClientByEmail(
            @Parameter(description = "Email address", required = true)
            @PathVariable String email) {
        log.info("REST: Finding sender client by email: {}", email);
        SenderClientResponseDTO client = senderClientService.findByEmail(email);
        return ResponseEntity.ok(client);
    }

    @GetMapping("/search")
    @Operation(
            summary = "Search sender clients by name",
            description = "Searches for sender clients by name (case-insensitive)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully")
    })
    public ResponseEntity<List<SenderClientResponseDTO>> searchSenderClients(
            @Parameter(description = "Search keyword", required = true)
            @RequestParam String keyword) {
        log.info("REST: Searching sender clients with keyword: {}", keyword);
        List<SenderClientResponseDTO> clients = senderClientService.searchByName(keyword);
        return ResponseEntity.ok(clients);
    }

    @GetMapping("/{id}/parcels/count")
    @Operation(
            summary = "Count parcels for sender client",
            description = "Returns the number of parcels sent by this client"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Count retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Sender client not found")
    })
    public ResponseEntity<Long> countParcels(
            @Parameter(description = "Sender client ID", required = true)
            @PathVariable String id) {
        log.info("REST: Counting parcels for sender client ID: {}", id);
        Long count = senderClientService.countParcels(id);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/{id}/parcels/in-progress")
    @Operation(
            summary = "Get in-progress parcels for sender client",
            description = "Returns all parcels that are not yet delivered (CREATED, COLLECTED, IN_STOCK, IN_TRANSIT)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "In-progress parcels retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Sender client not found")
    })
    public ResponseEntity<List<ParcelResponseDTO>> getInProgressParcels(
            @Parameter(description = "Sender client ID", required = true)
            @PathVariable String id) {
        log.info("REST: Finding in-progress parcels for sender client ID: {}", id);
        List<ParcelResponseDTO> parcels = parcelService.findInProgressBySenderClientId(id);
        return ResponseEntity.ok(parcels);
    }

    @GetMapping("/{id}/parcels/delivered")
    @Operation(
            summary = "Get delivered parcels for sender client",
            description = "Returns all parcels that have been delivered"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delivered parcels retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Sender client not found")
    })
    public ResponseEntity<List<ParcelResponseDTO>> getDeliveredParcels(
            @Parameter(description = "Sender client ID", required = true)
            @PathVariable String id) {
        log.info("REST: Finding delivered parcels for sender client ID: {}", id);
        List<ParcelResponseDTO> parcels = parcelService.findDeliveredBySenderClientId(id);
        return ResponseEntity.ok(parcels);
    }
}
