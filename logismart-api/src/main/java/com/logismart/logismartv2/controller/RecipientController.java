package com.logismart.logismartv2.controller;

import com.logismart.logismartv2.dto.recipient.RecipientCreateDTO;
import com.logismart.logismartv2.dto.recipient.RecipientResponseDTO;
import com.logismart.logismartv2.dto.recipient.RecipientUpdateDTO;
import com.logismart.logismartv2.service.RecipientService;
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
@RequestMapping("/api/recipients")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('MANAGER')")
@Tag(name = "Recipient Management", description = "APIs for managing parcel recipients")
public class RecipientController {

    private final RecipientService recipientService;

    @PostMapping
    @Operation(
            summary = "Create a new recipient",
            description = "Creates a new recipient (email is optional)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Recipient created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<RecipientResponseDTO> createRecipient(
            @Valid @RequestBody RecipientCreateDTO dto) {
        log.info("REST: Creating new recipient with phone: {}", dto.getPhone());
        RecipientResponseDTO created = recipientService.create(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get recipient by ID",
            description = "Retrieves a recipient by their unique identifier"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recipient found"),
            @ApiResponse(responseCode = "404", description = "Recipient not found")
    })
    public ResponseEntity<RecipientResponseDTO> getRecipientById(
            @Parameter(description = "Recipient ID", required = true)
            @PathVariable String id) {
        log.info("REST: Finding recipient by ID: {}", id);
        RecipientResponseDTO recipient = recipientService.findById(id);
        return ResponseEntity.ok(recipient);
    }

    @GetMapping
    @Operation(
            summary = "Get all recipients",
            description = "Retrieves a list of all recipients"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of recipients retrieved successfully")
    })
    public ResponseEntity<List<RecipientResponseDTO>> getAllRecipients() {
        log.info("REST: Finding all recipients");
        List<RecipientResponseDTO> recipients = recipientService.findAll();
        return ResponseEntity.ok(recipients);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update a recipient",
            description = "Updates recipient information (firstName, lastName, email, phone, address). " +
                    "All fields are optional - provide only what needs correcting. " +
                    "Supports partial updates for error correction (US-8)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recipient updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Recipient not found")
    })
    public ResponseEntity<RecipientResponseDTO> updateRecipient(
            @Parameter(description = "Recipient ID", required = true)
            @PathVariable String id,
            @Valid @RequestBody RecipientUpdateDTO dto) {
        log.info("REST: Updating recipient with ID: {}", id);

        
        dto.setId(id);

        RecipientResponseDTO updated = recipientService.update(dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a recipient",
            description = "Deletes a recipient by their ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Recipient deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Recipient not found")
    })
    public ResponseEntity<Void> deleteRecipient(
            @Parameter(description = "Recipient ID", required = true)
            @PathVariable String id) {
        log.info("REST: Deleting recipient with ID: {}", id);
        recipientService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(
            summary = "Search recipients by name",
            description = "Searches for recipients by name (case-insensitive)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully")
    })
    public ResponseEntity<List<RecipientResponseDTO>> searchRecipients(
            @Parameter(description = "Search keyword", required = true)
            @RequestParam String keyword) {
        log.info("REST: Searching recipients with keyword: {}", keyword);
        List<RecipientResponseDTO> recipients = recipientService.searchByName(keyword);
        return ResponseEntity.ok(recipients);
    }

    @GetMapping("/with-email")
    @Operation(
            summary = "Get recipients with email",
            description = "Retrieves recipients who have an email address"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recipients retrieved successfully")
    })
    public ResponseEntity<List<RecipientResponseDTO>> getRecipientsWithEmail() {
        log.info("REST: Finding recipients with email");
        List<RecipientResponseDTO> recipients = recipientService.findWithEmail();
        return ResponseEntity.ok(recipients);
    }

    @GetMapping("/without-email")
    @Operation(
            summary = "Get recipients without email",
            description = "Retrieves recipients who do not have an email address"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recipients retrieved successfully")
    })
    public ResponseEntity<List<RecipientResponseDTO>> getRecipientsWithoutEmail() {
        log.info("REST: Finding recipients without email");
        List<RecipientResponseDTO> recipients = recipientService.findWithoutEmail();
        return ResponseEntity.ok(recipients);
    }
}
