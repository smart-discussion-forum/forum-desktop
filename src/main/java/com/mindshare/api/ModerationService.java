package com.mindshare.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindshare.auth.model.UserSession;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.IOException;

public class ModerationService {

    private static final String BASE_URL = ApiConfig.getBaseUrl();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonNode fetchWarnings(Integer userId) throws IOException {
        String path = userId == null ? "/warnings" : "/warnings?user_id=" + userId;
        return get(path);
    }

    public JsonNode issueWarning(int userId, String reason) throws IOException {
        return post("/warnings", objectMapper.writeValueAsString(new WarningRequest(userId, reason)));
    }

    public JsonNode fetchBlacklist() throws IOException {
        return get("/blacklist");
    }

    public JsonNode liftBlacklist(int blacklistId) throws IOException {
        return post("/blacklist/" + blacklistId + "/lift", "{}");
    }

    private JsonNode get(String path) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(BASE_URL + path);
            request.setHeader("Accept", "application/json");
            if (UserSession.getToken() != null && !UserSession.getToken().isBlank()) {
                request.setHeader("Authorization", "Bearer " + UserSession.getToken());
            }
            try (CloseableHttpResponse response = client.execute(request)) {
                String body = response.getEntity() == null ? "" : new String(response.getEntity().getContent().readAllBytes());
                if (response.getCode() >= 400) {
                    throw new IOException(body.isBlank() ? "HTTP " + response.getCode() + " calling " + path : body);
                }
                return body.isBlank() ? objectMapper.createObjectNode() : objectMapper.readTree(body);
            }
        }
    }

    private JsonNode post(String path, String jsonBody) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(BASE_URL + path);
            request.setHeader("Accept", "application/json");
            if (UserSession.getToken() != null && !UserSession.getToken().isBlank()) {
                request.setHeader("Authorization", "Bearer " + UserSession.getToken());
            }
            request.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));
            try (CloseableHttpResponse response = client.execute(request)) {
                String body = response.getEntity() == null ? "" : new String(response.getEntity().getContent().readAllBytes());
                if (response.getCode() >= 400) {
                    throw new IOException(body.isBlank() ? "HTTP " + response.getCode() + " calling " + path : body);
                }
                return body.isBlank() ? objectMapper.createObjectNode() : objectMapper.readTree(body);
            }
        }
    }

    private static class WarningRequest {
        public int user_id;
        public String reason;

        WarningRequest(int userId, String reason) {
            this.user_id = userId;
            this.reason = reason;
        }
    }
}
