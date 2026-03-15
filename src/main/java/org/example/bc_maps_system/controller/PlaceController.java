package org.example.bc_maps_system.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.bc_maps_system.dto.PlaceRequest;
import org.example.bc_maps_system.dto.PlaceResponse;
import org.example.bc_maps_system.interceptor.TokenInterceptor;
import org.example.bc_maps_system.model.Token;
import org.example.bc_maps_system.service.PlaceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;

    @PostMapping
    public ResponseEntity<PlaceResponse> createPlace(@Valid @RequestBody PlaceRequest request,
                                                     HttpServletRequest httpRequest) {
        Token caller = (Token) httpRequest.getAttribute(TokenInterceptor.TOKEN_ATTRIBUTE);
        String userId = caller.getUser().getId().toString();
        PlaceResponse response = placeService.create(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}