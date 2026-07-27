package com.mindshare.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindshare.auth.model.UserSession;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;

import java.io.IOException;

public class AdminStatisticsService {
    private static final String BASE_URL = ApiConfig.getBaseUrl();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** Overview of every group with aggregate activity metrics. */
    public JsonNode fetchGroupStatistics() throws IOException {
        return get("/admin/statistics");
    }

    /** Detailed stats for a single group, including its topic breakdown. */
    public JsonNode fetchGroupDetail(int groupId) throws IOException {
        return get("/admin/statistics/" + groupId);
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
