package com.mindshare.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.IOException;


public class AuthService {
//Handles communication with the Laravel Sanctum authentication endpoint(Application layer/secure Laravel REST API)

    private static final String BASE_URL = ApiConfig.getBaseUrl();

    private final ObjectMapper objectMapper = new ObjectMapper();

    //Attempts login against POST /api/login and returns the parsed JSON response body regardless of success/failure.
    public JsonNode login(String email, String password) throws IOException {
        String requestBody = objectMapper.writeValueAsString(
                new LoginRequest(email, password)
        );

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(BASE_URL +"/login");
            request.setHeader("Accept", "application/json");
            request.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse response = client.execute(request)) {
                String responseBody = new String(response.getEntity().getContent().readAllBytes());
                System.out.println("Raw HTTP response: " + responseBody);
                return objectMapper.readTree(responseBody);
            }

        }
    }

    public JsonNode register(String name, String email, String password, String role, boolean acceptedTerms) throws IOException {
        String requestBody = objectMapper.writeValueAsString(
                new RegisterRequest(name, email, password, role, acceptedTerms)
        );

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(BASE_URL + "/register");
            request.setHeader("Accept", "application/json");
            request.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse response = client.execute(request)) {
                String responseBody = new String(response.getEntity().getContent().readAllBytes());
                System.out.println("Raw HTTP response: " + responseBody);
                return objectMapper.readTree(responseBody);
            }
        }
    }

//Internal helper class just to shape the JSON request body.
    private static class LoginRequest {
        public String email;
        public String password;

        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }

    private static class RegisterRequest {
        public String name;
        public String email;
        public String password;
        public String role;
        public boolean accepted_terms;

        public RegisterRequest(String name, String email, String password, String role, boolean acceptedTerms) {
            this.name = name;
            this.email = email;
            this.password = password;
            this.role = role;
            this.accepted_terms = acceptedTerms;
        }
    }
}
