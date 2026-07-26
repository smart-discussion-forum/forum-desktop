package com.mindshare.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindshare.auth.model.UserSession;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;

import java.io.IOException;

public class RecommendationServiceClient {

    private static final String BASE_URL = ApiConfig.getBaseUrl();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonNode fetchCombinedRecommendations() throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(BASE_URL + "/recommendations");
            request.setHeader("Accept", "application/json");
            if (UserSession.getToken() != null && !UserSession.getToken().isBlank()) {
                request.setHeader("Authorization", "Bearer " + UserSession.getToken());
            }

            try (CloseableHttpResponse response = client.execute(request)) {
                String body = response.getEntity() == null ? "" : new String(response.getEntity().getContent().readAllBytes());
                if (response.getCode() >= 400) {
                    throw new IOException("HTTP " + response.getCode() + " calling GET /recommendations"
                            + (body.isBlank() ? "" : ": " + body));
                }
                return body.isBlank() ? objectMapper.createObjectNode() : objectMapper.readTree(body);
            }
        }
    }
}
