package org.example.bc_maps_system.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Set;

@Data
public class PlaceRequest {

    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 255, message = "Le titre ne peut pas dépasser 255 caractères")
    private String title;

    @Size(max = 2000, message = "La description ne peut pas dépasser 2000 caractères")
    private String description;

    @NotNull(message = "La latitude est obligatoire")
    @DecimalMin(value = "-90.0",  message = "La latitude doit être comprise entre -90 et 90")
    @DecimalMax(value =  "90.0",  message = "La latitude doit être comprise entre -90 et 90")
    private Double latitude;

    @NotNull(message = "La longitude est obligatoire")
    @DecimalMin(value = "-180.0", message = "La longitude doit être comprise entre -180 et 180")
    @DecimalMax(value =  "180.0", message = "La longitude doit être comprise entre -180 et 180")
    private Double longitude;

    @Size(max = 500, message = "L'URL de l'image ne peut pas dépasser 500 caractères")
    private String imageUrl;

    private Set<String> tags;
}
