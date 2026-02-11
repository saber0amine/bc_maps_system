package org.example.bc_maps_system.repository;

import org.example.bc_maps_system.model.Place;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PlaceRepository extends JpaRepository<Place, UUID> {
}
