package org.example.bc_maps_system.repository;

import org.example.bc_maps_system.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TagRepository extends JpaRepository<Tag, UUID> {
    Optional<Tag> findByUserIdAndNameIgnoreCase(UUID userId, String name);

    @Query("SELECT t FROM Tag t WHERE t.user.id = :userId ORDER BY lower(t.name) ASC")
    List<Tag> findAllByUserIdOrderByNameAsc(@Param("userId") UUID userId);

    @Query("SELECT t FROM Tag t WHERE t.user.id = :userId AND t.id IN :ids ORDER BY lower(t.name) ASC")
    List<Tag> findAllByUserIdAndIdIn(@Param("userId") UUID userId, @Param("ids") List<UUID> ids);
}
