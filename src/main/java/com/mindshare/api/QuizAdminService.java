package com.mindshare.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindshare.auth.model.UserSession;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.IOException;
import java.util.List;
import java.net.URISyntaxException;

public class QuizAdminService {

    private static final String BASE_URL = ApiConfig.getBaseUrl();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonNode createQuiz(CreateQuizRequest requestPayload) throws IOException {
        String requestBody = objectMapper.writeValueAsString(requestPayload);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(BASE_URL + "/quizzes");
            request.setHeader("Accept", "application/json");
            if (UserSession.getToken() != null && !UserSession.getToken().isBlank()) {
                request.setHeader("Authorization", "Bearer " + UserSession.getToken());
            }
            request.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse response = client.execute(request)) {
                return readJsonOrThrow(response, request);
            }
        }
    }

    private JsonNode readJsonOrThrow(CloseableHttpResponse response, HttpUriRequestBase request) throws IOException {
        String body = response.getEntity() == null ? "" : new String(response.getEntity().getContent().readAllBytes());
        int statusCode = response.getCode();
        if (statusCode >= 400) {
            String uriString;
            try {
                uriString = request.getUri().toString();
            } catch (URISyntaxException e) {
                uriString = request.getRequestUri(); // raw path as stored, no parsing/validation
            }

            throw new IOException("HTTP " + statusCode + " calling " + request.getMethod() + " " + uriString
                    + (body.isBlank() ? "" : ": " + body));
        }
        if (body.isBlank()) {
            return objectMapper.createObjectNode();
        }
        return objectMapper.readTree(body);
    }

    public static class CreateQuizRequest {
        public String title;
        public Integer group_id;
        public String start_time;
        public Integer duration_minutes;
        public List<QuestionPayload> questions;

        public CreateQuizRequest(String title, Integer groupId, String startTime, Integer durationMinutes, List<QuestionPayload> questions) {
            this.title = title;
            this.group_id = groupId;
            this.start_time = startTime;
            this.duration_minutes = durationMinutes;
            this.questions = questions;
        }
    }

    public static class QuestionPayload {
        public String question;
        public List<String> options;
        public Integer correct_option;
        public Integer marks;

        public QuestionPayload(String question, List<String> options, Integer correctOption, Integer marks) {
            this.question = question;
            this.options = options;
            this.correct_option = correctOption;
            this.marks = marks;
        }
    }

    public static class QuizCreationResult {
        private final boolean success;
        private final String message;
        private final JsonNode quiz;

        public QuizCreationResult(boolean success, String message, JsonNode quiz) {
            this.success = success;
            this.message = message;
            this.quiz = quiz;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public JsonNode getQuiz() {
            return quiz;
        }
    }
}
