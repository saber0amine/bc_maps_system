package org.example.bc_maps_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ImageUploadResponse {
    private String imageUrl;
    private String fileName;
}
