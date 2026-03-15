package org.example.bc_maps_system.repository;

import org.example.bc_maps_system.model.AccessType;
import org.example.bc_maps_system.model.Permission;
import org.example.bc_maps_system.model.ResourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {

    List<Permission> findByTokenId(UUID tokenId);

    Optional<Permission> findByTokenIdAndResourceTypeAndResourceId(
            UUID tokenId,
            ResourceType resourceType,
            UUID resourceId
    );

    boolean existsByTokenIdAndResourceTypeAndResourceIdAndAccessType(
            UUID tokenId,
            ResourceType resourceType,
            UUID resourceId,
            AccessType accessType
    );
}