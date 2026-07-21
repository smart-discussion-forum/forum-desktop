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

public class QuizService {

    private static final String BASE_URL = "http://127.0.0.1:8000/api";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonNode fetchQuestions(int quizId) throws IOException {
        return get("/quizzes/" + quizId + "/questions");
    }

    //quiz attempt
    public JsonNode startAttempt(int quizId) throws IOException {
        return post("/quiz/" + quizId + "/attempt", "{}");
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
            request.setHeader("Authorization", "Bearer " + UserSession.getToken());
            try (CloseableHttpResponse response = client.execute(request)) {
                String body = new String(response.getEntity().getContent().readAllBytes());
                return objectMapper.readTree(body);
            }
        }
    }

    private JsonNode post(String path, String jsonBody) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(BASE_URL + path);
            request.setHeader("Accept", "application/json");
            request.setHeader("Authorization", "Bearer " + UserSession.getToken());
            request.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));
            try (CloseableHttpResponse response = client.execute(request)) {
                String body = new String(response.getEntity().getContent().readAllBytes());
                return objectMapper.readTree(body);
            }
        }
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