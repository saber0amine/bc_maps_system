package org.example.bc_maps_system.repository;

import org.example.bc_maps_system.model.Place;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlaceRepository extends JpaRepository<Place, UUID>, JpaSpecificationExecutor<Place> {

    @Query("SELECT DISTINCT p FROM Place p JOIN FETCH p.user LEFT JOIN FETCH p.tags WHERE p.id = :id")
    Optional<Place> findByIdWithUser(@Param("id") UUID id);

    @Query(value = "SELECT DISTINCT p FROM Place p LEFT JOIN FETCH p.user LEFT JOIN FETCH p.tags WHERE p.user.id = :userId AND p.isCurrentPosition = false",
           countQuery = "SELECT COUNT(p) FROM Place p WHERE p.user.id = :userId AND p.isCurrentPosition = false")
    Page<Place> findAllByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Place p LEFT JOIN FETCH p.user LEFT JOIN FETCH p.tags WHERE p.user.id = :userId AND p.isCurrentPosition = false ORDER BY p.updatedAt DESC, p.createdAt DESC")
    List<Place> findAllByUserIdNoPage(@Param("userId") UUID userId);

    @Query("SELECT DISTINCT p FROM Place p LEFT JOIN FETCH p.user LEFT JOIN FETCH p.tags WHERE p.user.id = :userId AND p.isCurrentPosition = true")
    Optional<Place> findByUserIdAndIsCurrentPositionTrue(@Param("userId") UUID userId);

    @Query("SELECT DISTINCT p FROM Place p LEFT JOIN FETCH p.user LEFT JOIN FETCH p.tags WHERE p.id IN :ids AND p.isCurrentPosition = false")
    List<Place> findAllByIdInDetailed(@Param("ids") Collection<UUID> ids);

    @Query("SELECT DISTINCT p FROM Place p LEFT JOIN FETCH p.user LEFT JOIN FETCH p.tags t WHERE t.id IN :tagIds AND p.isCurrentPosition = false")
    List<Place> findAllByTagIds(@Param("tagIds") Collection<UUID> tagIds);
}
