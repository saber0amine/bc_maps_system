package org.example.bc_maps_system.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.bc_maps_system.dto.CollectionResponse;
import org.example.bc_maps_system.dto.PlaceResponse;
import org.example.bc_maps_system.interceptor.TokenInterceptor;
import org.example.bc_maps_system.model.Tag;
import org.example.bc_maps_system.model.Token;
import org.example.bc_maps_system.service.ExportService;
import org.example.bc_maps_system.service.PlaceService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/collections")
@RequiredArgsConstructor
public class CollectionController {

    private final PlaceService placeService;
    private final ExportService exportService;

    private Token caller(HttpServletRequest request) {
        return (Token) request.getAttribute(TokenInterceptor.TOKEN_ATTRIBUTE);
    }

    @GetMapping
    public ResponseEntity<List<CollectionResponse>> listCollections(HttpServletRequest request) {
        Token caller = caller(request);
        List<CollectionResponse> collections = new ArrayList<>();
        long total = placeService.accessiblePlaces(caller).size();
        collections.add(new CollectionResponse("all", "Tous les lieux", total, true));
        for (Tag tag : placeService.getAccessibleTags(caller)) {
            long count = placeService.getPlacesForCollection(caller, tag.getId().toString()).size();
            collections.add(new CollectionResponse(tag.getId().toString(), tag.getName(), count, false));
        }
        return ResponseEntity.ok(collections);
    }

    @GetMapping("/places")
    public ResponseEntity<List<PlaceResponse>> placesByTag(@RequestParam String tagId,
                                                           HttpServletRequest request) {
        Token caller = caller(request);
        return ResponseEntity.ok(placeService.getPlacesForCollection(caller, tagId)
                .stream()
                .map(place -> placeService.findById(place.getId(), caller))
                .toList());
    }

    @GetMapping("/{collectionId}/export")
    public ResponseEntity<String> exportCollection(
            @PathVariable String collectionId,
            @RequestParam(defaultValue = "geojson") String format,
            HttpServletRequest request) {
        Token caller = caller(request);
        var payload = exportService.export(placeService.getPlacesForCollection(caller, collectionId), format);
        return ResponseEntity.ok()
                .contentType(payload.mediaType())
                .eTag(payload.etag())
                .lastModified(payload.lastModified())
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + payload.filename() + "\"")
                .body(payload.body());
    }
}
