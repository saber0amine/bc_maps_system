package org.example.bc_maps_system.mapper;

import org.example.bc_maps_system.dto.PlaceRequest;
import org.example.bc_maps_system.dto.PlaceResponse;
import org.example.bc_maps_system.model.Place;
import org.example.bc_maps_system.model.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PlaceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true) // Map userId separately
    @Mapping(target = "isCurrentPosition", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "tags", ignore = true)
    Place toEntity(PlaceRequest request);

    @Mapping(target = "userId", expression = "java(place.getUser() != null ? place.getUser().getId().toString() : null)")
    @Mapping(target = "tags", expression = "java(mapTags(place.getTags()))")
    @Mapping(target = "currentPosition", source = "isCurrentPosition")
    PlaceResponse toResponse(Place place);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true) // Map userId separately
    @Mapping(target = "isCurrentPosition", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "tags", ignore = true)
    void updateEntity(PlaceRequest request, @MappingTarget Place place);

    default Set<String> mapTags(List<Tag> tags) {
        if (tags == null) return Set.of();
        return tags.stream().map(Tag::getName).collect(Collectors.toSet());
    }
}