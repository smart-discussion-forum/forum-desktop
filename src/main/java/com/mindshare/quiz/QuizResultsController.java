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
        scoreLabel.setText("Score: " + score + "points");
        gradeLabel.setText(autoSubmitted ? "Time expired - auto-submitted" : "Submitted successfully");

        StringBuilder sb = new StringBuilder();
        JsonNode feedback = resultsJson.get("feedback");
        if (feedback != null) {
            for (JsonNode f : feedback) {
                sb.append(f.get("question").asText())
                        .append(" - ")
                        .append(f.get("is_correct").asBoolean() ? "Correct" : "Incorrect")
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
            stage.setScene(SceneUtils.createStyledScene(root, 600, 420));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
