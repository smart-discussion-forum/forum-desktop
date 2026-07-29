package com.mindshare.quiz;

import com.fasterxml.jackson.databind.JsonNode;
import com.mindshare.api.QuizService;
import com.mindshare.utils.SceneUtils;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.util.Duration;

public class QuizSubmissionsController {
    @FXML private Label titleLabel;
    @FXML private Label countLabel;
    @FXML private Label statusLabel;
    @FXML private TableView<JsonNode> submissionsTable;
    @FXML private TableColumn<JsonNode, String> studentColumn;
    @FXML private TableColumn<JsonNode, String> scoreColumn;
    @FXML private TableColumn<JsonNode, String> typeColumn;

    private final QuizService quizService = new QuizService();
    private QuizListItem quiz;
    private Timeline poller;

    @FXML public void initialize() {
        studentColumn.setCellValueFactory(data -> text(data.getValue().path("student_name").asText("Unknown")));
        scoreColumn.setCellValueFactory(data -> text(data.getValue().path("Score").asText("0") + " / " + data.getValue().path("total_marks").asText("-")));
        typeColumn.setCellValueFactory(data -> text(data.getValue().path("Auto_submitted").asBoolean(false) ? "Auto-submitted" : "Submitted"));
    }

    public void setQuiz(QuizListItem quiz) {
        this.quiz = quiz;
        titleLabel.setText(quiz.getTitle() + " — Submissions");
        load();
        poller = new Timeline(new KeyFrame(Duration.seconds(5), event -> load()));
        poller.setCycleCount(Timeline.INDEFINITE);
        poller.play();
    }

    private ReadOnlyStringWrapper text(String value) { return new ReadOnlyStringWrapper(value); }

    private void load() {
        Task<JsonNode> task = new Task<>() {
            @Override protected JsonNode call() throws Exception { return quizService.fetchLecturerResults(quiz.getId()); }
        };
        task.setOnSucceeded(event -> {
            JsonNode results = task.getValue().path("results");
            java.util.List<JsonNode> rows = new java.util.ArrayList<>();
            if (results.isArray()) results.forEach(rows::add);
            submissionsTable.setItems(FXCollections.observableArrayList(rows));
            countLabel.setText("Student Submissions (" + (results.isArray() ? results.size() : 0) + ")");
            statusLabel.setText("");
        });
        task.setOnFailed(event -> statusLabel.setText("Could not load submissions."));
        Thread thread = new Thread(task, "quiz-submissions-load");
        thread.setDaemon(true);
        thread.start();
    }

    @FXML private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mindshare/quiz/QuizOwnerView.fxml"));
            Parent root = loader.load();
            loader.<QuizOwnerController>getController().setQuiz(quiz);
            SceneUtils.switchScene((Stage) ((Button) event.getSource()).getScene().getWindow(), root);
        } catch (Exception e) { e.printStackTrace(); }
    }
}
