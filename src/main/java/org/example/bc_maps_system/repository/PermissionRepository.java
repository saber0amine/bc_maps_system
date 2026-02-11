package org.example.bc_maps_system.repository;

import org.example.bc_maps_system.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {
}
