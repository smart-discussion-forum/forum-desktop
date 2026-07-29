package com.mindshare.quiz;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindshare.api.QuizService;
import com.mindshare.utils.SceneUtils;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;

public class QuizTakingController {
    @FXML private Label timerLabel;
    @FXML private Label questionLabel;
    @FXML private VBox optionsBox;
    @FXML private Button submitButton;

    private Quiz quiz;
    private List<QuizQuestion> questions;
    private int currentIndex = 0;
    private ToggleGroup optionsGroup;
    private Timeline timer;
    private int secondsRemaining;
    private int attemptId = -1;
    private boolean finished;
    private boolean closeHandlerInstalled;

    private final QuizService quizService = new QuizService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
        if (questionLabel != null) {
            questionLabel.setText("Starting quiz...");
        }
        installCloseWarning();
        startAttempt();
    }

    private void installCloseWarning() {
        if (closeHandlerInstalled) return;
        closeHandlerInstalled = true;
        submitButton.sceneProperty().addListener((observable, oldScene, scene) -> {
            if (scene == null) return;
            installCloseWarning(scene.getWindow());
            scene.windowProperty().addListener((windowObservable, oldWindow, window) -> installCloseWarning(window));
        });
        if (submitButton.getScene() != null) {
            installCloseWarning(submitButton.getScene().getWindow());
        }
    }

    private void installCloseWarning(javafx.stage.Window window) {
        if (window == null) return;
        window.setOnCloseRequest(event -> {
                if (finished || timer == null || !timer.getStatus().equals(Timeline.Status.RUNNING)) return;
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                        "Leaving now will not submit your current answers. Continue?", ButtonType.CANCEL, ButtonType.OK);
                alert.setTitle("Leave quiz?");
                ButtonType choice = alert.showAndWait().orElse(ButtonType.CANCEL);
                if (!ButtonType.OK.equals(choice)) event.consume();
            });
    }

    private void startAttempt() {
        Task<JsonNode> task = new Task<>() {
            @Override
            protected JsonNode call() throws Exception {
                return quizService.startAttempt(quiz.getId());
            }
        };

        task.setOnSucceeded(e -> {
            JsonNode result = task.getValue();
            System.out.println("Raw startAttempt response: " + result);

            JsonNode attemptNode = result.path("attempt");
            JsonNode attemptIdNode = attemptNode.path("Attempt_id");
            if (attemptIdNode.isMissingNode() || attemptIdNode.isNull()) {
                attemptIdNode = attemptNode.path("attempt_id");
            }
            if (attemptIdNode.isMissingNode() || attemptIdNode.isNull()) {
                attemptIdNode = attemptNode.path("id");
            }
            if (attemptIdNode.isMissingNode() || attemptIdNode.isNull()) {
                attemptIdNode = result.path("Attempt_id");
            }
            if (attemptIdNode.isMissingNode() || attemptIdNode.isNull()) {
                attemptIdNode = result.path("attempt_id");
            }

            if (!attemptIdNode.isMissingNode() && !attemptIdNode.isNull()) {
                attemptId = attemptIdNode.asInt();
                loadQuestions();
            } else {
                questionLabel.setText(result.has("message") ? result.get("message").asText() : "Could not start quiz.");
            }
        });

        task.setOnFailed(e -> {
            Throwable error = task.getException();
            String message = error == null ? "" : error.getMessage();
            if (message != null && (message.contains("already closed") || message.contains("not yet available")
                    || message.contains("HTTP 403"))) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION,
                        "This quiz has closed; no attempt was recorded.");
                alert.setTitle("Quiz unavailable");
                alert.setHeaderText(null);
                alert.showAndWait();
                try {
                    Parent list = FXMLLoader.load(getClass().getResource("/com/mindshare/quiz/QuizListView.fxml"));
                    SceneUtils.switchScene((Stage) submitButton.getScene().getWindow(), list);
                } catch (Exception navigationError) {
                    navigationError.printStackTrace();
                }
            } else {
                task.getException().printStackTrace();
                questionLabel.setText("Error starting quiz attempt.");
            }
        });

        new Thread(task).start();
    }

    private void loadQuestions() {
        Task<List<QuizQuestion>> task = new Task<>() {
            @Override
            protected List<QuizQuestion> call() throws Exception {
                JsonNode raw = quizService.fetchQuestions(quiz.getId());
                System.out.println("Raw questions response: " + raw);
                JsonNode questionsNode = raw.isArray() ? raw : raw.path("questions");
                if (!questionsNode.isArray()) {
                    questionsNode = raw.path("data");
                }
                if (!questionsNode.isArray()) {
                    questionsNode = raw;
                }
                return objectMapper.readValue(questionsNode.traverse(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, QuizQuestion.class));
            }
        };

        task.setOnSucceeded(e -> {
            questions = task.getValue();
            if (questions == null || questions.isEmpty()) {
                questionLabel.setText("No questions found for this quiz.");
                return;
            }
            if (quiz.hasDuration()) {
                secondsRemaining = quiz.getRemainingSeconds();
                startTimer();
            } else {
                timerLabel.setText("Quiz in progress");
            }
            showQuestion();
        });

        task.setOnFailed(e -> {
            task.getException().printStackTrace();
            questionLabel.setText("Error loading questions.");
        });

        new Thread(task).start();
    }


    private void startTimer() {
        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            secondsRemaining--;
            timerLabel.setText("Time left: " + (secondsRemaining / 60) + ":" + String.format("%02d", secondsRemaining % 60));
            if (secondsRemaining <= 0) {
                timer.stop();
                finalizeAttempt(true);
            }
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    private void showQuestion() {
        if (questions == null || questions.isEmpty() || currentIndex >= questions.size()) {
            return;
        }
        QuizQuestion q = questions.get(currentIndex);
        questionLabel.setText(q.getQuestion());
        optionsBox.getChildren().clear();
        optionsGroup = new ToggleGroup();

        String[] options = q.getOptions();
        for (int optionIndex = 0; optionIndex < options.length; optionIndex++) {
            RadioButton rb = new RadioButton(options[optionIndex]);
            rb.setToggleGroup(optionsGroup);
            rb.setUserData(optionIndex);
            optionsBox.getChildren().add(rb);
        }
    }

    @FXML
    private void handleSubmit(ActionEvent event) {
        if (questions == null || questions.isEmpty() || currentIndex >= questions.size()) {
            return;
        }
        Toggle selected = optionsGroup.getSelectedToggle();
        String selectedIndex = selected != null ? String.valueOf((Integer) selected.getUserData()) : "";
        QuizQuestion currentQuestion = questions.get(currentIndex);

        Task<JsonNode> task = new Task<>() {
            @Override
            protected JsonNode call() throws Exception {
                return quizService.submitAnswer(attemptId, currentQuestion.getQuestionId(), selectedIndex);
            }
        };

        task.setOnSucceeded(e -> {
            System.out.println("Raw submitAnswer response: " + task.getValue());
            advanceOrFinish();
        });

        task.setOnFailed(e -> {
            task.getException().printStackTrace();
            advanceOrFinish(); // don't get stuck if one answer fails to save
        });
        new Thread(task).start();
    }

    private void advanceOrFinish() {
        currentIndex++;
        if (currentIndex < questions.size()) {
            showQuestion();
        } else {
            if (timer != null) timer.stop();
            finalizeAttempt(false);
        }
    }

    private void finalizeAttempt(boolean autoSubmitted) {
        Task<JsonNode> task = new Task<>() {
            @Override
            protected JsonNode call() throws Exception {
                quizService.submitFullAttempt(attemptId, autoSubmitted);
                return quizService.fetchResults(attemptId);
            }
        };

        task.setOnSucceeded(e -> {
            System.out.println("Raw results response: " + task.getValue());
            goToResults(task.getValue(), autoSubmitted);
        });

        task.setOnFailed(e -> task.getException().printStackTrace());

        new Thread(task).start();
    }


    private void goToResults(JsonNode resultsJson, boolean autoSubmitted) {
        try {
            finished = true;
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mindshare/quiz/QuizResultsView.fxml"));
            Parent root = loader.load();
            QuizResultsController controller = loader.getController();
            controller.setResults(resultsJson, autoSubmitted);
            Stage stage = (Stage) submitButton.getScene().getWindow();
            SceneUtils.switchScene(stage, root);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
