package com.mindshare.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindshare.auth.model.UserSession;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.IOException;

/**
 * Backs the admin "Manage Groups" screen — mirrors the webapp's
 * resources/views/groups/manage.blade.php (name, creator, members, topics,
 * edit, delete), consumed as JSON here for the desktop client.
 */
public class AdminGroupService {
    private static final String BASE_URL = ApiConfig.getBaseUrl();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** Same list the webapp's Manage Groups page shows (all groups, since only admins reach this screen). */
    public JsonNode fetchManagedGroups() throws IOException {
        return get("/groups/manage");
    }

    public JsonNode updateGroup(int groupId, String name, String description) throws IOException {
        return put("/groups/" + groupId, objectMapper.writeValueAsString(new UpdateRequest(name, description)));
    }

    public JsonNode deleteGroup(int groupId) throws IOException {
        return delete("/groups/" + groupId);
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

    private JsonNode put(String path, String body) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPut request = new HttpPut(BASE_URL + path);
            addHeaders(request);
            request.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
            try (CloseableHttpResponse response = client.execute(request)) {
                return read(response, path);
            }
        }
    }

    private JsonNode delete(String path) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpDelete request = new HttpDelete(BASE_URL + path);
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
        return body.isBlank() ? objectMapper.createObjectNode() : objectMapper.readTree(body);
    }

    private static class UpdateRequest {
        public final String name;
        public final String description;
        private UpdateRequest(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }
}
