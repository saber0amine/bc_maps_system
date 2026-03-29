package org.example.bc_maps_system.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.bc_maps_system.dto.PlaceRequest;
import org.example.bc_maps_system.model.Token;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ImportService {

    private final PlaceService placeService;
    private final ObjectMapper objectMapper;

    public ImportService(PlaceService placeService, ObjectMapper objectMapper) {
        this.placeService = placeService;
        this.objectMapper = objectMapper;
    }

    public int importPlaces(Token caller, MultipartFile file, String defaultTag) {
        String filename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        try {
            byte[] content = file.getBytes();
            List<PlaceRequest> places;
            if (filename.endsWith(".geojson") || filename.endsWith(".json")) {
                places = parseGeoJson(content, defaultTag);
            } else if (filename.endsWith(".gpx")) {
                places = parseGpx(content, defaultTag);
            } else if (filename.endsWith(".kml")) {
                places = parseKml(content, defaultTag);
            } else {
                throw new IllegalArgumentException("Format d'import non supporté. Utilise .geojson, .gpx ou .kml");
            }
            places.forEach(place -> placeService.create(place, caller));
            return places.size();
        } catch (Exception e) {
            throw new IllegalArgumentException("Import impossible : " + e.getMessage(), e);
        }
    }

    private List<PlaceRequest> parseGeoJson(byte[] bytes, String defaultTag) throws Exception {
        JsonNode root = objectMapper.readTree(bytes);
        List<PlaceRequest> requests = new ArrayList<>();
        JsonNode features = root.path("features");
        if (!features.isArray()) {
            return requests;
        }
        for (JsonNode feature : features) {
            JsonNode coordinates = feature.path("geometry").path("coordinates");
            if (!coordinates.isArray() || coordinates.size() < 2) continue;
            JsonNode properties = feature.path("properties");
            PlaceRequest request = new PlaceRequest();
            request.setTitle(textOrDefault(properties.path("title"), "Lieu importé"));
            request.setDescription(textOrDefault(properties.path("description"), ""));
            request.setLongitude(coordinates.get(0).asDouble());
            request.setLatitude(coordinates.get(1).asDouble());
            request.setImageUrl(textOrNull(properties.path("imageUrl")));
            request.setTags(extractTags(properties.path("tags"), defaultTag));
            requests.add(request);
        }
        return requests;
    }

    private List<PlaceRequest> parseGpx(byte[] bytes, String defaultTag) throws Exception {
        Document doc = xml(bytes);
        NodeList nodes = doc.getElementsByTagName("wpt");
        List<PlaceRequest> requests = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            Element element = (Element) nodes.item(i);
            PlaceRequest request = new PlaceRequest();
            request.setLatitude(Double.parseDouble(element.getAttribute("lat")));
            request.setLongitude(Double.parseDouble(element.getAttribute("lon")));
            request.setTitle(textOrDefault(firstText(element, "name"), "Lieu importé"));
            request.setDescription(textOrDefault(firstText(element, "desc"), ""));
            request.setTags(defaultTag == null || defaultTag.isBlank() ? Set.of() : Set.of(defaultTag));
            requests.add(request);
        }
        return requests;
    }

    private List<PlaceRequest> parseKml(byte[] bytes, String defaultTag) throws Exception {
        Document doc = xml(bytes);
        NodeList placemarks = doc.getElementsByTagName("Placemark");
        List<PlaceRequest> requests = new ArrayList<>();
        for (int i = 0; i < placemarks.getLength(); i++) {
            Element element = (Element) placemarks.item(i);
            String coordinates = firstText(element, "coordinates");
            if (coordinates == null || coordinates.isBlank()) continue;
            String[] chunks = coordinates.trim().split(",");
            if (chunks.length < 2) continue;
            PlaceRequest request = new PlaceRequest();
            request.setLongitude(Double.parseDouble(chunks[0].trim()));
            request.setLatitude(Double.parseDouble(chunks[1].trim()));
            request.setTitle(textOrDefault(firstText(element, "name"), "Lieu importé"));
            request.setDescription(textOrDefault(firstText(element, "description"), ""));
            request.setTags(defaultTag == null || defaultTag.isBlank() ? Set.of() : Set.of(defaultTag));
            requests.add(request);
        }
        return requests;
    }

    private Document xml(byte[] bytes) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        return factory.newDocumentBuilder().parse(new ByteArrayInputStream(bytes));
    }

    private String firstText(Element element, String tagName) {
        NodeList list = element.getElementsByTagName(tagName);
        if (list.getLength() == 0) return null;
        return list.item(0).getTextContent();
    }

    private String textOrDefault(JsonNode node, String fallback) {
        String value = textOrNull(node);
        return value == null || value.isBlank() ? fallback : value;
    }

    private String textOrDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String textOrNull(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) return null;
        String value = node.asText();
        return value == null || value.isBlank() ? null : value;
    }

    private Set<String> extractTags(JsonNode node, String defaultTag) {
        Set<String> tags = new HashSet<>();
        if (node != null && node.isArray()) {
            node.forEach(item -> {
                if (item != null && !item.asText().isBlank()) {
                    tags.add(item.asText().trim());
                }
            });
        }
        if ((defaultTag != null) && !defaultTag.isBlank()) {
            tags.add(defaultTag.trim());
        }
        return tags;
    }
}
