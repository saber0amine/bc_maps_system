package org.example.bc_maps_system.service;

import org.example.bc_maps_system.model.AccessType;
import org.example.bc_maps_system.model.Permission;
import org.example.bc_maps_system.model.ResourceType;
import org.example.bc_maps_system.model.Token;
import org.example.bc_maps_system.repository.PermissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    public boolean canRead(Token token, ResourceType resourceType, UUID resourceId) {
        if (token.isMasterToken()) return true;
        return hasPermission(token, resourceType, resourceId, AccessType.READ)
                || hasPermission(token, resourceType, resourceId, AccessType.WRITE)
                || hasPermission(token, resourceType, resourceId, AccessType.ADMIN);
    }

    public boolean canWrite(Token token, ResourceType resourceType, UUID resourceId) {
        if (token.isMasterToken()) return true;
        return hasPermission(token, resourceType, resourceId, AccessType.WRITE)
                || hasPermission(token, resourceType, resourceId, AccessType.ADMIN);
    }

    public Set<UUID> getReadableResourceIds(Token token, ResourceType resourceType) {
        if (token.isMasterToken()) {
            return Set.of();
        }
        return filterResourceIds(token, resourceType, EnumSet.of(AccessType.READ, AccessType.WRITE, AccessType.ADMIN));
    }

    public Set<UUID> getWritableResourceIds(Token token, ResourceType resourceType) {
        if (token.isMasterToken()) {
            return Set.of();
        }
        return filterResourceIds(token, resourceType, EnumSet.of(AccessType.WRITE, AccessType.ADMIN));
    }

    public boolean hasAtLeastOnePermission(Token token) {
        if (token.isMasterToken()) {
            return true;
        }
        return !permissionRepository.findByTokenId(token.getId()).isEmpty();
    }

    private Set<UUID> filterResourceIds(Token token, ResourceType resourceType, Set<AccessType> accessTypes) {
        List<Permission> permissions = permissionRepository.findByTokenId(token.getId());
        return permissions.stream()
                .filter(permission -> permission.getResourceType() == resourceType)
                .filter(permission -> accessTypes.contains(permission.getAccessType()))
                .map(Permission::getResourceId)
                .collect(Collectors.toSet());
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
