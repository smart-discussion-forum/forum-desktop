package com.mindshare.quiz;

import com.fasterxml.jackson.databind.JsonNode;
import com.mindshare.api.QuizService;
import com.mindshare.auth.model.UserSession;
import com.mindshare.utils.SceneUtils;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class QuizOwnerController {
    @FXML private Label titleLabel;
    @FXML private Label detailsLabel;
    @FXML private Label questionCountLabel;
    @FXML private Label statusLabel;
    @FXML private Button editButton;

    private final QuizService quizService = new QuizService();
    private QuizListItem quiz;

    public void setQuiz(QuizListItem quiz) {
        this.quiz = quiz;
        titleLabel.setText(value(quiz.getTitle()));
        detailsLabel.setText(value(quiz.getGroupName()) + " • Starts " + value(quiz.getStartTimeDisplay())
                + " • Status: " + value(quiz.getStatus()));
        boolean canEdit = quiz.isOwner() && "upcoming".equalsIgnoreCase(quiz.getStatus());
        editButton.setVisible(canEdit);
        editButton.setManaged(canEdit);
        loadQuestionCount();
    }

    private void loadQuestionCount() {
        Task<JsonNode> task = new Task<>() {
            @Override protected JsonNode call() throws Exception { return quizService.fetchAnswerKey(quiz.getId()); }
        };
        task.setOnSucceeded(event -> {
            JsonNode node = task.getValue();
            if (node.isObject()) node = node.path("questions");
            questionCountLabel.setText((node.isArray() ? node.size() : 0) + " question(s)");
        });
        task.setOnFailed(event -> questionCountLabel.setText("Question count unavailable."));
        Thread thread = new Thread(task, "quiz-question-count");
        thread.setDaemon(true);
        thread.start();
    }

    @FXML private void handleBack(ActionEvent event) { navigate("/com/mindshare/quiz/QuizListView.fxml", (Button) event.getSource()); }

    @FXML private void handleEdit(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mindshare/quiz/QuizConfigurationView.fxml"));
            Parent root = loader.load();
            loader.<QuizConfigurationController>getController().setQuizForEdit(quiz);
            SceneUtils.switchScene((Stage) ((Button) event.getSource()).getScene().getWindow(), root);
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Could not open quiz editor.");
        }
    }

    @FXML private void handleSubmissions(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mindshare/quiz/QuizSubmissionsView.fxml"));
            Parent root = loader.load();
            loader.<QuizSubmissionsController>getController().setQuiz(quiz);
            SceneUtils.switchScene((Stage) ((Button) event.getSource()).getScene().getWindow(), root);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void handleAnswerKey(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mindshare/quiz/QuizAnswerKeyView.fxml"));
            Parent root = loader.load();
            loader.<QuizAnswerKeyController>getController().setQuiz(quiz);
            SceneUtils.switchScene((Stage) ((Button) event.getSource()).getScene().getWindow(), root);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void navigate(String path, Button source) {
        try { SceneUtils.switchScene((Stage) source.getScene().getWindow(), FXMLLoader.load(getClass().getResource(path))); }
        catch (Exception e) { e.printStackTrace(); }
    }
    private String value(String value) { return value == null || value.isBlank() ? "-" : value; }
}
