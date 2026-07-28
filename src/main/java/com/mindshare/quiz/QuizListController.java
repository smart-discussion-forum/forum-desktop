package com.mindshare.quiz;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindshare.api.QuizService;
import com.mindshare.auth.model.UserSession;
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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class QuizListController {
    @FXML private Label statusLabel;
    @FXML private TableView<QuizListItem> quizTable;
    @FXML private TableColumn<QuizListItem, String> titleColumn;
    @FXML private TableColumn<QuizListItem, String> groupColumn;
    @FXML private TableColumn<QuizListItem, String> startColumn;
    @FXML private TableColumn<QuizListItem, String> statusColumn;
    @FXML private TableColumn<QuizListItem, String> actionsColumn;
    @FXML private Button createButton;
    @FXML private Button openButton;
    @FXML private Button backButton;

    private final QuizService quizService = new QuizService();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Timeline poller;

    @FXML
    public void initialize() {
        titleColumn.setCellValueFactory(data -> text(data.getValue().getTitle()));
        groupColumn.setCellValueFactory(data -> text(data.getValue().getGroupName()));
        startColumn.setCellValueFactory(data -> text(data.getValue().getStartTimeDisplay()));
        statusColumn.setCellValueFactory(data -> text(status(data.getValue())));
        actionsColumn.setCellValueFactory(data -> text(actionText(data.getValue())));
        actionsColumn.setCellFactory(column -> new ActionCell());

        boolean lecturer = "lecturer".equalsIgnoreCase(UserSession.getUserRole());
        createButton.setVisible(lecturer);
        createButton.setManaged(lecturer);
        loadQuizzes(false);

        poller = new Timeline(new KeyFrame(Duration.seconds(5), event -> loadQuizzes(true)));
        poller.setCycleCount(Timeline.INDEFINITE);
        poller.play();
    }

    private ReadOnlyStringWrapper text(String value) {
        return new ReadOnlyStringWrapper(value == null || value.isBlank() ? "-" : value);
    }

    private String status(QuizListItem item) {
        String value = item.getStatus();
        if (value == null || value.isBlank()) return "-";
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    private String actionText(QuizListItem item) {
        if (item.canAnnounce()) return "Announce";
        if (item.getMyAttemptId() != null) return "Completed";
        if ("closed".equalsIgnoreCase(item.getStatus())) return "Closed";
        return item.isOwner() || "admin".equalsIgnoreCase(UserSession.getUserRole()) ? "Open details" : "Open quiz";
    }

    private void loadQuizzes(boolean backgroundPoll) {
        Task<List<QuizListItem>> task = new Task<>() {
            @Override protected List<QuizListItem> call() throws Exception {
                JsonNode response = quizService.fetchQuizzes();
                JsonNode array = response.path("quizzes");
                if (!array.isArray()) return List.of();
                List<QuizListItem> result = new ArrayList<>();
                for (JsonNode node : array) result.add(objectMapper.treeToValue(node, QuizListItem.class));
                return result;
            }
        };
        task.setOnSucceeded(event -> {
            List<QuizListItem> fresh = task.getValue();
            QuizListItem selected = quizTable.getSelectionModel().getSelectedItem();
            quizTable.setItems(FXCollections.observableArrayList(fresh));
            if (selected != null) {
                fresh.stream().filter(item -> item.getId() == selected.getId()).findFirst()
                        .ifPresent(item -> quizTable.getSelectionModel().select(item));
            }
            if (!backgroundPoll) statusLabel.setText(fresh.isEmpty() ? "No quizzes available." : "Select a quiz to start.");
        });
        task.setOnFailed(event -> {
            if (!backgroundPoll) statusLabel.setText("Could not load quizzes from the API.");
        });
        Thread thread = new Thread(task, "quiz-list-load");
        thread.setDaemon(true);
        thread.start();
    }

    @FXML private void handleCreate(ActionEvent event) { navigate("/com/mindshare/quiz/QuizConfigurationView.fxml", createButton); }

    @FXML
    private void handleOpen(ActionEvent event) {
        QuizListItem selected = quizTable.getSelectionModel().getSelectedItem();
        if (selected == null) { statusLabel.setText("Select a quiz first."); return; }
        openQuiz(selected);
    }

    private void openQuiz(QuizListItem selected) {
        try {
            if (selected.isOwner() || "admin".equalsIgnoreCase(UserSession.getUserRole())) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mindshare/quiz/QuizOwnerView.fxml"));
                Parent root = loader.load();
                loader.<QuizOwnerController>getController().setQuiz(selected);
                SceneUtils.switchScene((Stage) openButton.getScene().getWindow(), root);
            } else if (selected.getMyAttemptId() != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mindshare/quiz/QuizResultsView.fxml"));
                Parent root = loader.load();
                loader.<QuizResultsController>getController().loadResults(selected.getMyAttemptId(), false);
                SceneUtils.switchScene((Stage) openButton.getScene().getWindow(), root);
            } else {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mindshare/quiz/QuizTakingView.fxml"));
                Parent root = loader.load();
                int duration = selected.getDurationMinutes() == null ? 0 : selected.getDurationMinutes();
                loader.<QuizTakingController>getController().setQuiz(new Quiz(selected.getId(), selected.getTitle(), duration, selected.getStartTime()));
                SceneUtils.switchScene((Stage) openButton.getScene().getWindow(), root);
            }
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Could not open the selected quiz.");
        }
    }

    private void announce(QuizListItem item, Button source) {
        source.setDisable(true);
        Task<JsonNode> task = new Task<>() {
            @Override protected JsonNode call() throws Exception { return quizService.announceQuiz(item.getId()); }
        };
        task.setOnSucceeded(event -> { statusLabel.setText("Quiz announced to the group."); loadQuizzes(false); });
        task.setOnFailed(event -> { source.setDisable(false); statusLabel.setText("Could not announce the quiz."); });
        Thread thread = new Thread(task, "quiz-announce");
        thread.setDaemon(true);
        thread.start();
    }

    @FXML private void handleBack(ActionEvent event) { navigate("/com/mindshare/dashboard/DashboardView.fxml", backButton); }

    private void navigate(String path, Button source) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            SceneUtils.switchScene((Stage) source.getScene().getWindow(), root);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private class ActionCell extends TableCell<QuizListItem, String> {
        private final Button action = new Button();

        ActionCell() {
            action.getStyleClass().add("chat-btn");
        action.setOnAction(event -> {
                QuizListItem item = getTableView().getItems().get(getIndex());
                if (item.canAnnounce()) {
                    announce(item, action);
                } else if ("closed".equalsIgnoreCase(item.getStatus()) && item.getMyAttemptId() == null) {
                    statusLabel.setText("This quiz has closed; no attempt was recorded.");
                } else {
                    openQuiz(item);
                }
            });
        }

        @Override protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) { setGraphic(null); setText(null); return; }
            action.setText(item);
            // A completed quiz remains actionable so students can reopen their results.
            action.setDisable(false);
            setGraphic(action);
            setText(null);
        }
    }
}
