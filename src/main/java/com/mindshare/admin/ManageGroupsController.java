package com.mindshare.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.mindshare.api.AdminGroupService;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Mirrors resources/views/groups/manage.blade.php — same data and actions, on the desktop. */
public class ManageGroupsController {
    @FXML private Label statusLabel;
    @FXML private TextField searchField;
    @FXML private VBox cardsContainer;
    @FXML private Button backButton;

    private final AdminGroupService groupService = new AdminGroupService();
    private List<JsonNode> allGroups = new ArrayList<>();

    @FXML
    public void initialize() {
        loadGroups();
    }

    private void loadGroups() {
        statusLabel.setText("Loading groups...");
        runTask("Could not load groups.", groupService::fetchManagedGroups, result -> {
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
            Label empty = new Label("No groups yet.");
            empty.getStyleClass().add("group-card-subtitle");
            cardsContainer.getChildren().add(empty);
            return;
        }
        for (JsonNode group : groups) {
            cardsContainer.getChildren().add(buildGroupCard(group));
        }
    }

    /** Mirrors resources/views/groups/manage.blade.php's table row for one group. */
    private VBox buildGroupCard(JsonNode group) {
        VBox card = new VBox(14);
        card.getStyleClass().add("group-card");

        Label nameLabel = new Label(group.path("name").asText(""));
        nameLabel.getStyleClass().add("group-card-title");
        Label creatorLabel = new Label("Created by " + group.path("created_by").asText("Unknown"));
        creatorLabel.getStyleClass().add("group-card-subtitle");
        VBox titleBox = new VBox(4, nameLabel, creatorLabel);

        Button statsButton = new Button("Stats");
        statsButton.setOnAction(e -> openStats(group.path("id").asInt()));
        Button editButton = new Button("Edit");
        editButton.setOnAction(e -> handleEdit(group));
        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(e -> handleDelete(group));

        HBox actions = new HBox(8, statsButton, editButton, deleteButton);
        HBox header = new HBox(12, titleBox, spacer(), actions);
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

    private void openStats(int groupId) {
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

    private void handleEdit(JsonNode group) {
        TextInputDialog nameDialog = new TextInputDialog(group.path("name").asText(""));
        nameDialog.setTitle("Edit Group");
        nameDialog.setHeaderText("Group name");
        nameDialog.setContentText("Enter a new name:");
        Optional<String> name = nameDialog.showAndWait();
        if (name.isEmpty() || name.get().isBlank()) return;

        TextInputDialog descDialog = new TextInputDialog(group.path("description").asText(""));
        descDialog.setTitle("Edit Group");
        descDialog.setHeaderText("Description");
        descDialog.setContentText("Enter a description (optional):");
        Optional<String> description = descDialog.showAndWait();
        if (description.isEmpty()) return;

        runTask("Could not update group.",
                () -> groupService.updateGroup(group.path("id").asInt(), name.get().trim(), description.get().trim()),
                result -> { statusLabel.setText("Group updated successfully."); loadGroups(); });
    }

    private void handleDelete(JsonNode group) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete \"" + group.path("name").asText("") + "\"? This cannot be undone.", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Delete Group");
        confirm.setHeaderText(null);
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.YES) return;

        runTask("Could not delete group.",
                () -> groupService.deleteGroup(group.path("id").asInt()),
                r -> { statusLabel.setText("Group deleted successfully."); loadGroups(); });
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
