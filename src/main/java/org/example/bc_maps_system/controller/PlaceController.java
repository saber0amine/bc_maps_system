package org.example.bc_maps_system.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.bc_maps_system.dto.ImportResultResponse;
import org.example.bc_maps_system.dto.PlaceRequest;
import org.example.bc_maps_system.dto.PlaceResponse;
import org.example.bc_maps_system.interceptor.TokenInterceptor;
import org.example.bc_maps_system.model.Token;
import org.example.bc_maps_system.service.ImportService;
import org.example.bc_maps_system.service.PlaceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;
    private final ImportService importService;

    private Token caller(HttpServletRequest httpRequest) {
        return (Token) httpRequest.getAttribute(TokenInterceptor.TOKEN_ATTRIBUTE);
    }

    @PostMapping
    public ResponseEntity<PlaceResponse> createPlace(
            @Valid @RequestBody PlaceRequest request,
            HttpServletRequest httpRequest) {
        PlaceResponse response = placeService.create(request, caller(httpRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportResultResponse> importPlaces(
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) String defaultTag,
            HttpServletRequest httpRequest) {
        int imported = importService.importPlaces(caller(httpRequest), file, defaultTag);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ImportResultResponse(imported));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlaceResponse> getPlaceById(
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(placeService.findById(id, caller(httpRequest)));
    }

    @GetMapping
    public ResponseEntity<Page<PlaceResponse>> findAll(
            @PageableDefault(size = 20) Pageable pageable,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(placeService.findAll(
                caller(httpRequest),
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlace(
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {
        placeService.delete(id, caller(httpRequest));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlaceResponse> updatePlace(
            @PathVariable UUID id,
            @Valid @RequestBody PlaceRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(placeService.update(id, caller(httpRequest), request));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<PlaceResponse>> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String tag,
            @PageableDefault(size = 20) Pageable pageable,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(placeService.search(
                caller(httpRequest), query, tag,
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize())));
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<PlaceResponse>> nearby(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "5") double radiusKm,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(placeService.nearby(caller(httpRequest), lat, lng, radiusKm));
    }

    @GetMapping("/position")
    public ResponseEntity<PlaceResponse> getCurrentPosition(HttpServletRequest httpRequest) {
        return ResponseEntity.ok(placeService.getCurrentPosition(caller(httpRequest)));
    }

    @PutMapping("/position")
    public ResponseEntity<PlaceResponse> updateCurrentPosition(
            @RequestParam BigDecimal lat,
            @RequestParam BigDecimal lng,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(placeService.updateCurrentPosition(
                caller(httpRequest), lat, lng));
    }

    @DeleteMapping("/position")
    public ResponseEntity<Void> deleteCurrentPosition(HttpServletRequest httpRequest) {
        placeService.deleteCurrentPosition(caller(httpRequest));
        return ResponseEntity.noContent().build();
    }
}
