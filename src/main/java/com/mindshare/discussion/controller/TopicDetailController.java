package com.mindshare.discussion.controller;

import com.mindshare.discussion.ExportDiscussionController;
import com.mindshare.discussion.model.Post;
import com.mindshare.discussion.model.Topic;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.util.List;

public class TopicDetailController {
    @FXML
    private Label topicTitleLabel;

    @FXML
    private ListView<Post> postsListView;

    @FXML
    private TextArea replyField;

    @FXML
    private Button sendButton;

    @FXML
    private Button refreshButton;

    @FXML
    private Button backButton;

    @FXML
    private Button exportButton;

    private Topic currentTopic;

    public void setTopic(Topic topic) {
        this.currentTopic = topic;
        topicTitleLabel.setText(topic.getTitle());
        loadPlaceholderPosts();
    }

    private void loadPlaceholderPosts() {
        // TODO: replace with real API call once /api/topics/{id}/posts exists
        List<Post> placeholderPosts = List.of(
                new Post(1, "Joel Agaba", "Has anyone started on part 2 yet?", "2026-07-14 09:15"),
                new Post(2, "Justine Peace", "Yes, I'm stuck on the migrations part.", "2026-07-14 09:22"),
                new Post(3, "Pearl Ikiring", "Check the seeder examples in the repo.", "2026-07-14 09:30")
        );
        postsListView.setItems(FXCollections.observableArrayList(placeholderPosts));
    }

    @FXML
    private void handleSend(ActionEvent event) {
        String replyText = replyField.getText();
        if (replyText.isBlank()) {
            return;
        }

        // TODO: replace with real POST to /api/topics/{id}/posts once available.
        // For now, just append it locally so the UI feels responsive during testing.
        Post newPost = new Post(
                postsListView.getItems().size() + 1,
                "You",
                replyText,
                "Just now"
        );
        postsListView.getItems().add(newPost);
        replyField.clear();
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadPlaceholderPosts();
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/mindshare/group/GroupDetailView.fxml"));
            Parent groupDetailRoot = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(com.mindshare.utils.SceneUtils.createStyledScene(groupDetailRoot, 600, 420));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleExport(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mindshare/discussion/ExportDiscussionView.fxml"));
            Parent root = loader.load();
            ExportDiscussionController controller = loader.getController();
            controller.setDiscussion(currentTopic, postsListView.getItems());
            Stage stage = (Stage) exportButton.getScene().getWindow();
            stage.setScene(com.mindshare.utils.SceneUtils.createStyledScene(root, 600, 420));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

