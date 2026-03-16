package org.example.bc_maps_system.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.bc_maps_system.dto.PlaceRequest;
import org.example.bc_maps_system.dto.PlaceResponse;
import org.example.bc_maps_system.exception.PlaceNotFoundException;
import org.example.bc_maps_system.exception.UnauthorizedException;
import org.example.bc_maps_system.exception.UserNotFoundException;
import org.example.bc_maps_system.mapper.PlaceMapper;
import org.example.bc_maps_system.model.Place;
import org.example.bc_maps_system.model.Tag;
import org.example.bc_maps_system.model.User;
import org.example.bc_maps_system.repository.PlaceRepository;
import org.example.bc_maps_system.repository.TagRepository;
import org.example.bc_maps_system.repository.UserRepository;
import org.example.bc_maps_system.specification.PlaceSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
        Place place = placeMapper.toEntity(request);
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé avec l'id: " + userId));
        place.setUser(user);
        place.setIsCurrentPosition(false);

        Place saved = placeRepository.save(place);
        attachTags(saved, request.getTags());
        return placeMapper.toResponse(saved);
    }

    public PlaceResponse findById(UUID id, UUID userId) {
        Place place = placeRepository.findByIdWithUser(id)
                .orElseThrow(() -> new PlaceNotFoundException(id));
        checkOwnership(place, userId);
        return placeMapper.toResponse(place);
    }

    @Transactional
    public Page<PlaceResponse> findAll(UUID userId, Pageable pageable) {
        return placeRepository
                .findAllByUserId(userId, pageable)
                .map(placeMapper::toResponse);
    }

    @Transactional
    public PlaceResponse update(UUID id, UUID userId, PlaceRequest request) {
        Place place = placeRepository.findByIdWithUser(id)
                .orElseThrow(() -> new PlaceNotFoundException(id));
        checkOwnership(place, userId);

        placeMapper.updateEntity(request, place);

        place.getTags().forEach(tag -> tag.getPlaces().remove(place));
        tagRepository.saveAll(place.getTags());
        place.getTags().clear();

        Place saved = placeRepository.save(place);
        attachTags(saved, request.getTags());

        return placeMapper.toResponse(saved);
    }

    @Transactional
    public void delete(UUID id, UUID userId) {
        Place place = placeRepository.findByIdWithUser(id)
                .orElseThrow(() -> new PlaceNotFoundException(id));
        checkOwnership(place, userId);

        place.getTags().forEach(tag -> tag.getPlaces().remove(place));
        tagRepository.saveAll(place.getTags());

        placeRepository.delete(place);
        log.info("Lieu {} supprimé par l'utilisateur {}", id, userId);
    }

    @Transactional
    public PlaceResponse updateCurrentPosition(String userId, BigDecimal latitude, BigDecimal longitude) {
        Place current = placeRepository.findByUserIdAndIsCurrentPositionTrue(UUID.fromString(userId))
                .orElseGet(() -> {
                    User user = userRepository.findById(UUID.fromString(userId))
                            .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé"));
                    Place p = new Place();
                    p.setUser(user);
                    p.setIsCurrentPosition(true);
                    p.setTitle("Ma position");
                    return p;
                });

        current.setLatitude(latitude);
        current.setLongitude(longitude);
        return placeMapper.toResponse(placeRepository.save(current));
    }

    @Transactional
    public void deleteCurrentPosition(String userId) {
        placeRepository.findByUserIdAndIsCurrentPositionTrue(UUID.fromString(userId))
                .ifPresent(p -> {
                    placeRepository.delete(p);
                    log.info("Position courante supprimée pour l'utilisateur {}", userId);
                });
    }

    public PlaceResponse getCurrentPosition(String userId) {
        Place current = placeRepository.findByUserIdAndIsCurrentPositionTrue(UUID.fromString(userId))
                .orElseThrow(() -> new PlaceNotFoundException("Position courante non trouvée"));
        return placeMapper.toResponse(current);
    }

    @Transactional
    public Page<PlaceResponse> search(UUID userId, String query, String tag, Pageable pageable) {
        Specification<Place> spec = Specification
                .where(PlaceSpecification.hasUser(userId))
                .and(PlaceSpecification.isNotCurrentPosition());

        if (query != null && !query.isBlank())
            spec = spec.and(PlaceSpecification.titleOrDescriptionContains(query));

        if (tag != null && !tag.isBlank())
            spec = spec.and(PlaceSpecification.hasTag(tag));

        return placeRepository.findAll(spec, pageable).map(placeMapper::toResponse);
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
                    tag.getPlaces().add(place);
                    place.getTags().add(tag);
                    tagRepository.save(tag);
                });
    }

    private void checkOwnership(Place place, UUID userId) {
        if (!place.getUser().getId().equals(userId)) {
            throw new UnauthorizedException(
                    "L'utilisateur " + userId + " n'est pas propriétaire du lieu " + place.getId());
        }
    }
}
