package org.example.bc_maps_system.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class TokenResponse {

    private UUID id;
    private String value;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private boolean isRevoked;
    private String serverUrl;

    public TokenResponse(UUID id, String value, String description, LocalDateTime createdAt,
                         LocalDateTime expiresAt, boolean isRevoked, String serverUrl) {
        this.id = id;
        this.value = value;
        this.description = description;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.isRevoked = isRevoked;
        this.serverUrl = serverUrl;
    }

    public UUID getId() { return id; }
    public String getValue() { return value; }
    public String getDescription() { return description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public boolean isRevoked() { return isRevoked; }
    public String getServerUrl() { return serverUrl; }
}