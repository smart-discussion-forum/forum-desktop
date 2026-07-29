package com.mindshare.quiz;

import com.fasterxml.jackson.databind.JsonNode;
import com.mindshare.api.QuizService;
import com.mindshare.utils.SceneUtils;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class QuizResultsController {
    @FXML private Label scoreLabel;
    @FXML private Label gradeLabel;
    @FXML private Label feedbackLabel;
    @FXML private TextArea breakdownArea;
    @FXML private Button doneButton;

    private final QuizService quizService = new QuizService();

    public void loadResults(int attemptId, boolean autoSubmitted) {
        scoreLabel.setText("Loading results...");
        Task<JsonNode> task = new Task<>() {
            @Override protected JsonNode call() throws Exception { return quizService.fetchResults(attemptId); }
        };
        task.setOnSucceeded(event -> setResults(task.getValue(), autoSubmitted));
        task.setOnFailed(event -> scoreLabel.setText("Could not load quiz results."));
        Thread thread = new Thread(task, "quiz-results-load");
        thread.setDaemon(true);
        thread.start();
    }

    public void setResults(JsonNode resultsJson, boolean autoSubmitted) {
        int score = resultsJson.path("score").asInt(0);
        int total = resultsJson.path("total_marks").asInt(0);
        double percentage = resultsJson.has("percentage") ? resultsJson.path("percentage").asDouble() : 0;
        String grade = resultsJson.path("grade").asText(gradeFor(percentage));
        scoreLabel.setText("Score: " + score + (total > 0 ? " / " + total : " points"));
        gradeLabel.setText("Grade: " + grade + (autoSubmitted || resultsJson.path("auto_submitted").asBoolean(false)
                ? " • Auto-submitted" : " • Submitted successfully"));
        feedbackLabel.setText(resultsJson.path("feedback_message").asText(
                percentage >= 60 ? "Well done! You have a solid understanding of this topic." : "Consider reviewing this topic further."));

        JsonNode feedback = resultsJson.path("feedback");
        StringBuilder breakdown = new StringBuilder("Question Breakdown\n\n");
        if (feedback.isArray()) {
            int i = 1;
            for (JsonNode row : feedback) {
                breakdown.append(i++).append(". ").append(row.path("question").asText("Unknown question"))
                        .append("\n   Your answer: ").append(row.path("your_answer").asText("No answer"))
                        .append("\n   Correct answer: ").append(row.path("correct_answer").asText("-"))
                        .append("\n   ").append(row.path("is_correct").asBoolean(false) ? "Correct" : "Incorrect")
                        .append("\n\n");
            }
        }
        breakdownArea.setText(breakdown.toString());
    }

    private String gradeFor(double percentage) {
        return percentage >= 80 ? "A" : percentage >= 60 ? "B" : percentage >= 40 ? "C" : "F";
    }

    @FXML private void handleDone(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/mindshare/quiz/QuizListView.fxml"));
            SceneUtils.switchScene((Stage) doneButton.getScene().getWindow(), root);
        } catch (Exception e) { e.printStackTrace(); }
    }
}
