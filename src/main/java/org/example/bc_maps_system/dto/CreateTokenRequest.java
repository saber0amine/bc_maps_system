package org.example.bc_maps_system.dto;

import org.example.bc_maps_system.model.AccessType;
import org.example.bc_maps_system.model.ResourceType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class CreateTokenRequest {

    private String description;
    private LocalDateTime expiresAt;
    private List<PermissionEntry> permissions;

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public List<PermissionEntry> getPermissions() { return permissions; }
    public void setPermissions(List<PermissionEntry> permissions) { this.permissions = permissions; }

    public static class PermissionEntry {
        private ResourceType resourceType;
        private UUID resourceId;
        private AccessType accessType;

        public ResourceType getResourceType() { return resourceType; }
        public void setResourceType(ResourceType resourceType) { this.resourceType = resourceType; }
        public UUID getResourceId() { return resourceId; }
        public void setResourceId(UUID resourceId) { this.resourceId = resourceId; }
        public AccessType getAccessType() { return accessType; }
        public void setAccessType(AccessType accessType) { this.accessType = accessType; }
    }
}