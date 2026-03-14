package org.example.bc_maps_system.repository;

import org.example.bc_maps_system.model.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TokenRepository extends JpaRepository<Token, UUID> {

    Optional<Token> findByValue(String value);

    List<Token> findByUserIdAndIsRevokedFalse(UUID userId);

    @Query("SELECT t FROM Token t WHERE t.user.id = :userId AND t.isMasterToken = true AND t.isRevoked = false")
    Optional<Token> findMasterTokenByUserId(UUID userId);
}