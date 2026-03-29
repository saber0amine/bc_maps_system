package org.example.bc_maps_system.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.bc_maps_system.dto.ExternalSourceRequest;
import org.example.bc_maps_system.dto.ExternalSourceResponse;
import org.example.bc_maps_system.exception.UnauthorizedException;
import org.example.bc_maps_system.interceptor.TokenInterceptor;
import org.example.bc_maps_system.model.ExternalSource;
import org.example.bc_maps_system.model.Token;
import org.example.bc_maps_system.model.User;
import org.example.bc_maps_system.repository.ExternalSourceRepository;
import org.example.bc_maps_system.repository.UserRepository;
import org.example.bc_maps_system.service.AggregatorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/external-sources")
@RequiredArgsConstructor
public class ExternalSourceController {

    private final ExternalSourceRepository externalSourceRepository;
    private final UserRepository userRepository;
    private final AggregatorService aggregatorService;

    private Token caller(HttpServletRequest httpRequest) {
        return (Token) httpRequest.getAttribute(TokenInterceptor.TOKEN_ATTRIBUTE);
    }

    @PostMapping
    public ResponseEntity<ExternalSourceResponse> add(@RequestBody ExternalSourceRequest request,
                                              HttpServletRequest httpRequest) {
        Token caller = caller(httpRequest);
        ensureMaster(caller);
        User user = userRepository.findById(caller.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        ExternalSource source = new ExternalSource();
        source.setUser(user);
        source.setName(request.getName());
        source.setServerUrl(normalizeUrl(request.getServerUrl()));
        source.setToken(request.getToken());

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(externalSourceRepository.save(source)));
    }

    @GetMapping
    public ResponseEntity<List<ExternalSourceResponse>> list(HttpServletRequest httpRequest) {
        Token caller = caller(httpRequest);
        ensureMaster(caller);
        return ResponseEntity.ok(externalSourceRepository.findByUserIdOrderByCreatedAtDesc(caller.getUser().getId())
                .stream()
                .map(this::toResponse)
                .toList());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, HttpServletRequest httpRequest) {
        Token caller = caller(httpRequest);
        ensureMaster(caller);
        ExternalSource source = externalSourceRepository.findById(id).orElse(null);
        if (source == null) {
            return ResponseEntity.notFound().build();
        }
        if (!source.getUser().getId().equals(caller.getUser().getId())) {
            return ResponseEntity.status(403).build();
        }
        externalSourceRepository.delete(source);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/aggregate")
    public ResponseEntity<List<Map<String, Object>>> aggregate(HttpServletRequest httpRequest) {
        Token caller = caller(httpRequest);
        List<Map<String, Object>> places = aggregatorService.aggregatePlaces(caller.getUser().getId());
        return ResponseEntity.ok(places);
    }

    private ExternalSourceResponse toResponse(ExternalSource source) {
        return new ExternalSourceResponse(
                source.getId(),
                source.getName(),
                source.getServerUrl(),
                maskToken(source.getToken()),
                source.isActive(),
                source.getLastSync(),
                source.getCreatedAt()
        );
    }

    private String maskToken(String token) {
        if (token == null || token.length() < 8) {
            return "********";
        }
        return token.substring(0, 4) + "••••" + token.substring(token.length() - 4);
    }

    private void ensureMaster(Token caller) {
        if (!caller.isMasterToken()) {
            throw new UnauthorizedException("Le token maître est requis pour gérer les sources externes");
        }
    }

    private String normalizeUrl(String url) {
        return url == null ? null : url.trim().replaceAll("/$", "");
    }
}
