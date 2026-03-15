package org.example.bc_maps_system.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.bc_maps_system.dto.PlaceRequest;
import org.example.bc_maps_system.dto.PlaceResponse;
import org.example.bc_maps_system.mapper.PlaceMapper;
import org.example.bc_maps_system.model.Place;
import org.example.bc_maps_system.model.Tag;
import org.example.bc_maps_system.model.User;
import org.example.bc_maps_system.repository.PlaceRepository;
import org.example.bc_maps_system.repository.TagRepository;
import org.example.bc_maps_system.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceService {

    private final PlaceMapper placeMapper;
    private final PlaceRepository placeRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;


    @Transactional
    public PlaceResponse create(PlaceRequest request, String userId) {
        log.debug("Création d'un lieu '{}' pour l'utilisateur {}", request.getTitle(), userId);

        Place place = placeMapper.toEntity(request);
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec l'id: " + userId));
        place.setUser(user);
        place.setIsCurrentPosition(false);

        attachTags(place, request.getTags());

        Place saved = placeRepository.save(place);
        log.info("Lieu créé avec l'id {} pour l'utilisateur {}", saved.getId(), userId);
        return placeMapper.toResponse(saved);
    }


    /**
     * Attache ou crée les tags associés à un lieu.
     * Les tags inexistants sont créés à la volée.
     */
    private void attachTags(Place place, Set<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) return;

        tagNames.stream()
                .map(String::trim)
                .filter(name -> !name.isBlank())
                .forEach(name -> {
                    Tag tag = tagRepository.findByNameIgnoreCase(name)
                            .orElseGet(() -> {
                                Tag t = new Tag();
                                t.setName(name);
                                return tagRepository.save(t);
                            });
                    place.getTags().add(tag);
                });
    }
}
