package org.example.bc_maps_system.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.bc_maps_system.dto.ExternalSourceRequest;
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

@RestController
@RequestMapping("/api/external-sources")
public class ExternalSourceController {

    private final ExternalSourceRepository externalSourceRepository;
    private final UserRepository userRepository;
    private final AggregatorService aggregatorService;

    public ExternalSourceController(ExternalSourceRepository externalSourceRepository,
                                    UserRepository userRepository,
                                    AggregatorService aggregatorService) {
        this.externalSourceRepository = externalSourceRepository;
        this.userRepository = userRepository;
        this.aggregatorService = aggregatorService;
    }

    @PostMapping
    public ResponseEntity<ExternalSource> add(@RequestBody ExternalSourceRequest request,
                                              HttpServletRequest httpRequest) {
        Token token = (Token) httpRequest.getAttribute(TokenInterceptor.TOKEN_ATTRIBUTE);
        User user = userRepository.findById(token.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        ExternalSource source = new ExternalSource();
        source.setUser(user);
        source.setName(request.getName());
        source.setServerUrl(request.getServerUrl());
        source.setToken(request.getToken());

        return ResponseEntity.status(HttpStatus.CREATED).body(externalSourceRepository.save(source));
    }

    @GetMapping("/aggregate")
    public ResponseEntity<List<Map>> aggregate(HttpServletRequest httpRequest) {
        Token token = (Token) httpRequest.getAttribute(TokenInterceptor.TOKEN_ATTRIBUTE);
        List<Map> places = aggregatorService.aggregatePlaces(token.getUser().getId());
        return ResponseEntity.ok(places);
    }
}