package org.example.bc_maps_system.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Set;

@Data
@Builder
public class PlaceResponse {

    private Long    id;
    private String  title;
    private String  description;
    private double  latitude;
    private double  longitude;
    private String  imageUrl;
    private boolean currentPosition;
    private String  userId;
    private Set<String> tags;
    private Instant createdAt;
    private Instant updatedAt;
}