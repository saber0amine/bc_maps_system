package org.example.bc_maps_system.service;

import org.example.bc_maps_system.dto.AuthResponse;
import org.example.bc_maps_system.dto.LoginRequest;
import org.example.bc_maps_system.dto.RegisterRequest;
import org.example.bc_maps_system.model.Token;
import org.example.bc_maps_system.model.User;
import org.example.bc_maps_system.repository.TokenRepository;
import org.example.bc_maps_system.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       TokenRepository tokenRepository,
                       BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        boolean emailExists = userRepository.findByEmail(request.getEmail()).isPresent();
        if (emailExists) {
            throw new IllegalArgumentException("Email already in use");
        }

        boolean usernameExists = userRepository.findByUsername(request.getUsername()).isPresent();
        if (usernameExists) {
            throw new IllegalArgumentException("Username already taken");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        Token masterToken = createMasterToken(user);
        return new AuthResponse(user.getId(), user.getUsername(), user.getEmail(), masterToken.getValue());
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        Token masterToken = tokenRepository.findMasterTokenByUserId(user.getId())
                .orElseGet(() -> createMasterToken(user));

        if (!masterToken.isRevoked()) {
            return new AuthResponse(user.getId(), user.getUsername(), user.getEmail(), masterToken.getValue());
        }

        Token newMasterToken = createMasterToken(user);
        return new AuthResponse(user.getId(), user.getUsername(), user.getEmail(), newMasterToken.getValue());
    }

    private Token createMasterToken(User user) {
        Token token = new Token();
        token.setValue(UUID.randomUUID().toString().replace("-", ""));
        token.setUser(user);
        token.setDescription("Master token - full access");
        token.setMasterToken(true);
        return tokenRepository.save(token);
    }
}