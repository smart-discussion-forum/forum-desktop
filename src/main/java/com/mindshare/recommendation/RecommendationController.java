package com.mindshare.recommendation;

import com.mindshare.discussion.controller.TopicDetailController;
import com.mindshare.discussion.model.Topic;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.util.List;


public class RecommendationController {
    @FXML private ListView<Topic> recommendationListView;
    @FXML private Button viewTopicButton;
    @FXML private Button refreshButton;
    @FXML private Button backButton;

    @FXML
    public void initialize() {
        loadRecommendations();
    }

    private void loadRecommendations() {
        // TODO: replace with real ML-driven recommendations from /api/recommendations (FR-14)
        List<Topic> placeholder = List.of(
                new Topic(4, "Trending: Exam prep tips", "Academic"),
                new Topic(5, "Popular: Group project ideas", "General"),
                new Topic(6, "Based on your activity: OOP patterns", "Coursework")
        );
        recommendationListView.setItems(FXCollections.observableArrayList(placeholder));
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
            stage.setScene(com.mindshare.utils.SceneUtils.createStyledScene(root, 600, 420));
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
            stage.setScene(com.mindshare.utils.SceneUtils.createStyledScene(root, 600, 420));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
