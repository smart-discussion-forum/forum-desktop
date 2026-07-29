package com.mindshare.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindshare.auth.model.UserSession;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Lecturer-facing "Student Participation" feature — distinct from
 * AdminStatisticsService's overall group-activity screen. Backs the
 * group picker (groups the lecturer created/manages) and the per-student
 * participation leaderboard for a chosen group.
 */
public class LecturerParticipationService {
    private static final String BASE_URL = ApiConfig.getBaseUrl();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** Groups this lecturer manages (or all groups, if Admin). */
    public JsonNode fetchManagedGroups() throws IOException {
        return get("/groups/managed-by-me");
    }

    /** Per-student participation marks for a group, sorted per the given field/order. */
    public JsonNode fetchParticipation(int groupId, String sortBy, String sortOrder) throws IOException {
        String query = "?sort_by=" + encode(sortBy) + "&sort_order=" + encode(sortOrder);
        return get("/groups/" + groupId + "/statistics" + query);
    }

    private String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private JsonNode get(String path) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(BASE_URL + path);
            request.setHeader("Accept", "application/json");
            if (UserSession.getToken() != null && !UserSession.getToken().isBlank()) {
                request.setHeader("Authorization", "Bearer " + UserSession.getToken());
            }
            try (CloseableHttpResponse response = client.execute(request)) {
                String body = response.getEntity() == null ? "" :
                        new String(response.getEntity().getContent().readAllBytes());
                if (response.getCode() >= 400) {
                    throw new IOException(body.isBlank() ? "HTTP " + response.getCode() + " calling " + path : body);
                }
                return body.isBlank() ? objectMapper.createObjectNode() : objectMapper.readTree(body);
            }
        }
    }
}
