package com.mindshare.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindshare.auth.model.UserSession;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.IOException;
import java.net.URISyntaxException;

public class QuizService {

    private static final String BASE_URL = ApiConfig.getBaseUrl();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonNode fetchQuestions(int quizId) throws IOException {
        return get("/quizzes/" + quizId + "/questions");
    }

    public JsonNode fetchQuizzes() throws IOException {
        return get("/quizzes");
    }

    //quiz attempt
    public JsonNode startAttempt(int quizId) throws IOException {
        return post("/quiz/" + quizId + "/attempt", "{}");
    }

    public JsonNode fetchQuizResults(int quizId) throws IOException {
        return get("/quiz/" + quizId + "/results");
    }

    public JsonNode submitAnswer(int attemptId, int questionId, String submittedAnswer) throws IOException {
        String body = objectMapper.writeValueAsString(new AnswerRequest(questionId, submittedAnswer));
        return post("/quiz/attempt/" + attemptId + "/answer", body);
    }

    public JsonNode submitFullAttempt(int attemptId) throws IOException {
        return post("/quiz/attempt/" + attemptId + "/submit", "{}");
    }

    public JsonNode fetchResults(int attemptId) throws IOException {
        return get("/quiz/attempt/" +attemptId +"/results");
    }


    private JsonNode get(String path) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(BASE_URL + path);
            request.setHeader("Accept", "application/json");
            if (UserSession.getToken() != null && !UserSession.getToken().isBlank()) {
                request.setHeader("Authorization", "Bearer " + UserSession.getToken());
            }
            try (CloseableHttpResponse response = client.execute(request)) {
                return readJsonOrThrow(response, request);
            }
        }
    }

    private JsonNode post(String path, String jsonBody) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(BASE_URL + path);
            request.setHeader("Accept", "application/json");
            if (UserSession.getToken() != null && !UserSession.getToken().isBlank()) {
                request.setHeader("Authorization", "Bearer " + UserSession.getToken());
            }
            request.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));
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

    private static class AnswerRequest {
        public int question_id;
        public String submitted_answer;
        AnswerRequest(int questionId, String submittedAnswer) {
            this.question_id = questionId;
            this.submitted_answer = submittedAnswer;
        }
    }

}
