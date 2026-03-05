package org.example.bc_maps_system.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.bc_maps_system.dto.CreateTokenRequest;
import org.example.bc_maps_system.dto.TokenResponse;
import org.example.bc_maps_system.interceptor.TokenInterceptor;
import org.example.bc_maps_system.model.Token;
import org.example.bc_maps_system.service.PermissionService;
import org.example.bc_maps_system.service.TokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tokens")
public class TokenController {

    private final TokenService tokenService;
    private final PermissionService permissionService;

    public TokenController(TokenService tokenService, PermissionService permissionService) {
        this.tokenService = tokenService;
        this.permissionService = permissionService;
    }

    @PostMapping
    public ResponseEntity<TokenResponse> create(@RequestBody CreateTokenRequest request,
                                                HttpServletRequest httpRequest) {
        Token caller = (Token) httpRequest.getAttribute(TokenInterceptor.TOKEN_ATTRIBUTE);
        UUID userId = caller.getUser().getId();

        Token token = tokenService.generateToken(userId, request.getDescription(), request.getExpiresAt());

        if (request.getPermissions() != null) {
            for (CreateTokenRequest.PermissionEntry entry : request.getPermissions()) {
                permissionService.addPermission(token, entry.getResourceType(), entry.getResourceId(), entry.getAccessType());
            }
        }

        String serverUrl = buildServerUrl(httpRequest);
        return ResponseEntity.ok(toResponse(token, serverUrl));
    }

    @GetMapping
    public ResponseEntity<List<TokenResponse>> listMine(HttpServletRequest httpRequest) {
        Token caller = (Token) httpRequest.getAttribute(TokenInterceptor.TOKEN_ATTRIBUTE);
        UUID userId = caller.getUser().getId();
        String serverUrl = buildServerUrl(httpRequest);

        List<TokenResponse> responses = tokenService.findActiveTokensByUser(userId)
                .stream()
                .map(t -> toResponse(t, serverUrl))
                .toList();

        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> revoke(@PathVariable UUID id, HttpServletRequest httpRequest) {
        Token caller = (Token) httpRequest.getAttribute(TokenInterceptor.TOKEN_ATTRIBUTE);
        Token target = tokenService.findByValue(id.toString())
                .orElse(null);

        if (target == null) {
            return ResponseEntity.notFound().build();
        }

        if (!target.getUser().getId().equals(caller.getUser().getId())) {
            return ResponseEntity.status(403).build();
        }

        tokenService.revokeToken(id);
        return ResponseEntity.noContent().build();
    }

    private TokenResponse toResponse(Token token, String serverUrl) {
        return new TokenResponse(
                token.getId(),
                token.getValue(),
                token.getDescription(),
                token.getCreatedAt(),
                token.getExpiresAt(),
                token.isRevoked(),
                serverUrl
        );
    }

    private String buildServerUrl(HttpServletRequest request) {
        return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
    }
}