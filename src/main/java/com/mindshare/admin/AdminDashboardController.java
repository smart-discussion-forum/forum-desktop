package com.mindshare.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.mindshare.api.ModerationService;
import javafx.concurrent.Task;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import java.util.Optional;

public class AdminDashboardController {

    @FXML private Label statusLabel;
    @FXML private ListView<String> userListView;
    @FXML private Button warningButton;
    @FXML private Button blacklistButton;
    @FXML private Button statisticsButton;
    @FXML private Button backButton;

    private final ModerationService moderationService = new ModerationService();

    @FXML
    public void initialize() {
        loadBlacklist();
    }

    private void loadBlacklist() {
        statusLabel.setText("Loading blacklist...");
        Task<JsonNode> task = new Task<>() {
            @Override
            protected JsonNode call() throws Exception {
                return moderationService.fetchBlacklist();
            }
        };

        task.setOnSucceeded(event -> {
            JsonNode response = task.getValue();
            JsonNode entries = response.isArray() ? response : response.path("blacklist");
            if (!entries.isArray()) {
                userListView.setItems(FXCollections.observableArrayList());
                statusLabel.setText("No active blacklist entries.");
                return;
            }

            javafx.collections.ObservableList<String> rows = FXCollections.observableArrayList();
            for (JsonNode entry : entries) {
                String name = entry.path("user").path("name").asText("Unknown");
                String reason = entry.path("Reason").asText(entry.path("reason").asText(""));
                rows.add(name + " - " + reason);
            }
            userListView.setItems(rows);
            statusLabel.setText(rows.isEmpty() ? "No active blacklist entries." : "Blacklist loaded.");
        });

        task.setOnFailed(event -> {
            Throwable failure = task.getException();
            statusLabel.setText(failure != null && failure.getMessage() != null
                    ? failure.getMessage()
                    : "Could not load blacklist.");
            if (failure != null) {
                failure.printStackTrace();
            }
        });

        new Thread(task).start();
    }

    @FXML
    private void handleWarning(ActionEvent event) {
        Optional<String> userIdInput = prompt("Issue Warning", "User ID", "Enter the user ID to warn:");
        Optional<String> reasonInput = prompt("Issue Warning", "Reason", "Enter the warning reason:");
        if (userIdInput.isEmpty() || reasonInput.isEmpty()) {
            return;
        }

        try {
            int userId = Integer.parseInt(userIdInput.get().trim());
            String reason = reasonInput.get().trim();
            statusLabel.setText("Issuing warning...");

            Task<JsonNode> task = new Task<>() {
                @Override
                protected JsonNode call() throws Exception {
                    return moderationService.issueWarning(userId, reason);
                }
            };

            task.setOnSucceeded(e -> {
                statusLabel.setText(task.getValue().path("message").asText("Warning issued."));
                loadBlacklist();
            });

            task.setOnFailed(e -> {
                Throwable failure = task.getException();
                statusLabel.setText(failure != null && failure.getMessage() != null
                        ? failure.getMessage()
                        : "Could not issue warning.");
            });

            new Thread(task).start();
        } catch (NumberFormatException ex) {
            statusLabel.setText("User ID must be a number.");
        }
    }

    @FXML
    private void handleBlacklist(ActionEvent event) {
        Optional<String> userIdInput = prompt("Blacklist User", "User ID", "Enter the user ID to blacklist:");
        Optional<String> reasonInput = prompt("Blacklist User", "Reason", "Enter the blacklist reason:");
        if (userIdInput.isEmpty() || reasonInput.isEmpty()) {
            return;
        }

        try {
            int userId = Integer.parseInt(userIdInput.get().trim());
            String reason = reasonInput.get().trim();
            statusLabel.setText("Blacklisting user...");

            Task<JsonNode> task = new Task<>() {
                @Override
                protected JsonNode call() throws Exception {
                    return moderationService.fetchBlacklist();
                }
            };

            task.setOnSucceeded(e -> statusLabel.setText("Use the Laravel web admin for direct blacklist actions."));
            task.setOnFailed(e -> statusLabel.setText("Could not access blacklist API."));
            new Thread(task).start();
        } catch (NumberFormatException ex) {
            statusLabel.setText("User ID must be a number.");
        }
    }

    @FXML
    private void handleStatistics(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mindshare/admin/ParticipationStatisticsView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) statisticsButton.getScene().getWindow();
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Optional<String> prompt(String title, String header, String content) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(content);
        return dialog.showAndWait();
    }
}
