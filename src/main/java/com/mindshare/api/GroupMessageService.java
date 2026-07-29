package com.mindshare.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mindshare.auth.model.UserSession;
import com.mindshare.sync.NetworkMonitor;
import com.mindshare.sync.OfflineActionQueue;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.IOException;
import java.util.List;

public class GroupMessageService {
    private static final String BASE_URL = ApiConfig.getBaseUrl();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonNode fetchMessages(int groupId) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(BASE_URL + "/messages/group/" + groupId);
            request.setHeader("Accept", "application/json");
            if (UserSession.getToken() != null && !UserSession.getToken().isBlank()) {
                request.setHeader("Authorization", "Bearer " + UserSession.getToken());
            }
            try (CloseableHttpResponse response = client.execute(request)) {
                String body = response.getEntity() == null ? "" : new String(response.getEntity().getContent().readAllBytes());
                return objectMapper.readTree(body);
            }
        }
    }

    public JsonNode sendMessage(int groupId, String content, List<Integer> excludedUserIds) throws IOException {
        ObjectNode json = buildMessageBody(groupId, content, excludedUserIds);
        if (!NetworkMonitor.isServerReachable()) {
            OfflineActionQueue.enqueuePost("/messages/send", json.toString(), UserSession.getToken());
            ObjectNode queued = objectMapper.createObjectNode();
            queued.put("queued", true);
            queued.put("message", "Message saved and will be sent when you are back online.");
            return queued;
        }

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(BASE_URL + "/messages/send");
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Accept", "application/json");

            if (UserSession.getToken() != null && !UserSession.getToken().isBlank()) {
                request.setHeader("Authorization", "Bearer " + UserSession.getToken());
            }

            request.setEntity(new StringEntity(json.toString(), ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse response = client.execute(request)) {
                String responseBody = response.getEntity() == null ? "{}" : new String(response.getEntity().getContent().readAllBytes());
                if (response.getCode() < 200 || response.getCode() >= 300) {
                    throw new IOException("Failed to send message (HTTP " + response.getCode() + "): " + responseBody);
                }
                return objectMapper.readTree(responseBody);
            }
        }
    }

    private ObjectNode buildMessageBody(int groupId, String content, List<Integer> excludedUserIds) {
        ObjectNode json = objectMapper.createObjectNode();
        json.put("group_id", groupId);
        json.put("content", content);
        json.put("message", content);
        if (excludedUserIds != null && !excludedUserIds.isEmpty()) {
            ArrayNode excludedArray = json.putArray("excluded_user_ids");
            excludedUserIds.forEach(excludedArray::add);
        }
        return json;
    }
}
