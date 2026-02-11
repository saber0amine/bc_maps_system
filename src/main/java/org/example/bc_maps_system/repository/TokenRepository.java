package org.example.bc_maps_system.repository;

import org.example.bc_maps_system.model.Token;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TokenRepository extends JpaRepository<Token, UUID> {
}
