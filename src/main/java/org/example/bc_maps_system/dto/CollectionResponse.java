package org.example.bc_maps_system.dto;

public record CollectionResponse(
        String id,
        String name,
        long placeCount,
        boolean global
) {}
