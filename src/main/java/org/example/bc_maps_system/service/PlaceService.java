package org.example.bc_maps_system.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.bc_maps_system.dto.PlaceRequest;
import org.example.bc_maps_system.dto.PlaceResponse;
import org.example.bc_maps_system.exception.PlaceNotFoundException;
import org.example.bc_maps_system.exception.UnauthorizedException;
import org.example.bc_maps_system.mapper.PlaceMapper;
import org.example.bc_maps_system.model.Place;
import org.example.bc_maps_system.model.ResourceType;
import org.example.bc_maps_system.model.Tag;
import org.example.bc_maps_system.model.Token;
import org.example.bc_maps_system.model.User;
import org.example.bc_maps_system.repository.PlaceRepository;
import org.example.bc_maps_system.repository.TagRepository;
import org.example.bc_maps_system.repository.UserRepository;
import org.example.bc_maps_system.specification.PlaceSpecification;
import org.example.bc_maps_system.util.GeoUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceService {

    private final PlaceMapper placeMapper;
    private final PlaceRepository placeRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final PermissionService permissionService;

    @Transactional
    public PlaceResponse create(PlaceRequest request, Token caller) {
        Place place = placeMapper.toEntity(request);
        User user = userRepository.findById(caller.getUser().getId())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        place.setUser(user);
        place.setIsCurrentPosition(false);

        Place saved = placeRepository.save(place);
        attachTags(saved, request.getTags(), caller);
        return placeMapper.toResponse(saved);
    }

    public PlaceResponse findById(UUID id, Token caller) {
        Place place = placeRepository.findByIdWithUser(id)
                .orElseThrow(() -> new PlaceNotFoundException(id));
        ensureCanRead(place, caller);
        return placeMapper.toResponse(place);
    }

    @Transactional
    public Page<PlaceResponse> findAll(Token caller, Pageable pageable) {
        if (caller.isMasterToken()) {
            return placeRepository
                    .findAllByUserId(caller.getUser().getId(), pageable)
                    .map(placeMapper::toResponse);
        }
        return page(accessiblePlaces(caller), pageable).map(placeMapper::toResponse);
    }

    @Transactional
    public PlaceResponse update(UUID id, Token caller, PlaceRequest request) {
        Place place = placeRepository.findByIdWithUser(id)
                .orElseThrow(() -> new PlaceNotFoundException(id));
        ensureCanWrite(place, caller);

        placeMapper.updateEntity(request, place);
        detachAllTags(place);
        Place saved = placeRepository.save(place);
        attachTags(saved, request.getTags(), caller);

        return placeMapper.toResponse(saved);
    }

    @Transactional
    public void delete(UUID id, Token caller) {
        Place place = placeRepository.findByIdWithUser(id)
                .orElseThrow(() -> new PlaceNotFoundException(id));
        ensureCanWrite(place, caller);
        detachAllTags(place);
        placeRepository.delete(place);
        log.info("Lieu {} supprimé par {}", id, caller.getUser().getId());
    }

    @Transactional
    public PlaceResponse updateCurrentPosition(Token caller, BigDecimal latitude, BigDecimal longitude) {
        ensureMasterToken(caller);
        Place current = placeRepository.findByUserIdAndIsCurrentPositionTrue(caller.getUser().getId())
                .orElseGet(() -> {
                    User user = userRepository.findById(caller.getUser().getId())
                            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
                    Place p = new Place();
                    p.setUser(user);
                    p.setIsCurrentPosition(true);
                    p.setTitle("Ma position");
                    p.setDescription("Position courante synchronisée");
                    return p;
                });

        current.setLatitude(latitude);
        current.setLongitude(longitude);
        return placeMapper.toResponse(placeRepository.save(current));
    }

    @Transactional
    public void deleteCurrentPosition(Token caller) {
        ensureMasterToken(caller);
        placeRepository.findByUserIdAndIsCurrentPositionTrue(caller.getUser().getId())
                .ifPresent(p -> {
                    placeRepository.delete(p);
                    log.info("Position courante supprimée pour l'utilisateur {}", caller.getUser().getId());
                });
    }

    public PlaceResponse getCurrentPosition(Token caller) {
        ensureMasterToken(caller);
        Place current = placeRepository.findByUserIdAndIsCurrentPositionTrue(caller.getUser().getId())
                .orElseThrow(() -> new PlaceNotFoundException("Position courante non trouvée"));
        return placeMapper.toResponse(current);
    }

    @Transactional
    public Page<PlaceResponse> search(Token caller, String query, String tag, Pageable pageable) {
        if (caller.isMasterToken()) {
            Specification<Place> spec = Specification
                    .where(PlaceSpecification.hasUser(caller.getUser().getId()))
                    .and(PlaceSpecification.isNotCurrentPosition());

            if (query != null && !query.isBlank()) {
                spec = spec.and(PlaceSpecification.titleOrDescriptionContains(query));
            }
            if (tag != null && !tag.isBlank()) {
                spec = spec.and(PlaceSpecification.hasTag(tag));
            }
            return placeRepository.findAll(spec, pageable).map(placeMapper::toResponse);
        }

        List<Place> filtered = filterPlaces(accessiblePlaces(caller), query, tag);
        return page(filtered, pageable).map(placeMapper::toResponse);
    }

    public List<PlaceResponse> nearby(Token caller, double latitude, double longitude, double radiusKm) {
        return accessiblePlaces(caller).stream()
                .filter(place -> GeoUtils.distanceKm(
                        latitude,
                        longitude,
                        place.getLatitude().doubleValue(),
                        place.getLongitude().doubleValue()) <= radiusKm)
                .sorted(Comparator.comparingDouble(place -> GeoUtils.distanceKm(
                        latitude,
                        longitude,
                        place.getLatitude().doubleValue(),
                        place.getLongitude().doubleValue())))
                .map(placeMapper::toResponse)
                .toList();
    }

    public List<Place> getPlacesForCollection(Token caller, String collectionId) {
        if (collectionId == null || collectionId.isBlank() || "all".equalsIgnoreCase(collectionId)) {
            return accessiblePlaces(caller);
        }
        UUID tagId = UUID.fromString(collectionId);
        if (caller.isMasterToken()) {
            Tag tag = tagRepository.findById(tagId).orElseThrow(() -> new IllegalArgumentException("Collection introuvable"));
            if (!Objects.equals(tag.getUser().getId(), caller.getUser().getId())) {
                throw new UnauthorizedException("Collection non accessible");
            }
            return accessiblePlaces(caller).stream()
                    .filter(place -> place.getTags().stream().anyMatch(tagItem -> Objects.equals(tagItem.getId(), tagId)))
                    .toList();
        }
        if (!permissionService.canRead(caller, ResourceType.TAG, tagId)) {
            throw new UnauthorizedException("Collection non accessible");
        }
        return accessiblePlaces(caller).stream()
                .filter(place -> place.getTags().stream().anyMatch(tagItem -> Objects.equals(tagItem.getId(), tagId)))
                .toList();
    }

    public List<Tag> getAccessibleTags(Token caller) {
        if (caller.isMasterToken()) {
            return tagRepository.findAllByUserIdOrderByNameAsc(caller.getUser().getId());
        }
        Set<UUID> readableTagIds = permissionService.getReadableResourceIds(caller, ResourceType.TAG);
        if (readableTagIds.isEmpty()) {
            return List.of();
        }
        return tagRepository.findAllByUserIdAndIdIn(caller.getUser().getId(), new ArrayList<>(readableTagIds));
    }

    public List<Place> accessiblePlaces(Token caller) {
        if (caller.isMasterToken()) {
            return placeRepository.findAllByUserIdNoPage(caller.getUser().getId());
        }

        Set<UUID> readablePlaceIds = permissionService.getReadableResourceIds(caller, ResourceType.PLACE);
        Set<UUID> readableTagIds = permissionService.getReadableResourceIds(caller, ResourceType.TAG);

        LinkedHashMap<UUID, Place> merged = new LinkedHashMap<>();
        if (!readableTagIds.isEmpty()) {
            placeRepository.findAllByTagIds(readableTagIds).stream()
                    .filter(place -> Objects.equals(place.getUser().getId(), caller.getUser().getId()))
                    .forEach(place -> merged.put(place.getId(), place));
        }
        if (!readablePlaceIds.isEmpty()) {
            placeRepository.findAllByIdInDetailed(readablePlaceIds).stream()
                    .filter(place -> Objects.equals(place.getUser().getId(), caller.getUser().getId()))
                    .forEach(place -> merged.put(place.getId(), place));
        }
        return merged.values().stream()
                .sorted(Comparator.comparing(Place::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Place::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    private List<Place> filterPlaces(List<Place> places, String query, String tag) {
        return places.stream()
                .filter(place -> {
                    if (query == null || query.isBlank()) return true;
                    String haystack = (place.getTitle() + " " + Objects.toString(place.getDescription(), "")).toLowerCase();
                    return haystack.contains(query.trim().toLowerCase());
                })
                .filter(place -> {
                    if (tag == null || tag.isBlank()) return true;
                    String normalized = tag.trim().toLowerCase();
                    return place.getTags().stream().anyMatch(item -> item.getName().equalsIgnoreCase(normalized));
                })
                .toList();
    }

    private Page<Place> page(List<Place> places, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), places.size());
        List<Place> content = start >= places.size() ? List.of() : places.subList(start, end);
        return new PageImpl<>(content, pageable, places.size());
    }

    private void attachTags(Place place, Set<String> tagNames, Token caller) {
        if (tagNames == null || tagNames.isEmpty()) return;
        User owner = caller.getUser();

        tagNames.stream()
                .map(String::trim)
                .filter(name -> !name.isBlank())
                .forEach(name -> {
                    Tag tag = tagRepository.findByUserIdAndNameIgnoreCase(owner.getId(), name)
                            .orElseGet(() -> {
                                ensureMasterToken(caller);
                                Tag t = new Tag();
                                t.setName(name);
                                t.setUser(owner);
                                return tagRepository.save(t);
                            });

                    if (!caller.isMasterToken() && !permissionService.canWrite(caller, ResourceType.TAG, tag.getId())) {
                        throw new UnauthorizedException("Le token n'a pas les droits d'écriture sur la collection " + tag.getName());
                    }
                    tag.getPlaces().add(place);
                    place.getTags().add(tag);
                    tagRepository.save(tag);
                });
    }

    private void detachAllTags(Place place) {
        List<Tag> currentTags = new ArrayList<>(place.getTags());
        currentTags.forEach(tag -> tag.getPlaces().remove(place));
        tagRepository.saveAll(currentTags);
        place.getTags().clear();
    }

    private void ensureCanRead(Place place, Token caller) {
        if (caller.isMasterToken()) {
            checkOwnership(place, caller.getUser().getId());
            return;
        }
        if (permissionService.canRead(caller, ResourceType.PLACE, place.getId())) {
            return;
        }
        boolean throughTag = place.getTags().stream().anyMatch(tag -> permissionService.canRead(caller, ResourceType.TAG, tag.getId()));
        if (!throughTag) {
            throw new UnauthorizedException("Accès refusé au lieu " + place.getId());
        }
    }

    private void ensureCanWrite(Place place, Token caller) {
        if (caller.isMasterToken()) {
            checkOwnership(place, caller.getUser().getId());
            return;
        }
        if (permissionService.canWrite(caller, ResourceType.PLACE, place.getId())) {
            return;
        }
        boolean throughTag = place.getTags().stream().anyMatch(tag -> permissionService.canWrite(caller, ResourceType.TAG, tag.getId()));
        if (!throughTag) {
            throw new UnauthorizedException("Écriture refusée sur le lieu " + place.getId());
        }
    }

    private void checkOwnership(Place place, UUID userId) {
        if (!place.getUser().getId().equals(userId)) {
            throw new UnauthorizedException(
                    "L'utilisateur " + userId + " n'est pas propriétaire du lieu " + place.getId());
        }
    }

    private void ensureMasterToken(Token caller) {
        if (!caller.isMasterToken()) {
            throw new UnauthorizedException("Cette opération nécessite le token maître du propriétaire");
        }
    }
}
