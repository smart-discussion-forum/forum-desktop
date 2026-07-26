package com.mindshare.group;

import com.mindshare.api.GroupService;
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

public class MyGroupsController {

    @FXML
    private ListView<Group> groupListView;

    @FXML
    private Label statusLabel;

    @FXML
    private Button backButton;

    private final GroupService groupService = new GroupService();

    @FXML
    public void initialize() {
        loadGroups();

        groupListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) { // click to open
                Group selected = groupListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    openGroupDetail(selected);
                }
            }
        });
    }

    private void openGroupDetail(Group group) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/mindshare/chat/ChatView.fxml"));
            Parent root = loader.load();


            // Pass selected group to ChatController
            com.mindshare.chat.ChatController chatController = loader.getController();
            chatController.setGroupId(group.getId());

            Stage stage = (Stage) groupListView.getScene().getWindow();
            com.mindshare.utils.SceneUtils.switchScene(stage, root);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            groupListView.setItems(FXCollections.observableArrayList(groups));
            statusLabel.setText(groups == null || groups.isEmpty() ? "No groups found for your account." : "");
            statusLabel.setVisible(groups == null || groups.isEmpty());
        });

        fetchTask.setOnFailed(event -> {
            Throwable failure = fetchTask.getException();
            statusLabel.setText(failure != null && failure.getMessage() != null
                    ? failure.getMessage()
                    : "Could not load groups. Check your connection.");
            statusLabel.setVisible(true);
            if (failure != null) {
                failure.printStackTrace();
            }
        });

        new Thread(fetchTask).start();
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
