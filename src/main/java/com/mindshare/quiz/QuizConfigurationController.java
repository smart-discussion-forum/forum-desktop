package com.mindshare.quiz;

import com.mindshare.utils.SceneUtils;
import javafx.scene.Node;
import com.mindshare.api.GroupService;
import com.mindshare.api.QuizAdminService;
import com.mindshare.group.Group;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

public class QuizConfigurationController {

    @FXML
    private TextField titleField;
    @FXML
    private DatePicker datePicker;
    @FXML
    private TextField durationField;
    @FXML
    private ComboBox<Group> categoryBox;
    @FXML
    private VBox questionsContainer;
    @FXML
    private Label statusLabel;
    @FXML
    private Button saveButton;
    @FXML
    private Button backButton;
    @FXML
    private Button addQuestionButton;

    @FXML
    private Button dashboardButton;
    @FXML
    private Button groupChatButton;

    private final GroupService groupService = new GroupService();
    private final QuizAdminService quizAdminService = new QuizAdminService();
    private final List<QuizQuestionBuilderView> questionBuilders = new ArrayList<>();

    @FXML
    public void initialize() {
        loadGroups();
        addQuestion();
    }

    private void loadGroups() {
        Task<List<Group>> fetchTask = new Task<>() {
            @Override
            protected List<Group> call() throws Exception {
                return groupService.fetchMyGroups();
            }
        };

        fetchTask.setOnSucceeded(event -> {
            categoryBox.setItems(FXCollections.observableArrayList(fetchTask.getValue()));
        });

        fetchTask.setOnFailed(event -> {
            fetchTask.getException().printStackTrace();
            statusLabel.setText("Could not load groups from the API.");
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setVisible(true);
        });
        new Thread(fetchTask).start();
    }

    @FXML
    private void handleSave(ActionEvent event) {
        if (titleField.getText().isBlank()
                || datePicker.getValue() == null
                || durationField.getText().isBlank()
                || categoryBox.getValue() == null) {
            statusLabel.setText("Title, date, duration, at least one question, and target group are required.");
            statusLabel.setStyle("-fx-text-fill: red");
            statusLabel.setVisible(true);
            return;
        }

        try {
            Integer durationMinutes = Integer.parseInt(durationField.getText().trim());

            // Format selected date into ISO DateTime string (e.g., 2026-07-22T00:00:00)
            LocalDate selectedDate = datePicker.getValue();
            String normalizedDate = selectedDate.atStartOfDay().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            Group targetGroup = categoryBox.getValue();
            List<QuizAdminService.QuestionPayload> questions = buildQuestionPayloads();
            if (questions.isEmpty()) {
                statusLabel.setText("Add at least one valid question.");
                statusLabel.setStyle("-fx-text-fill: red");
                statusLabel.setVisible(true);
                return;
            }

            Task<com.fasterxml.jackson.databind.JsonNode> saveTask = new Task<>() {
                @Override
                protected com.fasterxml.jackson.databind.JsonNode call() throws Exception {
                    return quizAdminService.createQuiz(
                            new QuizAdminService.CreateQuizRequest(
                                    titleField.getText().trim(),
                                    targetGroup.getId(),
                                    normalizedDate,
                                    durationMinutes,
                                    questions
                            )
                    );
                }
            };

            saveTask.setOnSucceeded(task -> {
                com.fasterxml.jackson.databind.JsonNode response = saveTask.getValue();
                statusLabel.setText(response.has("message")
                        ? response.get("message").asText()
                        : "Quiz created successfully.");
                if (response.has("quiz")) {
                    statusLabel.setText(statusLabel.getText() + " Quiz ID: " + response.get("quiz").path("quiz_id").asText("?"));
                }
                statusLabel.setStyle("-fx-text-fill: green;");
                statusLabel.setVisible(true);
            });

            saveTask.setOnFailed(task -> {
                saveTask.getException().printStackTrace();
                Throwable failure = saveTask.getException();
                statusLabel.setText(failure != null && failure.getMessage() != null
                        ? failure.getMessage()
                        : "Could not save quiz to the API.");
                statusLabel.setStyle("-fx-text-fill: red;");
                statusLabel.setVisible(true);
            });

            new Thread(saveTask).start();
        } catch (NumberFormatException ex) {
            statusLabel.setText("Duration must be a number.");
            statusLabel.setStyle("-fx-text-fill: red");
            statusLabel.setVisible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
            statusLabel.setText("An unexpected error occurred while saving.");
            statusLabel.setStyle("-fx-text-fill: red");
            statusLabel.setVisible(true);
        }
    }

    @FXML
    private void addQuestion() {
        QuizQuestionDraft draft = new QuizQuestionDraft();
        final QuizQuestionBuilderView[] builderRef = new QuizQuestionBuilderView[1];
        QuizQuestionBuilderView builder = new QuizQuestionBuilderView(
                questionBuilders.size() + 1,
                draft,
                () -> removeQuestion(builderIndex(builderRef[0]))
        );
        builderRef[0] = builder;
        questionBuilders.add(builder);
        questionsContainer.getChildren().add(builder);
        refreshQuestionHeaders();
    }

    private void removeQuestion(int index) {
        if (index < 0 || index >= questionBuilders.size()) {
            return;
        }
        if (questionBuilders.size() == 1) {
            statusLabel.setText("At least one question is required.");
            statusLabel.setStyle("-fx-text-fill: red");
            statusLabel.setVisible(true);
            return;
        }

        QuizQuestionBuilderView builder = questionBuilders.remove(index);
        questionsContainer.getChildren().remove(builder);
        refreshQuestionHeaders();
    }

    private int builderIndex(QuizQuestionBuilderView builder) {
        return questionBuilders.indexOf(builder);
    }

    private void refreshQuestionHeaders() {
        for (int i = 0; i < questionBuilders.size(); i++) {
            QuizQuestionBuilderView builder = questionBuilders.get(i);
            builder.setId("question-" + (i + 1));
        }
    }

    private List<QuizAdminService.QuestionPayload> buildQuestionPayloads() {
        List<QuizAdminService.QuestionPayload> questions = new ArrayList<>();
        for (QuizQuestionBuilderView builder : questionBuilders) {
            QuizQuestionDraft draft = builder.getDraft();
            if (!builder.isValid()) {
                throw new IllegalArgumentException("Fill every question, option, correct answer, and marks field.");
            }

            questions.add(new QuizAdminService.QuestionPayload(
                    draft.getQuestionText().trim(),
                    new ArrayList<>(draft.getOptions()),
                    draft.getCorrectOptionIndex(),
                    draft.getMarks()
            ));
        }
        return questions;
    }

    @FXML
    private void handleBack(ActionEvent event) {
        navigateTo(event, "/com/mindshare/dashboard/DashboardView.fxml");
    }

    @FXML
    private void handleDashboard(ActionEvent event) {
        navigateTo(event, "/com/mindshare/dashboard/DashboardView.fxml");
    }

    @FXML
    private void handleGroupChat(ActionEvent event) {
        navigateTo(event, "/com/mindshare/chat/ChatView.fxml");
    }

        private void navigateTo (ActionEvent event, String fxmlPath){
            try {
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
                SceneUtils.switchScene(stage, root);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
