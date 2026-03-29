package org.example.bc_maps_system.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ExternalSourceResponse(
        UUID id,
        String name,
        String serverUrl,
        String maskedToken,
        boolean active,
        LocalDateTime lastSync,
        LocalDateTime createdAt
) {}
