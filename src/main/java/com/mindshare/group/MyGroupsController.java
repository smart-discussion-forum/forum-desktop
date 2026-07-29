package com.mindshare.group;

import com.mindshare.api.GroupService;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class MyGroupsController {

    @FXML
    private VBox groupsContainer;

    @FXML
    private Label statusLabel;

    @FXML
    private Button backButton;

    private final GroupService groupService = new GroupService();

    @FXML
    public void initialize() {
        loadGroups();
    }

    private void loadGroups() {
        Task<List<Group>> fetchTask = new Task<>() {
            @Override
            protected List<Group> call() throws Exception {
                return groupService.fetchMyGroups();
            }
        };

        fetchTask.setOnSucceeded(event -> {
            List<Group> groups = fetchTask.getValue();
            renderGroups(groups);
        });

        fetchTask.setOnFailed(event -> {
            Throwable failure = fetchTask.getException();
            groupsContainer.getChildren().clear();
            showStatus(failure != null && failure.getMessage() != null
                    ? failure.getMessage()
                    : "Could not load groups. Check your connection.");
            if (failure != null) {
                failure.printStackTrace();
            }
        });

        new Thread(fetchTask).start();
    }

    private void renderGroups(List<Group> groups) {
        groupsContainer.getChildren().clear();

        if (groups == null || groups.isEmpty()) {
            showStatus("You haven't joined any groups yet.");
            return;
        }

        hideStatus();

        for (Group group : groups) {
            groupsContainer.getChildren().add(buildGroupCard(group));
        }
    }

    private VBox buildGroupCard(Group group) {
        Label name = new Label(group.getName());
        name.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: #101827;");

        VBox card = new VBox(6, name);
        card.getStyleClass().add("group-card");
        card.setCursor(Cursor.HAND);

        if (group.getDescription() != null && !group.getDescription().isBlank()) {
            Label desc = new Label(group.getDescription());
            desc.setWrapText(true);
            desc.getStyleClass().add("group-card-subtitle");
            card.getChildren().add(desc);
        }

        String role = group.getPivot() != null ? group.getPivot().getRole() : "Member";
        Label roleBadge = new Label(role);
        roleBadge.getStyleClass().add("status-pill");
        VBox.setMargin(roleBadge, new Insets(4, 0, 0, 0));
        card.getChildren().add(roleBadge);

        card.setOnMouseClicked(event -> openGroupDetail(group));

        return card;
    }

    private void openGroupDetail(Group group) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/mindshare/chat/ChatView.fxml"));
            Parent root = loader.load();

            com.mindshare.chat.ChatController chatController = loader.getController();
            chatController.setGroupId(group.getId());

            Stage stage = (Stage) groupsContainer.getScene().getWindow();
            com.mindshare.utils.SceneUtils.switchScene(stage, root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showStatus(String message) {
        statusLabel.setText(message);
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);
    }

    private void hideStatus() {
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            boolean isAdmin = "admin".equalsIgnoreCase(com.mindshare.auth.model.UserSession.getUserRole());
            String path = isAdmin ? "/com/mindshare/admin/AdminLandingView.fxml" : "/com/mindshare/dashboard/DashboardView.fxml";
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(path));
            Parent dashboardRoot = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            com.mindshare.utils.SceneUtils.switchScene(stage, dashboardRoot);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
