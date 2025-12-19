package com.logismart.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for permission response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionResponse {

    private String id;
    private String name;
    private String description;
    private String resource;
    private String action;
    private Boolean enabled;
}
