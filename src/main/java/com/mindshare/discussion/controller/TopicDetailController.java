package com.mindshare.discussion.controller;

import com.mindshare.discussion.ExportDiscussionController;
import com.mindshare.api.PostService;
import com.mindshare.discussion.model.Post;
import com.mindshare.discussion.model.Topic;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindshare.auth.model.UserSession;
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

import java.util.ArrayList;
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
    private final PostService postService = new PostService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void setTopic(Topic topic) {
        this.currentTopic = topic;
        topicTitleLabel.setText(topic.getTitle());
        loadPosts();
    }

    private void loadPosts() {
        javafx.concurrent.Task<List<Post>> task = new javafx.concurrent.Task<>() {
            @Override
            protected List<Post> call() throws Exception {
                JsonNode response = postService.fetchPosts(currentTopic.getId());
                JsonNode postsNode = response.isArray() ? response : response.path("posts");
                List<Post> posts = new ArrayList<>();
                if (postsNode != null && postsNode.isArray()) {
                    for (JsonNode node : postsNode) {
                        String author = node.path("author_name").asText(node.path("author").path("name").asText("Unknown"));
                        String content = node.path("content").asText(node.path("message").asText(""));
                        String createdAt = node.path("created_at").asText("");
                        posts.add(new Post(node.path("id").asInt(), author, content, createdAt));
                    }
                }
                return posts;
            }
        };

        task.setOnSucceeded(workerEvent -> postsListView.setItems(FXCollections.observableArrayList(task.getValue())));
        task.setOnFailed(workerEvent -> task.getException().printStackTrace());
        new Thread(task).start();
    }

    @FXML
    private void handleSend(ActionEvent event) {
        String replyText = replyField.getText();
        if (replyText.isBlank()) {
            return;
        }

        javafx.concurrent.Task<Post> task = new javafx.concurrent.Task<>() {
            @Override
            protected Post call() throws Exception {
                JsonNode response = postService.createPost(currentTopic.getId(), replyText);
                return new Post(
                        response.path("id").asInt(postsListView.getItems().size() + 1),
                        response.path("author_name").asText(UserSession.getUserName()),
                        response.path("content").asText(replyText),
                        response.path("created_at").asText("Just now")
                );
            }
        };

        task.setOnSucceeded(workerEvent -> {
            postsListView.getItems().add(task.getValue());
            replyField.clear();
        });
        task.setOnFailed(workerEvent -> task.getException().printStackTrace());
        new Thread(task).start();
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadPosts();
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

