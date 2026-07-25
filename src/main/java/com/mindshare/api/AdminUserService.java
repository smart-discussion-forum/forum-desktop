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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class AdminUserService {
    private static final String BASE_URL = ApiConfig.getBaseUrl();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonNode fetchUsers(String filter, String search) throws IOException {
        String path = "/admin/users?filter=" + encode(filter) + "&search=" + encode(search);
        return get(path);
    }

    public JsonNode runInactivityCheck() throws IOException {
        return post("/admin/users/run-inactivity-check", "{}");
    }

    public JsonNode warn(int userId, String reason) throws IOException {
        return post("/admin/users/" + userId + "/warn", objectMapper.writeValueAsString(new ReasonRequest(reason)));
    }

    public JsonNode blacklist(int userId, String reason, Integer durationDays) throws IOException {
        return post("/admin/users/" + userId + "/blacklist",
                objectMapper.writeValueAsString(new BlacklistRequest(reason, durationDays)));
    }

    public JsonNode reinstate(int userId) throws IOException {
        return post("/admin/users/" + userId + "/reinstate", "{}");
    }

    private JsonNode get(String path) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(BASE_URL + path);
            addHeaders(request);
            try (CloseableHttpResponse response = client.execute(request)) {
                return read(response, path);
            }
        }
    }

    private JsonNode post(String path, String body) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(BASE_URL + path);
            addHeaders(request);
            request.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
            try (CloseableHttpResponse response = client.execute(request)) {
                return read(response, path);
            }
        }
    }

    private void addHeaders(org.apache.hc.client5.http.classic.methods.HttpUriRequest request) {
        request.setHeader("Accept", "application/json");
        request.setHeader("Content-Type", "application/json");
        if (UserSession.getToken() != null && !UserSession.getToken().isBlank()) {
            request.setHeader("Authorization", "Bearer " + UserSession.getToken());
        }
    }

    private JsonNode read(CloseableHttpResponse response, String path) throws IOException {
        String body = response.getEntity() == null ? "" :
                new String(response.getEntity().getContent().readAllBytes());
        if (response.getCode() >= 400) {
            throw new IOException(body.isBlank() ? "HTTP " + response.getCode() + " calling " + path : body);
        }
        return body.isBlank() ? objectMapper.createObjectNode() : objectMapper.readTree(body);
    }

    private String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private static class ReasonRequest {
        public final String reason;
        private ReasonRequest(String reason) { this.reason = reason; }
    }

    private static class BlacklistRequest {
        public final String reason;
        public final Integer duration_days;
        private BlacklistRequest(String reason, Integer durationDays) {
            this.reason = reason;
            this.duration_days = durationDays;
        }
    }
}
