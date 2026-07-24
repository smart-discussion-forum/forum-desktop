package com.mindshare.recommendation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindshare.api.RecommendationServiceClient;
import com.mindshare.discussion.controller.TopicDetailController;
import com.mindshare.discussion.model.Topic;
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

import java.util.List;
import java.util.ArrayList;

public class RecommendationController {
    @FXML private ListView<Topic> recommendationListView;
    @FXML private Label statusLabel;
    @FXML private Button viewTopicButton;
    @FXML private Button refreshButton;
    @FXML private Button backButton;

    private final RecommendationServiceClient recommendationService = new RecommendationServiceClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @FXML
    public void initialize() {
        loadRecommendations();
    }

    private void loadRecommendations() {
        statusLabel.setText("Loading recommendations...");

        Task<List<Topic>> task = new Task<>() {
            @Override
            protected List<Topic> call() throws Exception {
                JsonNode response = recommendationService.fetchCombinedRecommendations();
                JsonNode combined = response.path("personalized");
                if (!combined.isArray()) {
                    combined = response.path("trending");
                }

                List<Topic> topics = new ArrayList<>();
                if (combined.isArray()) {
                    for (JsonNode entry : combined) {
                        JsonNode topicNode = entry.path("topic");
                        if (topicNode.isMissingNode() || topicNode.isNull()) {
                            topicNode = entry;
                        }
                        topics.add(objectMapper.treeToValue(topicNode, Topic.class));
                    }
                }
                return topics;
            }
        };

        task.setOnSucceeded(event -> {
            List<Topic> topics = task.getValue();
            recommendationListView.setItems(FXCollections.observableArrayList(topics));
            statusLabel.setText(topics == null || topics.isEmpty()
                    ? "No recommendations available."
                    : "Recommendations loaded.");
        });

        task.setOnFailed(event -> {
            Throwable failure = task.getException();
            statusLabel.setText(failure != null && failure.getMessage() != null
                    ? failure.getMessage()
                    : "Could not load recommendations.");
            if (failure != null) {
                failure.printStackTrace();
            }
        });

        new Thread(task).start();
    }

    @FXML
    private void handleViewTopic(ActionEvent event) {
        Topic selected = recommendationListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mindshare/discussion/TopicDetailView.fxml"));
            Parent root = loader.load();
            TopicDetailController controller = loader.getController();
            controller.setTopic(selected);
            Stage stage = (Stage) viewTopicButton.getScene().getWindow();
            com.mindshare.utils.SceneUtils.switchScene(stage, root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadRecommendations();
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mindshare/dashboard/DashboardView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            com.mindshare.utils.SceneUtils.switchScene(stage, root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
