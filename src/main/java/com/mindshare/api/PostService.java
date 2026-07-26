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

public class PostService {

    private static final String BASE_URL = ApiConfig.getBaseUrl();

    private final ObjectMapper objectMapper = new ObjectMapper();

    //returns a JSON array of posts
    public JsonNode fetchPosts(int topicId) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(BASE_URL + "/topics/" + topicId + "/posts");
            request.setHeader("Accept", "application/json");
            request.setHeader("Authorization", "Bearer " + UserSession.getToken());

            try (CloseableHttpResponse response = client.execute(request)) {
                String responseBody = new String(response.getEntity().getContent().readAllBytes());
                return objectMapper.readTree(responseBody);
            }
        }
    }

    //Returns the body
    public JsonNode createPost(int topicId, String content) throws IOException {
        String requestBody = objectMapper.writeValueAsString(new PostRequest(content));

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(BASE_URL + "/topics/" + topicId + "/posts");
            request.setHeader("Accept", "application/json");
            request.setHeader("Authorization", "Bearer " + UserSession.getToken());
            request.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse response = client.execute(request)) {
                String responseBody = new String(response.getEntity().getContent().readAllBytes());
                return objectMapper.readTree(responseBody);
            }
        }
    }

    private static class PostRequest {
        public String content;
        public PostRequest(String content) { this.content = content; }
    }
}
