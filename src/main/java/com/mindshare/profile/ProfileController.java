package com.mindshare.profile;

import com.fasterxml.jackson.databind.JsonNode;
import com.mindshare.api.UserService;
import com.mindshare.auth.model.UserSession;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ProfileController {
    @FXML private Label nameLabel;
    @FXML private Label emailLabel;
    @FXML private Label roleLabel;
    @FXML private Label statusLabel;
    @FXML private Button backButton;
    @FXML private Button refreshButton;

    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        // Show whatever we already have from login immediately, then refresh from the backend.
        renderFromSession();
        loadProfile();
    }

    private void renderFromSession() {
        nameLabel.setText(safe(UserSession.getUserName(), "Unknown"));
        emailLabel.setText(safe(UserSession.getUserEmail(), "-"));
        roleLabel.setText(safe(UserSession.getUserRole(), "-"));
    }

    private void loadProfile() {
        statusLabel.setText("Refreshing profile...");

        Task<JsonNode> task = new Task<>() {
            @Override
            protected JsonNode call() throws Exception {
                return userService.fetchProfile();
            }
        };
        task.setOnSucceeded(event -> {
            JsonNode response = task.getValue();
            JsonNode userNode = response.has("user") ? response.get("user") : response;

            String name = userNode.path("name").asText(UserSession.getUserName());
            String email = userNode.path("email").asText(UserSession.getUserEmail());

            String role = userNode.path("role").asText(UserSession.getUserRole());

            nameLabel.setText(safe(name, "Unknown"));
            emailLabel.setText(safe(email, "-"));
            roleLabel.setText(safe(role, "-"));
            statusLabel.setText("Profile up to date.");
        });

        task.setOnFailed(event -> {
            Throwable failure = task.getException();
            statusLabel.setText("Showing cached info - could not reach the server."
                    + (failure != null && failure.getMessage() != null ? " (" + failure.getMessage() + ")" : ""));
            if (failure != null) {
                failure.printStackTrace();
            }
        });

        new Thread(task).start();
    }
    private String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadProfile();
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mindshare/dashboard/DashboardView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            com.mindshare.utils.SceneUtils.switchScene(stage, root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
