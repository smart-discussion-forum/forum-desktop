package com.mindshare.quiz;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindshare.api.QuizService;
import com.mindshare.utils.SceneUtils;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class QuizListController {

    @FXML private Label statusLabel;
    @FXML private ListView<QuizListItem> quizListView;
    @FXML private Button openButton;
    @FXML private Button backButton;

    private final QuizService quizService = new QuizService();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private List<QuizListItem> quizzes = new ArrayList<>();

    @FXML
    public void initialize() {
        loadQuizzes();
    }

    private void loadQuizzes() {
        statusLabel.setText("Loading quizzes...");

        Task<List<QuizListItem>> task = new Task<>() {
            @Override
            protected List<QuizListItem> call() throws Exception {
                JsonNode response = quizService.fetchQuizzes();
                JsonNode quizArray = response.path("quizzes");
                if (quizArray.isMissingNode() || !quizArray.isArray()) {
                    return List.of();
                }

                List<QuizListItem> items = new ArrayList<>();
                for (JsonNode node : quizArray) {
                    items.add(objectMapper.treeToValue(node, QuizListItem.class));
                }
                return items;
            }
        };

        task.setOnSucceeded(event -> {
            quizzes = task.getValue();
            quizListView.setItems(FXCollections.observableArrayList(quizzes));
            statusLabel.setText(quizzes.isEmpty() ? "No quizzes available." : "Select a quiz to start.");
        });

        task.setOnFailed(event -> {
            task.getException().printStackTrace();
            statusLabel.setText("Could not load quizzes from the API.");
        });

        new Thread(task).start();
    }

    @FXML
    private void handleOpen(ActionEvent event) {
        QuizListItem selected = quizListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a quiz first.");
            return;
        }

        try {
            if (selected.getMyAttemptId() != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mindshare/quiz/QuizResultsView.fxml"));
                Parent root = loader.load();
                QuizResultsController controller = loader.getController();
                JsonNode results = quizService.fetchResults(selected.getMyAttemptId());
                controller.setResults(results, false);

                Stage stage = (Stage) openButton.getScene().getWindow();
                stage.setScene(SceneUtils.createStyledScene(root, 900, 620));
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mindshare/quiz/QuizTakingView.fxml"));
            Parent root = loader.load();
            QuizTakingController controller = loader.getController();
            int durationMinutes = selected.getDurationMinutes() != null ? selected.getDurationMinutes() : 0;
            controller.setQuiz(new Quiz(selected.getId(), selected.getTitle(), durationMinutes));

            Stage stage = (Stage) openButton.getScene().getWindow();
            stage.setScene(SceneUtils.createStyledScene(root, 600, 420));
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Could not open the selected quiz.");
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mindshare/dashboard/DashboardView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(SceneUtils.createStyledScene(root, 600, 420));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
