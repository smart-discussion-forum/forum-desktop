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
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ProfileController {
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField roleField;
    @FXML private TextField statusField;
    @FXML private Label statusLabel;
    @FXML private Button dashboardButton;
    @FXML private Button chatButton;
    @FXML private Button quizzesButton;
    @FXML private Button refreshButton;

    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        boolean isAdmin = "admin".equalsIgnoreCase(UserSession.getUserRole());
        chatButton.setVisible(!isAdmin);
        chatButton.setManaged(!isAdmin);

        renderFromSession();
        loadProfile();
    }

    private void renderFromSession() {
        nameField.setText(safe(UserSession.getUserName(), "Unknown"));
        emailField.setText(safe(UserSession.getUserEmail(), "-"));
        roleField.setText(capitalize(safe(UserSession.getUserRole(), "-")));
        statusField.setText("-");
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
            String status = userNode.path("status").asText("-");

            nameField.setText(safe(name, "Unknown"));
            emailField.setText(safe(email, "-"));
            roleField.setText(capitalize(safe(role, "-")));
            statusField.setText(capitalize(safe(status, "-")));
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

    private String capitalize(String value) {
        if (value == null || value.isBlank() || "-".equals(value)) return value;
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadProfile();
    }

    @FXML
    private void handleBack(ActionEvent event) {
        navigate("admin".equalsIgnoreCase(UserSession.getUserRole())
                ? "/com/mindshare/admin/AdminLandingView.fxml"
                : "/com/mindshare/dashboard/DashboardView.fxml");
    }

    @FXML
    private void handleGroupChat(ActionEvent event) {
        navigate("/com/mindshare/group/MyGroupsView.fxml");
    }

    @FXML
    private void handleQuizzes(ActionEvent event) {
        navigate("lecturer".equalsIgnoreCase(UserSession.getUserRole())
                ? "/com/mindshare/quiz/QuizConfigurationView.fxml"
                : "/com/mindshare/quiz/QuizListView.fxml");
    }

    private void navigate(String path) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            Stage stage = (Stage) dashboardButton.getScene().getWindow();
            com.mindshare.utils.SceneUtils.switchScene(stage, root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
