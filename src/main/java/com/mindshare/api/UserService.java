package com.mindshare.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindshare.auth.model.UserSession;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;

import java.io.IOException;

//Fetches the authenticated user's profile from the Laravel Sanctum
public class UserService {
    private static final String BASE_URL = ApiConfig.getBaseUrl();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonNode fetchProfile() throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(BASE_URL + "/user");
            request.setHeader("Accept", "application/json");
            if (UserSession.getToken() != null && !UserSession.getToken().isBlank()) {
                request.setHeader("Authorization", "Bearer " + UserSession.getToken());
            }

            try (CloseableHttpResponse response = client.execute(request)) {
                String body = response.getEntity() == null
                        ? ""
                        : new String(response.getEntity().getContent().readAllBytes());
                if (body.isBlank()) {
                    throw new IOException("Empty response from /user (HTTP " + response.getCode() + ")");
                }
                return objectMapper.readTree(body);
            }
        }
    }

}
