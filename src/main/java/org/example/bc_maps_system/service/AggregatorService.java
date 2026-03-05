package org.example.bc_maps_system.service;

import org.example.bc_maps_system.model.ExternalSource;
import org.example.bc_maps_system.repository.ExternalSourceRepository;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AggregatorService {

    private final ExternalSourceRepository externalSourceRepository;
    private final RestTemplate restTemplate;

    public AggregatorService(ExternalSourceRepository externalSourceRepository, RestTemplate restTemplate) {
        this.externalSourceRepository = externalSourceRepository;
        this.restTemplate = restTemplate;
    }

    public List<Map> aggregatePlaces(UUID userId) {
        List<ExternalSource> sources = externalSourceRepository.findByUserIdAndIsActiveTrue(userId);
        List<Map> result = new ArrayList<>();

        for (ExternalSource source : sources) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + source.getToken());
                headers.setAccept(List.of(MediaType.APPLICATION_JSON));

                HttpEntity<Void> entity = new HttpEntity<>(headers);
                ResponseEntity<List> response = restTemplate.exchange(
                        source.getServerUrl() + "/api/places",
                        HttpMethod.GET,
                        entity,
                        List.class
                );

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    result.addAll(response.getBody());
                    source.setLastSync(LocalDateTime.now());
                    externalSourceRepository.save(source);
                }

            } catch (HttpClientErrorException.Unauthorized e) {
                source.setActive(false);
                externalSourceRepository.save(source);
            } catch (HttpClientErrorException.Forbidden e) {
                // token valide mais accès refusé, on ne désactive pas la source
            } catch (Exception e) {
                // source temporairement injoignable, on continue
            }
        }

        return result;
    }
}