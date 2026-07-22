package com.mindshare.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindshare.auth.model.UserSession;
import com.mindshare.discussion.model.Topic;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;

import java.io.IOException;
import java.util.List;


public class TopicService {

    private static final String BASE_URL = ApiConfig.getBaseUrl();

    private final ObjectMapper objectMapper = new ObjectMapper();

    //Fetches topics from particular groups
    public List<Topic> fetchTopics(int groupId) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(BASE_URL + "/groups/" + groupId + "/topics");
            request.setHeader("Accept", "application/json");
            request.setHeader("Authorization", "Bearer " + UserSession.getToken());

            try (CloseableHttpResponse response = client.execute(request)) {
                String responseBody = new String(response.getEntity().getContent().readAllBytes());
                System.out.println("Raw topics response: " + responseBody);

                return objectMapper.readValue(responseBody,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Topic.class));
            }
        }

    }
}
