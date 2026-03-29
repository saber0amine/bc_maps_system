package org.example.bc_maps_system.service;

import org.example.bc_maps_system.model.Token;
import org.example.bc_maps_system.model.User;
import org.example.bc_maps_system.repository.TokenRepository;
import org.example.bc_maps_system.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TokenService {

    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;

    public TokenService(TokenRepository tokenRepository, UserRepository userRepository) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Token generateToken(UUID userId, String description, LocalDateTime expiresAt) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Token token = new Token();
        token.setValue(UUID.randomUUID().toString().replace("-", ""));
        token.setUser(user);
        token.setDescription(description);
        token.setExpiresAt(expiresAt);
        token.setMasterToken(false);

        return tokenRepository.save(token);
    }

    @Transactional
    public void revokeToken(UUID tokenId) {
        Token token = tokenRepository.findById(tokenId)
                .orElseThrow(() -> new RuntimeException("Token not found"));
        token.setRevoked(true);
        tokenRepository.save(token);
    }

    public Optional<Token> findById(UUID tokenId) {
        return tokenRepository.findById(tokenId);
    }

    public Optional<Token> findByValue(String value) {
        return tokenRepository.findByValue(value);
    }

    public List<Token> findActiveTokensByUser(UUID userId) {
        return tokenRepository.findByUserIdAndIsRevokedFalse(userId);
    }

    public boolean isValid(Token token) {
        return !token.isRevoked() && !isExpired(token);
    }

    public boolean isExpired(Token token) {
        if (token.getExpiresAt() == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(token.getExpiresAt());
    }
}
