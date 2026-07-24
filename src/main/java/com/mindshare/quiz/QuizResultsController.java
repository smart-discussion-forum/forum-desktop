package com.mindshare.quiz;

import com.fasterxml.jackson.databind.JsonNode;
import com.mindshare.utils.SceneUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class QuizResultsController {
    @FXML private Label scoreLabel;
    @FXML private Label gradeLabel;
    @FXML private Label feedbackLabel;
    @FXML private Button doneButton;

    public void setResults(JsonNode resultsJson, boolean autoSubmitted) {
        int score = resultsJson.has("score") ? resultsJson.get("score").asInt() : 0;
        scoreLabel.setText("Score: " + score + " points");
        boolean backendAutoSubmitted = resultsJson.has("auto_submitted") && resultsJson.get("auto_submitted").asBoolean();
        gradeLabel.setText((autoSubmitted || backendAutoSubmitted) ? "Time expired - auto-submitted" : "Submitted successfully");

        StringBuilder sb = new StringBuilder();
        JsonNode feedback = resultsJson.path("feedback");
        if ((feedback.isMissingNode() || !feedback.isArray()) && resultsJson.has("breakdown")) {
            feedback = resultsJson.get("breakdown");
        }
        if (feedback != null) {
            for (JsonNode f : feedback) {
                JsonNode questionNode = f.get("question");
                if (questionNode == null) {
                    questionNode = f.get("Question");
                }
                JsonNode correctNode = f.get("is_correct");
                if (correctNode == null) {
                    correctNode = f.get("correct");
                }
                sb.append(questionNode != null ? questionNode.asText() : "Unknown question")
                        .append(" - ")
                        .append(correctNode != null && correctNode.asBoolean() ? "Correct" : "Incorrect")
                        .append("\n");
            }
        }
        feedbackLabel.setText(sb.toString());
    }

    @FXML
    private void handleDone(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mindshare/dashboard/DashboardView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) doneButton.getScene().getWindow();
            SceneUtils.switchScene(stage, root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
