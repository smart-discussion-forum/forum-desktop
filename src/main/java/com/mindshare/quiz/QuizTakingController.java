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

    private final QuizService quizService = new QuizService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
        questionLabel.setText("Starting quiz...");
        startAttempt();
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

               if (result.has("attempt") && result.get("attempt").has("Attempt_id")) {
                   attemptId = result.get("attempt").get("attempt").get("Attempt_id").asInt();
                   loadQuestions();
               } else {
                   questionLabel.setText(result.has("message") ? result.get("message").asText() : "Could not start quiz.");
               }
           });

           task.setOnFailed(e -> {
               task.getException().printStackTrace();
               questionLabel.setText("Error starting quiz attempt.");
           });

           new Thread(task).start();
       }

    private void loadQuestions() {
        Task<List<QuizQuestion>> task = new Task<>() {
            @Override
            protected List<QuizQuestion> call() throws Exception {
                JsonNode raw = quizService.fetchQuestions(quiz.getId());
                System.out.println("Raw questions response: " + raw);
                return objectMapper.readValue(raw.traverse(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, QuizQuestion.class));
            }
        };

        task.setOnSucceeded(e -> {
            questions = task.getValue();
            if (questions.isEmpty()) {
                questionLabel.setText("No questions found for this quiz.");
                return;
            }
            secondsRemaining = quiz.getDurationMinutes() * 60;
            startTimer();
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
        QuizQuestion q = questions.get(currentIndex);
        questionLabel.setText(q.getQuestion());
        optionsBox.getChildren().clear();
        optionsGroup = new ToggleGroup();

        for (String option : q.getOptions()) {
            RadioButton rb = new RadioButton(option);
            rb.setToggleGroup(optionsGroup);
            optionsBox.getChildren().add(rb);
        }
    }

    @FXML
    private void handleSubmit(ActionEvent event) {
        Toggle selected = optionsGroup.getSelectedToggle();
        String selectedText = selected != null ? ((RadioButton) selected).getText() : "";
        QuizQuestion currentQuestion = questions.get(currentIndex);

        Task<JsonNode> task = new Task<>() {
            @Override
            protected JsonNode call() throws Exception {
                return quizService.submitAnswer(attemptId, currentQuestion.getQuestionId(), selectedText);
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
                quizService.submitFullAttempt(attemptId);
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mindshare/quiz/QuizResultsView.fxml"));
            Parent root = loader.load();
            QuizResultsController controller = loader.getController();
            controller.setResults(resultsJson, autoSubmitted);
            Stage stage = (Stage) submitButton.getScene().getWindow();
            stage.setScene(SceneUtils.createStyledScene(root, 600, 420));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
