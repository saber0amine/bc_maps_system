package org.example.bc_maps_system.dto;

import java.util.UUID;

public class AuthResponse {

    private UUID userId;
    private String username;
    private String email;
    private String token;

    public AuthResponse(UUID userId, String username, String email, String token) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.token = token;
    }

    public UUID getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getToken() { return token; }
}