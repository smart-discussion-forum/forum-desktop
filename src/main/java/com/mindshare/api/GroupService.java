package com.mindshare.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindshare.auth.model.UserSession;
import com.mindshare.group.Group;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;

import java.io.IOException;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

// Handle fetching the logged-in user's groups from Laravel API.
public class GroupService {
    private static final String BASE_URL = ApiConfig.getBaseUrl();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public static class BrowseGroupsResult {
        public List<Group> myGroups;
        public List<Group> joinableGroups;
    }

    public List<Group> fetchMyGroups() throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(BASE_URL + "/groups");
            request.setHeader("Accept", "application/json");
            if (UserSession.getToken() != null && !UserSession.getToken().isBlank()) {
                request.setHeader("Authorization", "Bearer " + UserSession.getToken());
            }

            try (CloseableHttpResponse response = client.execute(request)) {
                String responseBody = response.getEntity() == null ? "" : new String(response.getEntity().getContent().readAllBytes());
                JsonNode root = objectMapper.readTree(responseBody);
                JsonNode groupsNode = root.isArray() ? root : root.path("groups");
                if (!groupsNode.isArray()) {
                    throw new IOException("Unexpected /groups response shape: " + responseBody);
                }

                return objectMapper.readValue(groupsNode.traverse(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Group.class));
            }
        }
    }

    public BrowseGroupsResult fetchBrowseGroups() throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(BASE_URL + "/groups/browse");
            request.setHeader("Accept", "application/json");
            if (UserSession.getToken() != null && !UserSession.getToken().isBlank()) {
                request.setHeader("Authorization", "Bearer " + UserSession.getToken());
            }
            try (CloseableHttpResponse response = client.execute(request)) {
                String responseBody = response.getEntity() == null ? "" : new String(response.getEntity().getContent().readAllBytes());
                System.out.println("Raw browse-groups response: " + responseBody);

                if (response.getCode() != 200) {
                    throw new IOException("Failed to load groups (HTTP " + response.getCode() + "): " + responseBody);
                }

                JsonNode root = objectMapper.readTree(responseBody);
                BrowseGroupsResult result = new BrowseGroupsResult();
                result.myGroups = objectMapper.readValue(root.path("myGroups").traverse(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Group.class));
                result.joinableGroups = objectMapper.readValue(root.path("joinableGroups").traverse(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Group.class));
                return result;
            }
        }
    }

    public void joinGroup(int groupId) throws IOException {
        postAction(groupId, "join");
    }

    public void leaveGroup(int groupId) throws IOException {
        postAction(groupId, "leave");
    }

    private void postAction(int groupId, String action) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(BASE_URL + "/groups/" + groupId + "/" + action);
            request.setHeader("Accept", "application/json");
            if (UserSession.getToken() != null && !UserSession.getToken().isBlank()) {
                request.setHeader("Authorization", "Bearer " + UserSession.getToken());
            }

            try (CloseableHttpResponse response = client.execute(request)) {
                String responseBody = response.getEntity() == null ? "" : new String(response.getEntity().getContent().readAllBytes());
                if (response.getCode() != 200) {
                    throw new IOException("Failed to " + action + " group (HTTP " + response.getCode() + "): " + responseBody);
                }
            }
        }
    }
}
