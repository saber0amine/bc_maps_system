package org.example.bc_maps_system.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.bc_maps_system.model.ExternalSource;
import org.example.bc_maps_system.repository.ExternalSourceRepository;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AggregatorService {

    private final ExternalSourceRepository externalSourceRepository;
    private final RestTemplate restTemplate;

    public List<Map<String, Object>> aggregatePlaces(UUID userId) {
        List<ExternalSource> sources = externalSourceRepository.findByUserIdAndIsActiveTrue(userId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (ExternalSource source : sources) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(source.getToken());
                headers.setAccept(List.of(MediaType.APPLICATION_JSON));

                HttpEntity<Void> entity = new HttpEntity<>(headers);
                ResponseEntity<Map> response = restTemplate.exchange(
                        normalizeUrl(source.getServerUrl()) + "/api/places?page=0&size=200",
                        HttpMethod.GET,
                        entity,
                        Map.class
                );

                Object rawBody = response.getBody();
                List<?> rows;
                if (rawBody instanceof Map<?, ?> bodyMap && bodyMap.get("content") instanceof List<?> content) {
                    rows = content;
                } else if (rawBody instanceof List<?> directList) {
                    rows = directList;
                } else {
                    rows = List.of();
                }

                for (Object row : rows) {
                    if (row instanceof Map<?, ?> rawMap) {
                        Map<String, Object> map = new LinkedHashMap<>();
                        rawMap.forEach((key, value) -> map.put(String.valueOf(key), value));
                        map.putIfAbsent("sourceName", source.getName());
                        result.add(map);
                    }
                }

                source.setLastSync(LocalDateTime.now());
                source.setActive(true);
                externalSourceRepository.save(source);

            } catch (HttpClientErrorException.Unauthorized e) {
                source.setActive(false);
                externalSourceRepository.save(source);
                log.warn("Source {} désactivée après 401", source.getName());
            } catch (HttpClientErrorException.Forbidden e) {
                log.warn("Source {} refuse l'accès agrégé (403)", source.getName());
            } catch (Exception e) {
                log.warn("Agrégation temporairement impossible pour {}: {}", source.getName(), e.getMessage());
            }
        }

        return result;
    }

    private String normalizeUrl(String url) {
        return url == null ? "" : url.replaceAll("/$", "");
    }
}
