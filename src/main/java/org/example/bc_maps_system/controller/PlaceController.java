package org.example.bc_maps_system.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.bc_maps_system.dto.PlaceRequest;
import org.example.bc_maps_system.dto.PlaceResponse;
import org.example.bc_maps_system.interceptor.TokenInterceptor;
import org.example.bc_maps_system.model.Token;
import org.example.bc_maps_system.service.PlaceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;

    private UUID getUserId(HttpServletRequest httpRequest) {
        Token caller = (Token) httpRequest.getAttribute(TokenInterceptor.TOKEN_ATTRIBUTE);
        return caller.getUser().getId();
    }

    @PostMapping
    public ResponseEntity<PlaceResponse> createPlace(
            @Valid @RequestBody PlaceRequest request,
            HttpServletRequest httpRequest) {
        PlaceResponse response = placeService.create(request, getUserId(httpRequest).toString());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlaceResponse> getPlaceById(
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(placeService.findById(id, getUserId(httpRequest)));
    }

    @GetMapping
    public ResponseEntity<Page<PlaceResponse>> findAll(
            @PageableDefault(size = 20) Pageable pageable,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(placeService.findAll(
                getUserId(httpRequest),
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlace(
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {
        placeService.delete(id, getUserId(httpRequest));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlaceResponse> updatePlace(
            @PathVariable UUID id,
            @Valid @RequestBody PlaceRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(placeService.update(id, getUserId(httpRequest), request));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<PlaceResponse>> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String tag,
            @PageableDefault(size = 20) Pageable pageable,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(placeService.search(
                getUserId(httpRequest), query, tag,
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize())));
    }


    @GetMapping("/position")
    public ResponseEntity<PlaceResponse> getCurrentPosition(HttpServletRequest httpRequest) {
        return ResponseEntity.ok(placeService.getCurrentPosition(getUserId(httpRequest).toString()));
    }

    @PutMapping("/position")
    public ResponseEntity<PlaceResponse> updateCurrentPosition(
            @RequestParam BigDecimal lat,
            @RequestParam BigDecimal lng,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(placeService.updateCurrentPosition(
                getUserId(httpRequest).toString(), lat, lng));
    }

    @DeleteMapping("/position")
    public ResponseEntity<Void> deleteCurrentPosition(HttpServletRequest httpRequest) {
        placeService.deleteCurrentPosition(getUserId(httpRequest).toString());
        return ResponseEntity.noContent().build();
    }
}