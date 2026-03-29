package org.example.bc_maps_system.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.bc_maps_system.model.Place;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class ExportService {

    private final ObjectMapper objectMapper;

    public ExportService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ExportPayload export(List<Place> places, String requestedFormat) {
        String format = normalizeFormat(requestedFormat);
        String body = switch (format) {
            case "gpx" -> exportGpx(places);
            case "kml" -> exportKml(places);
            default -> exportGeoJson(places);
        };

        MediaType mediaType = switch (format) {
            case "gpx" -> MediaType.parseMediaType("application/gpx+xml");
            case "kml" -> MediaType.parseMediaType("application/vnd.google-earth.kml+xml");
            default -> MediaType.parseMediaType("application/geo+json");
        };

        String filename = "places." + format;
        String etag = "\"" + DigestUtils.md5DigestAsHex(body.getBytes(StandardCharsets.UTF_8)) + "\"";
        long lastModified = places.stream()
                .map(place -> place.getUpdatedAt() != null ? place.getUpdatedAt() : place.getCreatedAt())
                .filter(Objects::nonNull)
                .map(date -> date.toInstant(ZoneOffset.UTC).toEpochMilli())
                .max(Long::compareTo)
                .orElse(Instant.now().toEpochMilli());

        return new ExportPayload(body, mediaType, filename, etag, lastModified);
    }

    public String normalizeFormat(String requestedFormat) {
        if (requestedFormat == null || requestedFormat.isBlank()) {
            return "geojson";
        }
        String normalized = requestedFormat.trim().toLowerCase();
        if (normalized.equals("json") || normalized.equals("geojson") || normalized.equals("application/geo+json")) return "geojson";
        if (normalized.equals("gpx") || normalized.equals("application/gpx+xml")) return "gpx";
        if (normalized.equals("kml") || normalized.equals("application/vnd.google-earth.kml+xml")) return "kml";
        throw new IllegalArgumentException("Format d'export non supporté : " + requestedFormat);
    }

    private String exportGpx(List<Place> places) {
        StringBuilder builder = new StringBuilder();
        builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        builder.append("<gpx version=\"1.1\" creator=\"bc_maps_system\" xmlns=\"http://www.topografix.com/GPX/1/1\">\n");
        for (Place place : places) {
            builder.append("  <wpt lat=\"").append(place.getLatitude()).append("\" lon=\"").append(place.getLongitude()).append("\">\n");
            builder.append("    <name>").append(escapeXml(place.getTitle())).append("</name>\n");
            if (place.getDescription() != null && !place.getDescription().isBlank()) {
                builder.append("    <desc>").append(escapeXml(place.getDescription())).append("</desc>\n");
            }
            builder.append("  </wpt>\n");
        }
        builder.append("</gpx>\n");
        return builder.toString();
    }

    private String exportKml(List<Place> places) {
        StringBuilder builder = new StringBuilder();
        builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        builder.append("<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n<Document>\n");
        for (Place place : places) {
            builder.append("  <Placemark>\n");
            builder.append("    <name>").append(escapeXml(place.getTitle())).append("</name>\n");
            if (place.getDescription() != null && !place.getDescription().isBlank()) {
                builder.append("    <description>").append(escapeXml(place.getDescription())).append("</description>\n");
            }
            builder.append("    <Point><coordinates>")
                    .append(place.getLongitude())
                    .append(',')
                    .append(place.getLatitude())
                    .append("</coordinates></Point>\n");
            builder.append("  </Placemark>\n");
        }
        builder.append("</Document>\n</kml>\n");
        return builder.toString();
    }

    private String exportGeoJson(List<Place> places) {
        Map<String, Object> featureCollection = new HashMap<>();
        featureCollection.put("type", "FeatureCollection");
        featureCollection.put("features", places.stream().map(place -> {
            Map<String, Object> feature = new HashMap<>();
            feature.put("type", "Feature");

            Map<String, Object> geometry = new HashMap<>();
            geometry.put("type", "Point");
            geometry.put("coordinates", List.of(place.getLongitude(), place.getLatitude()));
            feature.put("geometry", geometry);

            Map<String, Object> properties = new HashMap<>();
            properties.put("id", place.getId());
            properties.put("title", place.getTitle());
            properties.put("description", place.getDescription());
            properties.put("imageUrl", place.getImageUrl());
            properties.put("tags", place.getTags().stream().map(tag -> tag.getName()).toList());
            feature.put("properties", properties);
            return feature;
        }).toList());

        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(featureCollection);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Impossible de générer le GeoJSON", e);
        }
    }

    private String escapeXml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    public record ExportPayload(String body, MediaType mediaType, String filename, String etag, long lastModified) {}
}
