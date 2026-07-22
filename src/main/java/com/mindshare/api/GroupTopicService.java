package com.mindshare.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindshare.auth.model.UserSession;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.IOException;

public class GroupTopicService {
    private static final String BASE_URL = ApiConfig.getBaseUrl();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonNode fetchTopicsRaw(int groupId) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(BASE_URL + "/groups/" + groupId + "/topics");
            request.setHeader("Accept", "application/json");
            if (UserSession.getToken() != null && !UserSession.getToken().isBlank()) {
                request.setHeader("Authorization", "Bearer " + UserSession.getToken());
            }

            try (CloseableHttpResponse response = client.execute(request)) {
                String body = response.getEntity() == null ? "" : new String(response.getEntity().getContent().readAllBytes());
                return objectMapper.readTree(body);
            }
        }
    }

    public JsonNode createTopic(int groupId, String title, String category) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(BASE_URL + "/groups/" + groupId + "/topics");
            request.setHeader("Accept", "application/json");
            if (UserSession.getToken() != null && !UserSession.getToken().isBlank()) {
                request.setHeader("Authorization", "Bearer " + UserSession.getToken());
            }

            String payload = objectMapper.writeValueAsString(new TopicRequest(title, category));
            request.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse response = client.execute(request)) {
                String body = response.getEntity() == null ? "" : new String(response.getEntity().getContent().readAllBytes());
                if (response.getCode() >= 400) {
                    throw new IOException("HTTP " + response.getCode() + " calling POST /groups/" + groupId + "/topics"
                            + (body.isBlank() ? "" : ": " + body));
                }
                return body.isBlank() ? objectMapper.createObjectNode() : objectMapper.readTree(body);
            }
        }
    }

    private static class TopicRequest {
        public String title;
        public String category;

        TopicRequest(String title, String category) {
            this.title = title;
            this.category = category;
        }
    }
}
