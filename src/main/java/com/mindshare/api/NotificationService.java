package com.mindshare.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindshare.auth.model.UserSession;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;

import java.io.IOException;

/**
 * Backs the notification bell — talks to NotificationController's JSON API
 * (GET /notifications, POST /notifications/{id}/read, POST /notifications/read-all).
 * Note: notification ids are UUID strings, not ints.
 */
public class NotificationService {
    private static final String BASE_URL = ApiConfig.getBaseUrl();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** All notifications for the current user, newest first. */
    public JsonNode fetchNotifications() throws IOException {
        return get("/notifications");
    }

    public JsonNode markRead(String notificationId) throws IOException {
        return post("/notifications/" + notificationId + "/read");
    }

    public JsonNode markAllRead() throws IOException {
        return post("/notifications/read-all");
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

    private JsonNode post(String path) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(BASE_URL + path);
            addHeaders(request);
            try (CloseableHttpResponse response = client.execute(request)) {
                return read(response, path);
            }
        }
    }

    private void addHeaders(HttpUriRequest request) {
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
        return body.isBlank() ? objectMapper.createArrayNode() : objectMapper.readTree(body);
    }
}
