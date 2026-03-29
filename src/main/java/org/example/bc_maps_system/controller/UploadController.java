package org.example.bc_maps_system.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.bc_maps_system.dto.ImageUploadResponse;
import org.example.bc_maps_system.interceptor.TokenInterceptor;
import org.example.bc_maps_system.model.Token;
import org.example.bc_maps_system.service.ImageStorageService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/uploads")
@RequiredArgsConstructor
public class UploadController {

    private final ImageStorageService imageStorageService;

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageUploadResponse> uploadImage(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest httpRequest) throws Exception {
        Token caller = (Token) httpRequest.getAttribute(TokenInterceptor.TOKEN_ATTRIBUTE);
        if (caller == null) {
            return ResponseEntity.status(401).build();
        }

        ImageStorageService.StoredImage stored = imageStorageService.storePlaceImage(file);
        String absoluteUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(stored.relativeUrl())
                .toUriString();
        return ResponseEntity.ok(new ImageUploadResponse(absoluteUrl, stored.fileName()));
    }
}
