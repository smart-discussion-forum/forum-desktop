package com.mindshare.group;

import com.mindshare.api.GroupService;
import com.mindshare.auth.model.UserSession;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CreateGroupController {

    @FXML private TextField nameField;
    @FXML private TextArea descriptionField;
    @FXML private Label statusLabel;
    @FXML private Button createButton;
    @FXML private Button backButton;

    private final GroupService groupService = new GroupService();

    @FXML
    public void initialize() {
        if (!canCreateGroups()) {
            showStatus("Only lecturers can create groups.");
            createButton.setDisable(true);
            nameField.setDisable(true);
            descriptionField.setDisable(true);
        }
    }

    @FXML
    private void handleCreate(ActionEvent event) {
        if (!canCreateGroups()) {
            showStatus("Only lecturers can create groups.");
            return;
        }

        String name = nameField.getText() == null ? "" : nameField.getText().trim();
        String description = descriptionField.getText() == null ? "" : descriptionField.getText().trim();

        if (name.isBlank()) {
            showStatus("Group name is required.");
            return;
        }
        if (name.length() > 100) {
            showStatus("Group name must be 100 characters or fewer.");
            return;
        }

        createButton.setDisable(true);
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);

        Task<Group> task = new Task<>() {
            @Override
            protected Group call() throws Exception {
                return groupService.createGroup(name, description.isBlank() ? null : description);
            }
        };

        task.setOnSucceeded(e -> navigate("/com/mindshare/dashboard/DashboardView.fxml"));
        task.setOnFailed(e -> {
            createButton.setDisable(false);
            Throwable error = task.getException();
            showStatus(error != null && error.getMessage() != null
                    ? error.getMessage()
                    : "Could not create group.");
            if (error != null) error.printStackTrace();
        });

        Thread thread = new Thread(task, "create-group");
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void handleBack(ActionEvent event) {
        navigate("/com/mindshare/dashboard/DashboardView.fxml");
    }

    private boolean canCreateGroups() {
        String role = UserSession.getUserRole();
        return "lecturer".equalsIgnoreCase(role) || "admin".equalsIgnoreCase(role);
    }

    private void navigate(String path) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            Stage stage = (Stage) createButton.getScene().getWindow();
            com.mindshare.utils.SceneUtils.switchScene(stage, root);
        } catch (Exception e) {
            e.printStackTrace();
            showStatus("Could not open that screen.");
        }
    }

    private void showStatus(String message) {
        statusLabel.setText(message);
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);
    }
}
