package com.mindshare.lecturer;

import com.fasterxml.jackson.databind.JsonNode;
import com.mindshare.api.LecturerParticipationService;
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

/**
 * Lecturer landing screen for the Student Participation feature: lists the
 * groups this lecturer created (or all groups, if Admin) so they can pick
 * one to view its per-student leaderboard.
 */
public class LecturerGroupPickerController {
    @FXML private Label statusLabel;
    @FXML private TextField searchField;
    @FXML private VBox cardsContainer;
    @FXML private Button backButton;

    private final LecturerParticipationService participationService = new LecturerParticipationService();
    private List<JsonNode> allGroups = new ArrayList<>();

    @FXML
    public void initialize() {
        loadGroups();
    }

    private void loadGroups() {
        statusLabel.setText("Loading your groups...");
        Task<JsonNode> task = new Task<>() {
            @Override protected JsonNode call() throws Exception { return participationService.fetchManagedGroups(); }
        };
        task.setOnSucceeded(e -> {
            JsonNode groups = task.getValue().path("groups");
            allGroups = new ArrayList<>();
            if (groups.isArray()) groups.forEach(allGroups::add);
            renderCards(allGroups);
            statusLabel.setText(allGroups.isEmpty() ? "" : allGroups.size() + " group(s) loaded.");
        });
        task.setOnFailed(e -> {
            Throwable failure = task.getException();
            statusLabel.setText(failure == null || failure.getMessage() == null
                    ? "Could not load your groups." : failure.getMessage());
        });
        new Thread(task).start();
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
            Label empty = new Label("No groups yet. Create a group on the web app to start tracking participation.");
            empty.getStyleClass().add("group-card-subtitle");
            cardsContainer.getChildren().add(empty);
            return;
        }
        for (JsonNode group : groups) {
            cardsContainer.getChildren().add(buildGroupCard(group));
        }
    }

    private VBox buildGroupCard(JsonNode group) {
        VBox card = new VBox(14);
        card.getStyleClass().add("group-card");

        Label nameLabel = new Label(group.path("name").asText(""));
        nameLabel.getStyleClass().add("group-card-title");
        Label creatorLabel = new Label("Created by " + group.path("created_by").asText("Unknown"));
        creatorLabel.getStyleClass().add("group-card-subtitle");
        VBox titleBox = new VBox(4, nameLabel, creatorLabel);

        Button viewButton = new Button("View participation");
        viewButton.setOnAction(e -> openParticipation(group.path("id").asInt()));

        HBox header = new HBox(12, titleBox, spacer(), viewButton);
        header.setAlignment(Pos.TOP_LEFT);

        HBox statsRow = new HBox(12,
                statBox("Members", group.path("member_count").asText("0")),
                statBox("Topics", group.path("topic_count").asText("0")));
        for (var node : statsRow.getChildren()) {
            HBox.setHgrow(node, Priority.ALWAYS);
        }

        card.getChildren().addAll(header, statsRow);
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

    private Region spacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    private void openParticipation(int groupId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mindshare/lecturer/LecturerParticipationView.fxml"));
            Parent root = loader.load();
            LecturerParticipationController controller = loader.getController();
            controller.loadGroup(groupId);
            Stage stage = (Stage) cardsContainer.getScene().getWindow();
            com.mindshare.utils.SceneUtils.switchScene(stage, root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mindshare/dashboard/DashboardView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            com.mindshare.utils.SceneUtils.switchScene(stage, root);
        } catch (Exception e) { e.printStackTrace(); }
    }
}
