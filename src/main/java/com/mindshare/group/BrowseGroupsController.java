package com.mindshare.group;

import com.mindshare.api.GroupService;
import com.mindshare.auth.model.UserSession;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class BrowseGroupsController {

    @FXML private Label myGroupsLabel;
    @FXML private ListView<Group> myGroupsListView;
    @FXML private Label joinableGroupsLabel;
    @FXML private ListView<Group> joinableGroupsListView;
    @FXML private Label statusLabel;
    @FXML private Button backButton;

    private final GroupService groupService = new GroupService();

    @FXML
    public void initialize() {
        setupMyGroupsCellFactory();
        setupJoinableGroupsCellFactory();
        loadGroups();
    }

    private void loadGroups() {
        Task<GroupService.BrowseGroupsResult> fetchTask = new Task<>() {
            @Override
            protected GroupService.BrowseGroupsResult call() throws Exception {
                return groupService.fetchBrowseGroups();
            }
        };

        fetchTask.setOnSucceeded(e -> {
            GroupService.BrowseGroupsResult result = fetchTask.getValue();
            myGroupsListView.setItems(FXCollections.observableArrayList(result.myGroups));
            joinableGroupsListView.setItems(FXCollections.observableArrayList(result.joinableGroups));
            myGroupsLabel.setText("My Groups (" + result.myGroups.size() + ")");
            joinableGroupsLabel.setText("Browse Groups to Join (" + result.joinableGroups.size() + ")");
            hideStatus();
        });

        fetchTask.setOnFailed(e -> {
            Throwable ex = fetchTask.getException();
            showStatus(ex != null && ex.getMessage() != null ? ex.getMessage() : "Could not load groups.");
            if (ex != null) ex.printStackTrace();
        });

        new Thread(fetchTask).start();
    }

    private void setupMyGroupsCellFactory() {
        myGroupsListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Group group, boolean empty) {
                super.updateItem(group, empty);
                if (empty || group == null) {
                    setGraphic(null);
                    return;
                }

                Label name = new Label(group.getName());
                name.setStyle("-fx-font-weight: bold;");
                Label desc = new Label(group.getDescription() == null ? "" : group.getDescription());
                desc.setStyle("-fx-text-fill: #666;");
                String role = group.getPivot() != null ? group.getPivot().getRole() : "Member";
                Label roleBadge = new Label(role);
                roleBadge.setStyle("-fx-background-color: #dbe4ee; -fx-background-radius: 12; -fx-padding: 2 10;");

                VBox info = new VBox(4, name, desc, roleBadge);

                Button leaveButton = new Button("Leave");
                leaveButton.setOnAction(ev -> handleLeave(group));

                HBox row = new HBox(15, info, leaveButton);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(8));
                HBox.setHgrow(info, Priority.ALWAYS);

                setGraphic(row);
            }
        });
    }

    private void setupJoinableGroupsCellFactory() {
        joinableGroupsListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Group group, boolean empty) {
                super.updateItem(group, empty);
                if (empty || group == null) {
                    setGraphic(null);
                    return;
                }

                Label name = new Label(group.getName());
                name.setStyle("-fx-font-weight: bold;");
                Label desc = new Label(group.getDescription() == null ? "" : group.getDescription());
                desc.setStyle("-fx-text-fill: #666;");
                int count = group.getMembersCount() == null ? 0 : group.getMembersCount();
                Label members = new Label(count + " members");
                members.setStyle("-fx-text-fill: #999;");

                VBox info = new VBox(4, name, desc, members);

                Button joinButton = new Button("Join");
                joinButton.setStyle("-fx-background-color: #3b5f8a; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 6 20; -fx-font-size: 13px;");
                joinButton.setOnAction(ev -> handleJoin(group));

                HBox row = new HBox(15, info, joinButton);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(8));
                HBox.setHgrow(info, Priority.ALWAYS);

                setGraphic(row);
            }
        });
    }

    private void handleJoin(Group group) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                groupService.joinGroup(group.getId());
                return null;
            }
        };

        task.setOnSucceeded(e -> loadGroups());
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            showStatus(ex != null ? ex.getMessage() : "Could not join group.");
            if (ex != null) ex.printStackTrace();
        });
        new Thread(task).start();
    }

    private void handleLeave(Group group) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                groupService.leaveGroup(group.getId());
                return null;
            }
        };

        task.setOnSucceeded(e -> loadGroups());
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            showStatus(ex != null ? ex.getMessage() : "Could not leave group.");
            if (ex != null) ex.printStackTrace();
        });
        new Thread(task).start();
    }

    private void showStatus(String msg) {
        statusLabel.setText(msg);
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
            boolean isAdmin = "admin".equalsIgnoreCase(UserSession.getUserRole());
            String path = isAdmin ? "/com/mindshare/admin/AdminLandingView.fxml" : "/com/mindshare/dashboard/DashboardView.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            com.mindshare.utils.SceneUtils.switchScene(stage, root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
