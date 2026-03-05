package org.example.bc_maps_system.service;

import org.example.bc_maps_system.model.AccessType;
import org.example.bc_maps_system.model.Permission;
import org.example.bc_maps_system.model.ResourceType;
import org.example.bc_maps_system.model.Token;
import org.example.bc_maps_system.repository.PermissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    public boolean canRead(Token token, ResourceType resourceType, UUID resourceId) {
        return hasPermission(token, resourceType, resourceId, AccessType.READ)
                || hasPermission(token, resourceType, resourceId, AccessType.WRITE)
                || hasPermission(token, resourceType, resourceId, AccessType.ADMIN);
    }

    public boolean canWrite(Token token, ResourceType resourceType, UUID resourceId) {
        return hasPermission(token, resourceType, resourceId, AccessType.WRITE)
                || hasPermission(token, resourceType, resourceId, AccessType.ADMIN);
    }

    private boolean hasPermission(Token token, ResourceType resourceType, UUID resourceId, AccessType accessType) {
        return permissionRepository.existsByTokenIdAndResourceTypeAndResourceIdAndAccessType(
                token.getId(), resourceType, resourceId, accessType
        );
    }

    @Transactional
    public Permission addPermission(Token token, ResourceType resourceType, UUID resourceId, AccessType accessType) {
        Permission permission = new Permission();
        permission.setToken(token);
        permission.setResourceType(resourceType);
        permission.setResourceId(resourceId);
        permission.setAccessType(accessType);
        return permissionRepository.save(permission);
    }
}