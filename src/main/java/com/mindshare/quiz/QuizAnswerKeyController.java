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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class QuizAnswerKeyController {
    @FXML private VBox contentBox;
    @FXML private Label titleLabel;
    @FXML private Label countLabel;
    @FXML private Label statusLabel;

    private final QuizService quizService = new QuizService();
    private QuizListItem quiz;

    public void setQuiz(QuizListItem quiz) {
        this.quiz = quiz;
        titleLabel.setText(quiz.getTitle() + " — Answer Key");
        load();
    }

    private void load() {
        Task<JsonNode> task = new Task<>() {
            @Override protected JsonNode call() throws Exception { return quizService.fetchAnswerKey(quiz.getId()); }
        };
        task.setOnSucceeded(event -> {
            JsonNode questions = task.getValue();
            if (questions.isObject()) questions = questions.path("questions");
            int count = questions.isArray() ? questions.size() : 0;
            countLabel.setText(count + " question(s)");
            statusLabel.setVisible(false);
            statusLabel.setManaged(false);
            if (questions.isArray()) {
                for (int i = 0; i < questions.size(); i++) addQuestion(i + 1, questions.get(i));
            }
        });
        task.setOnFailed(event -> statusLabel.setText("Could not load the answer key."));
        Thread thread = new Thread(task, "quiz-answer-key-load");
        thread.setDaemon(true);
        thread.start();
    }

    private void addQuestion(int number, JsonNode question) {
        VBox card = new VBox(7);
        card.getStyleClass().add("quiz-question-card");
        Label prompt = new Label(number + ". " + question.path("Question").asText("Question")
                + " (" + question.path("Marks").asInt(0) + " mark(s))");
        prompt.setWrapText(true);
        card.getChildren().add(prompt);
        JsonNode options = question.path("Options");
        if (options.isTextual()) {
            try { options = new com.fasterxml.jackson.databind.ObjectMapper().readTree(options.asText()); }
            catch (Exception ignored) { }
        }
        int correct = question.path("Correct_answer").asInt(-1);
        if (options.isArray()) {
            for (int i = 0; i < options.size(); i++) {
                Label option = new Label(options.get(i).asText() + (i == correct ? "  ✓ Correct answer" : ""));
                option.setWrapText(true);
                option.getStyleClass().add(i == correct ? "answer-key-correct" : "answer-key-option");
                card.getChildren().add(option);
            }
        }
        contentBox.getChildren().add(contentBox.getChildren().size() - 1, card);
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
