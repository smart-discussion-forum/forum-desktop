package com.mindshare.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.mindshare.api.AdminStatisticsService;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class GroupStatisticsDetailController {
    @FXML private Label statusLabel;
    @FXML private Label nameLabel;
    @FXML private Label subtitleLabel;
    @FXML private HBox statsRow;
    @FXML private Label mostActiveTitleLabel;
    @FXML private Label mostActivePostsLabel;
    @FXML private Label leastActiveTitleLabel;
    @FXML private Label leastActivePostsLabel;
    @FXML private TableView<JsonNode> topicsTable;
    @FXML private TableColumn<JsonNode, String> topicTitleColumn;
    @FXML private TableColumn<JsonNode, String> topicCategoryColumn;
    @FXML private TableColumn<JsonNode, String> topicPostsColumn;
    @FXML private Button backButton;

    private final AdminStatisticsService statisticsService = new AdminStatisticsService();

    @FXML
    public void initialize() {
        topicTitleColumn.setCellValueFactory(data -> value(data.getValue(), "title"));
        topicCategoryColumn.setCellValueFactory(data -> value(data.getValue(), "category", "\u2014"));
        topicPostsColumn.setCellValueFactory(data -> value(data.getValue(), "posts_count", "0"));
    }

    public void loadGroup(int groupId) {
        statusLabel.setText("Loading group details...");
        Task<JsonNode> task = new Task<>() {
            @Override protected JsonNode call() throws Exception { return statisticsService.fetchGroupDetail(groupId); }
        };
        task.setOnSucceeded(e -> render(task.getValue().path("stats")));
        task.setOnFailed(e -> {
            Throwable failure = task.getException();
            statusLabel.setText(failure == null || failure.getMessage() == null
                    ? "Could not load group details." : failure.getMessage());
        });
        new Thread(task).start();
    }

    private void render(JsonNode stats) {
        statusLabel.setText("");
        nameLabel.setText(stats.path("name").asText("Group"));

        String creator = stats.path("created_by").asText("Unknown");
        String description = stats.path("description").isNull() ? "" : stats.path("description").asText("");
        subtitleLabel.setText("Created by " + creator + (description.isBlank() ? "" : " \u00b7 " + description));

        statsRow.getChildren().setAll(
                statBox("Members", stats.path("member_count").asText("0")),
                statBox("Topics", stats.path("topic_count").asText("0")),
                statBox("Total posts", stats.path("total_posts").asText("0")),
                statBox("Posts this week", stats.path("posts_this_week").asText("0")),
                statBox("Chat messages", stats.path("message_count").asText("0")));
        for (var node : statsRow.getChildren()) {
            HBox.setHgrow(node, Priority.ALWAYS);
        }

        setTopicInfo(mostActiveTitleLabel, mostActivePostsLabel, stats.path("most_active_topic"));
        setTopicInfo(leastActiveTitleLabel, leastActivePostsLabel, stats.path("least_active_topic"));

        JsonNode topics = stats.path("topics");
        List<JsonNode> topicList = new ArrayList<>();
        if (topics.isArray()) topics.forEach(topicList::add);
        topicsTable.setItems(FXCollections.observableArrayList(topicList));
    }

    private void setTopicInfo(Label titleLabel, Label postsLabel, JsonNode topic) {
        if (topic == null || topic.isMissingNode() || topic.isNull()) {
            titleLabel.setText("No topics yet.");
            postsLabel.setText("");
        } else {
            titleLabel.setText(topic.path("title").asText());
            postsLabel.setText(topic.path("posts_count").asText("0") + " posts");
        }
    }

    private VBox statBox(String label, String value) {
        Label labelNode = new Label(label);
        labelNode.getStyleClass().add("stat-box-label");
        Label valueNode = new Label(value);
        valueNode.getStyleClass().add("stat-box-value-large");
        VBox box = new VBox(8, labelNode, valueNode);
        box.getStyleClass().add("stat-box");
        box.setMaxWidth(Double.MAX_VALUE);
        return box;
    }

    private ReadOnlyStringWrapper value(JsonNode node, String field) {
        return value(node, field, "");
    }

    private ReadOnlyStringWrapper value(JsonNode node, String field, String fallback) {
        String text = node.path(field).isNull() ? fallback : node.path(field).asText(fallback);
        return new ReadOnlyStringWrapper(text);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mindshare/admin/ParticipationStatisticsView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            com.mindshare.utils.SceneUtils.switchScene(stage, root);
        } catch (Exception e) { e.printStackTrace(); }
    }
}
