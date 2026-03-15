package org.example.bc_maps_system.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
public class PlaceResponse {

    private UUID id;
    private String  title;
    private String  description;
    private double  latitude;
    private double  longitude;
    private String  imageUrl;
    private boolean currentPosition;
    private String  userId;
    private Set<String> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}