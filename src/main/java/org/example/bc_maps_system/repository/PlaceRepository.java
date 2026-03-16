package org.example.bc_maps_system.repository;

import org.example.bc_maps_system.model.Place;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PlaceRepository extends JpaRepository<Place, UUID>, JpaSpecificationExecutor<Place> {
    @Query("SELECT p FROM Place p JOIN FETCH p.user LEFT JOIN FETCH p.tags WHERE p.id = :id")
    Optional<Place> findByIdWithUser(@Param("id") UUID id);
    @Query("SELECT DISTINCT p FROM Place p LEFT JOIN FETCH p.user LEFT JOIN FETCH p.tags WHERE p.user.id = :userId AND p.isCurrentPosition = false")
    Page<Place> findAllByUserId(@Param("userId") UUID userId, Pageable pageable);
    @Query("SELECT p FROM Place p LEFT JOIN FETCH p.user WHERE p.user.id = :userId AND p.isCurrentPosition = true")
    Optional<Place> findByUserIdAndIsCurrentPositionTrue(@Param("userId") UUID userId);
}
