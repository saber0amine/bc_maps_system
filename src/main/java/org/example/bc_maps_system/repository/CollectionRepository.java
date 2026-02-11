package org.example.bc_maps_system.repository;

import org.example.bc_maps_system.model.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CollectionRepository extends JpaRepository<Collection, UUID> {
}
