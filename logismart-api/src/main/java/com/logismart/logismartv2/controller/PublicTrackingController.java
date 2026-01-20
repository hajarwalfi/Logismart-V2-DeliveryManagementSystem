package com.logismart.logismartv2.controller;

import com.logismart.logismartv2.dto.tracking.PublicTrackingRequestDTO;
import com.logismart.logismartv2.dto.tracking.PublicTrackingResponseDTO;
import com.logismart.logismartv2.service.ParcelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Public controller for parcel tracking.
 * No authentication required - recipients can track their parcels
 * using the parcel ID and their email address.
 */
@RestController
@RequestMapping("/api/public/tracking")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Public Tracking", description = "Public API for recipients to track their parcels without authentication")
public class PublicTrackingController {

    private final ParcelService parcelService;

    @PostMapping
    @Operation(
            summary = "Track a parcel (public)",
            description = "Allows recipients to track their parcels using the parcel ID and their email address. " +
                    "The email must match the recipient email registered for the parcel. " +
                    "Returns parcel information and delivery history."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Parcel tracking information retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Email does not match the recipient"),
            @ApiResponse(responseCode = "404", description = "Parcel not found")
    })
    public ResponseEntity<PublicTrackingResponseDTO> trackParcel(
            @Valid @RequestBody PublicTrackingRequestDTO request) {
        log.info("REST: Public tracking request for parcel ID: {}", request.getParcelId());

        PublicTrackingResponseDTO response = parcelService.trackParcelPublic(
                request.getParcelId(),
                request.getEmail()
        );

        return ResponseEntity.ok(response);
    }
}
