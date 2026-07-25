package com.mindshare.discussion.controller;

import com.mindshare.api.PostService;
import com.mindshare.discussion.model.Post;
import com.mindshare.discussion.model.Topic;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import com.mindshare.group.Group;

public class TopicDetailController {
    @FXML private Label topicTitleLabel;
    @FXML private ListView<Post> postsListView;
    @FXML private TextArea replyField;
    @FXML private Button sendButton;
    @FXML private Button backButton;

    private Topic currentTopic;
    private Group currentGroup;
    private final PostService postService = new PostService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a").withZone(ZoneId.systemDefault());

    public void setTopic(Topic topic) {
        this.currentTopic = topic;
        topicTitleLabel.setText(topic.getTitle());
        setupCellFactory();
        loadPosts();
    }
    public void setGroup(Group group) {
        this.currentGroup = group;
    }

    private void setupCellFactory() {
        postsListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Post post, boolean empty) {
                super.updateItem(post, empty);
                if (empty || post == null) {
                    setGraphic(null);
                    return;
                }

                Label author = new Label(post.getAuthorName());
                author.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 12px;");

                Label content = new Label(post.getContent());
                content.setWrapText(true);
                content.setStyle("-fx-text-fill: #1a1a1a; -fx-font-size: 14px;");

                Label time = new Label(formatDate(post.getCreatedAt()));
                time.setStyle("-fx-text-fill: #adb5bd; -fx-font-size: 11px;");

                VBox card = new VBox(4, author, content, time);
                card.setPadding(new Insets(12, 16, 12, 16));
                card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 1);");
                card.setMaxWidth(660);

                setGraphic(card);
                setStyle("-fx-background-color: transparent; -fx-padding: 4 0;");
            }
        });
    }

    private String formatDate(String rawIso) {
        if (rawIso == null || rawIso.isBlank()) return "";
        try {
            // Laravel sends microsecond precision e.g. 2026-07-08T06:49:44.000000Z
            Instant instant = Instant.parse(rawIso);
            return DISPLAY_FORMAT.format(instant);
        } catch (Exception e) {
            return rawIso;
        }
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
                        posts.add(parsePost(node));
                    }
                }
                return posts;
            }
        };

        task.setOnSucceeded(e -> postsListView.setItems(FXCollections.observableArrayList(task.getValue())));
        task.setOnFailed(e -> task.getException().printStackTrace());
        new Thread(task).start();
    }

    private Post parsePost(JsonNode node) {
        String author = node.path("user").path("name").asText(
                node.path("author_name").asText(
                        node.path("author").path("name").asText("Unknown")));
        String content = node.path("content").asText(node.path("message").asText(""));
        String createdAt = node.path("created_at").asText("");
        return new Post(node.path("id").asInt(), author, content, createdAt);
    }

    @FXML
    private void handleSend(ActionEvent event) {
        String replyText = replyField.getText();
        if (replyText == null || replyText.isBlank()) return;

        javafx.concurrent.Task<Post> task = new javafx.concurrent.Task<>() {
            @Override
            protected Post call() throws Exception {
                JsonNode response = postService.createPost(currentTopic.getId(), replyText);
                JsonNode postNode = response.has("post") ? response.path("post") : response;
                return parsePost(postNode);
            }
        };

        task.setOnSucceeded(e -> {
            postsListView.getItems().add(task.getValue());
            replyField.clear();
        });
        task.setOnFailed(e -> task.getException().printStackTrace());
        new Thread(task).start();
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/mindshare/group/GroupDetailView.fxml"));
            Parent groupDetailRoot = loader.load();

            com.mindshare.group.GroupDetailController controller = loader.getController();
            controller.setGroup(currentGroup);

            Stage stage = (Stage) backButton.getScene().getWindow();
            com.mindshare.utils.SceneUtils.switchScene(stage, groupDetailRoot);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}