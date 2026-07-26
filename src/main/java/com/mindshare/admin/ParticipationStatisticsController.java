package com.mindshare.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.mindshare.api.AdminStatisticsService;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class ParticipationStatisticsController {
    @FXML private Label statusLabel;
    @FXML private TextField searchField;
    @FXML private VBox cardsContainer;
    @FXML private Button backButton;

    private final AdminStatisticsService statisticsService = new AdminStatisticsService();
    private List<JsonNode> allGroups = new ArrayList<>();

    @FXML
    public void initialize() {
        loadGroups();
    }

    private void loadGroups() {
        statusLabel.setText("Loading statistics...");
        runTask("Could not load statistics.", statisticsService::fetchGroupStatistics, result -> {
            JsonNode groups = result.path("groups");
            allGroups = new ArrayList<>();
            if (groups.isArray()) groups.forEach(allGroups::add);
            renderCards(allGroups);
            statusLabel.setText(allGroups.size() + " group(s) loaded.");
        });
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String search = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        List<JsonNode> filtered = new ArrayList<>();
        for (JsonNode group : allGroups) {
            if (search.isEmpty() || group.path("name").asText("").toLowerCase().contains(search)) {
                filtered.add(group);
            }
        }
        renderCards(filtered);
    }

    private void renderCards(List<JsonNode> groups) {
        cardsContainer.getChildren().clear();
        if (groups.isEmpty()) {
            Label empty = new Label("No groups yet. Create a group to start tracking statistics.");
            empty.getStyleClass().add("group-card-subtitle");
            cardsContainer.getChildren().add(empty);
            return;
        }
        for (JsonNode group : groups) {
            cardsContainer.getChildren().add(buildGroupCard(group));
        }
    }

    /** Mirrors resources/views/admin/statistics/index.blade.php's per-group card. */
    private VBox buildGroupCard(JsonNode group) {
        VBox card = new VBox(14);
        card.getStyleClass().add("group-card");

        Label nameLabel = new Label(group.path("name").asText(""));
        nameLabel.getStyleClass().add("group-card-title");
        Label creatorLabel = new Label("Created by " + group.path("created_by").asText("Unknown"));
        creatorLabel.getStyleClass().add("group-card-subtitle");
        VBox titleBox = new VBox(4, nameLabel, creatorLabel);

        Button detailsButton = new Button("View details");
        detailsButton.setOnAction(e -> openDetail(group.path("id").asInt()));

        HBox header = new HBox(12, titleBox, spacer(), detailsButton);
        header.setAlignment(Pos.TOP_LEFT);

        HBox statsRow = new HBox(12,
                statBox("Members", group.path("member_count").asText("0")),
                statBox("Topics", group.path("topic_count").asText("0")),
                statBox("Total posts", group.path("total_posts").asText("0")),
                statBox("Posts this week", group.path("posts_this_week").asText("0")));
        for (var node : statsRow.getChildren()) {
            HBox.setHgrow(node, Priority.ALWAYS);
        }

        Label mostActive = new Label(topicSummary("Most active topic:", group.path("most_active_topic")));
        Label leastActive = new Label(topicSummary("Least active topic:", group.path("least_active_topic")));
        mostActive.getStyleClass().add("group-card-subtitle");
        leastActive.getStyleClass().add("group-card-subtitle");
        mostActive.setWrapText(true);
        leastActive.setWrapText(true);
        HBox topicsRow = new HBox(20, mostActive, leastActive);

        card.getChildren().addAll(header, statsRow, topicsRow);
        return card;
    }

    private VBox statBox(String label, String value) {
        Label labelNode = new Label(label);
        labelNode.getStyleClass().add("stat-box-label");
        Label valueNode = new Label(value);
        valueNode.getStyleClass().add("stat-box-value");
        VBox box = new VBox(6, labelNode, valueNode);
        box.getStyleClass().add("stat-box");
        box.setMaxWidth(Double.MAX_VALUE);
        return box;
    }

    private String topicSummary(String prefix, JsonNode topic) {
        if (topic == null || topic.isMissingNode() || topic.isNull()) {
            return prefix + " None yet";
        }
        return prefix + " " + topic.path("title").asText() + " (" + topic.path("posts_count").asText("0") + " posts)";
    }

    private Region spacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    private void openDetail(int groupId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mindshare/admin/GroupStatisticsDetailView.fxml"));
            Parent root = loader.load();
            GroupStatisticsDetailController controller = loader.getController();
            controller.loadGroup(groupId);
            Stage stage = (Stage) cardsContainer.getScene().getWindow();
            com.mindshare.utils.SceneUtils.switchScene(stage, root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void runTask(String failureMessage, IoAction action, SuccessAction success) {
        Task<JsonNode> task = new Task<>() {
            @Override protected JsonNode call() throws Exception { return action.run(); }
        };
        task.setOnSucceeded(event -> success.accept(task.getValue()));
        task.setOnFailed(event -> {
            Throwable failure = task.getException();
            statusLabel.setText(failure == null || failure.getMessage() == null ? failureMessage : failure.getMessage());
        });
        new Thread(task).start();
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mindshare/admin/AdminLandingView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            com.mindshare.utils.SceneUtils.switchScene(stage, root);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private interface IoAction { JsonNode run() throws Exception; }
    private interface SuccessAction { void accept(JsonNode value); }
}
