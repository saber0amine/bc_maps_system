package org.example.bc_maps_system.specification;

import org.example.bc_maps_system.model.Place;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class PlaceSpecification {

    public static Specification<Place> hasUser(UUID userId) {
        return (root, query, cb) ->
                cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Place> isNotCurrentPosition() {
        return (root, query, cb) ->
                cb.isFalse(root.get("isCurrentPosition"));
    }

    public static Specification<Place> titleOrDescriptionContains(String keyword) {
        return (root, query, cb) -> {
            String like = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), like),
                    cb.like(cb.lower(root.get("description")), like)
            );
        };
    }

    public static Specification<Place> hasTag(String tag) {
        return (root, query, cb) ->
                cb.equal(
                        cb.lower(root.join("tags").get("name")),
                        tag.toLowerCase()
                );
    }
}