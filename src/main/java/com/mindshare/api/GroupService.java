package com.mindshare.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindshare.auth.model.UserSession;
import com.mindshare.group.Group;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;

import java.io.IOException;
import java.util.List;

// Handle fetching the logged-in user's groups from Laravel API.
public class GroupService {
    private static final String BASE_URL = "http://127.0.0.1:8000/api";
    // Confirm exact path once backend team finalizes group routes
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Group> fetchMyGroups() throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(BASE_URL + "/groups");
            request.setHeader("Accept", "application/json");
            request.setHeader("Authorization", "Bearer " + UserSession.getToken());

            try (CloseableHttpResponse response = client.execute(request)) {
                String responseBody = new String(response.getEntity().getContent().readAllBytes());
                System.out.println("Raw groups response: " + responseBody);

                return objectMapper.readValue(responseBody,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Group.class));
            }

        }
    }
}