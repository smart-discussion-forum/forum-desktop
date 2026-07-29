package com.mindshare.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindshare.auth.model.UserSession;
import com.mindshare.group.Group;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.IOException;
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

    public JsonNode fetchGroupMembers(int groupId) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(BASE_URL + "/groups/" + groupId + "/members");
            request.setHeader("Accept", "application/json");

            if (UserSession.getToken() != null && !UserSession.getToken().isBlank()) {
                request.setHeader("Authorization", "Bearer " + UserSession.getToken());
            }

            try (CloseableHttpResponse response = client.execute(request)) {
                String responseBody = response.getEntity() == null ? "" : new String(response.getEntity().getContent().readAllBytes());
                if (response.getCode() != 200) {
                    throw new IOException("Failed to fetch group members (HTTP " + response.getCode() + "): " + responseBody);
                }
                return objectMapper.readTree(responseBody);
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

    public Group createGroup(String name, String description) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(BASE_URL + "/groups");
            request.setHeader("Accept", "application/json");
            if (UserSession.getToken() != null && !UserSession.getToken().isBlank()) {
                request.setHeader("Authorization", "Bearer " + UserSession.getToken());
            }

            String body = objectMapper.writeValueAsString(new CreateGroupRequest(name, description));
            request.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse response = client.execute(request)) {
                String responseBody = response.getEntity() == null
                        ? ""
                        : new String(response.getEntity().getContent().readAllBytes());
                if (response.getCode() != 201 && response.getCode() != 200) {
                    throw new IOException(extractErrorMessage(responseBody, response.getCode()));
                }
                return objectMapper.readValue(responseBody, Group.class);
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

    private String extractErrorMessage(String responseBody, int statusCode) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            if (root.has("message") && !root.get("message").asText("").isBlank()) {
                return root.get("message").asText();
            }
            if (root.has("errors") && root.get("errors").isObject()) {
                var fields = root.get("errors").fields();
                if (fields.hasNext()) {
                    JsonNode first = fields.next().getValue();
                    if (first.isArray() && first.size() > 0) {
                        return first.get(0).asText();
                    }
                }
            }
        } catch (Exception ignored) {
            // fall through to generic message
        }
        return "Failed to create group (HTTP " + statusCode + ")";
    }

    private static class CreateGroupRequest {
        public String name;
        public String description;

        CreateGroupRequest(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }
}