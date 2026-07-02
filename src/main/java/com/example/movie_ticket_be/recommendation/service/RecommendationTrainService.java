package com.example.movie_ticket_be.recommendation.service;

import com.example.movie_ticket_be.recommendation.config.RecommendationProperties;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RecommendationTrainService {

    RecommendationProperties properties;
    RestTemplateBuilder restTemplateBuilder;

    /**
     * Gọi POST {pythonServiceUrl}/api/train để Python train CF và ghi
     * toàn bộ kết quả vào bảng user_preference.
     * Ném RuntimeException nếu Python service không phản hồi — caller tự quyết định log hay bỏ qua.
     */
    public Map<String, Object> triggerTrain() {
        RestTemplate restTemplate = restTemplateBuilder.build();
        String url = properties.getPythonServiceUrl() + "/api/train";

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                RequestEntity.post(URI.create(url)).build(),
                new ParameterizedTypeReference<>() {}
        );

        Map<String, Object> body = response.getBody();
        log.info("[RecommendationTrain] Python trả về: {}", body);
        return body != null ? body : Map.of();
    }
}
