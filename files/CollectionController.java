package org.example.bc_maps_system.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.bc_maps_system.interceptor.TokenInterceptor;
import org.example.bc_maps_system.model.*;
import org.example.bc_maps_system.repository.PlaceRepository;
import org.example.bc_maps_system.repository.TagRepository;
import org.example.bc_maps_system.service.PermissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/collections")
public class CollectionController {

    private final TagRepository tagRepository;
    private final PlaceRepository placeRepository;
    private final PermissionService permissionService;

    public CollectionController(TagRepository tagRepository,
                                PlaceRepository placeRepository,
                                PermissionService permissionService) {
        this.tagRepository = tagRepository;
        this.placeRepository = placeRepository;
        this.permissionService = permissionService;
    }

    @GetMapping
    public ResponseEntity<List<Tag>> listCollections(HttpServletRequest request) {
        Token token = (Token) request.getAttribute(TokenInterceptor.TOKEN_ATTRIBUTE);

        List<Tag> tags = tagRepository.findAll().stream()
                .filter(tag -> permissionService.canRead(token, ResourceType.TAG, tag.getId()))
                .toList();

        return ResponseEntity.ok(tags);
    }

    @GetMapping("/places")
    public ResponseEntity<List<Place>> placesByTag(@RequestParam UUID tagId,
                                                   HttpServletRequest request) {
        Token token = (Token) request.getAttribute(TokenInterceptor.TOKEN_ATTRIBUTE);

        if (!permissionService.canRead(token, ResourceType.TAG, tagId)) {
            return ResponseEntity.status(403).build();
        }

        Tag tag = tagRepository.findById(tagId).orElse(null);
        if (tag == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(tag.getPlaces());
    }
}