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

public class MessageService {

    private static final String BASE_URL = ApiConfig.apiBaseUrl();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonNode fetchMessages(int groupId) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(BASE_URL + "/messages/group/" + groupId);
            request.setHeader("Accept", "application/json");
            request.setHeader("Authorization", "Bearer " + UserSession.getToken());

            try (CloseableHttpResponse response = client.execute(request)) {
                String responseBody = new String(response.getEntity().getContent().readAllBytes());
                return objectMapper.readTree(responseBody);
            }
        }
    }

    public JsonNode sendMessage(int groupId, String content) throws IOException {
        String body = objectMapper.writeValueAsString(new MessageRequest(groupId, content));

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(BASE_URL + "/messages/send");
            request.setHeader("Accept", "application/json");
            request.setHeader("Authorization", "Bearer " + UserSession.getToken());
            request.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse response = client.execute(request)) {
                String responseBody = new String(response.getEntity().getContent().readAllBytes());
                return objectMapper.readTree(responseBody);
            }
        }
    }

    private static class MessageRequest {
        public int group_id;
        public String content;

        public MessageRequest(int groupId, String content) {
            this.group_id = groupId;
            this.content = content;
        }
    }
}
