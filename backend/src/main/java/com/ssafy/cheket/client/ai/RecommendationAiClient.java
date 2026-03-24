package com.ssafy.cheket.client.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.cheket.client.ai.dto.RecommendationRequestPayload;
import com.ssafy.cheket.client.ai.dto.RecommendationResponsePayload;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RecommendationAiClient {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build();

    @Value("${ai.server.base-url:http://127.0.0.1:8001}")
    private String baseUrl;

    public RecommendationResponsePayload recommend(RecommendationRequestPayload payload) {
        try {
            byte[] requestBody = objectMapper.writeValueAsBytes(payload);
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/v1/recommendations"))
                .timeout(REQUEST_TIMEOUT)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .POST(HttpRequest.BodyPublishers.ofByteArray(requestBody))
                .build();

            HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("AI recommendation request failed: " + response.statusCode());
            }

            return objectMapper.readValue(response.body(), RecommendationResponsePayload.class);
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException("AI recommendation request failed", e);
        }
    }
}
