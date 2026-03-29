package org.example.bc_maps_system.repository;

import org.example.bc_maps_system.model.ExternalSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExternalSourceRepository extends JpaRepository<ExternalSource, UUID> {
    List<ExternalSource> findByUserIdAndIsActiveTrue(UUID userId);
    List<ExternalSource> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
