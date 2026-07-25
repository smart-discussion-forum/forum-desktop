package com.mindshare.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.mindshare.api.AdminUserService;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.Optional;

public class AdminDashboardController {
    @FXML private Label statusLabel;
    @FXML private Label policyLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterBox;
    @FXML private TableView<JsonNode> usersTable;
    @FXML private TableColumn<JsonNode, String> nameColumn;
    @FXML private TableColumn<JsonNode, String> emailColumn;
    @FXML private TableColumn<JsonNode, String> roleColumn;
    @FXML private TableColumn<JsonNode, String> activeColumn;
    @FXML private TableColumn<JsonNode, String> statusColumn;
    @FXML private TableColumn<JsonNode, String> manualColumn;
    @FXML private TableColumn<JsonNode, String> automaticColumn;
    @FXML private Button runCheckButton;
    @FXML private Button warningButton;
    @FXML private Button blacklistButton;
    @FXML private Button reinstateButton;
    @FXML private Button statisticsButton;
    @FXML private Button backButton;

    private final AdminUserService adminUserService = new AdminUserService();

    @FXML
    public void initialize() {
        filterBox.setItems(FXCollections.observableArrayList("All", "Active", "Blacklisted", "Warned"));
        filterBox.getSelectionModel().selectFirst();
        nameColumn.setCellValueFactory(data -> value(data.getValue(), "name"));
        emailColumn.setCellValueFactory(data -> value(data.getValue(), "email"));
        roleColumn.setCellValueFactory(data -> value(data.getValue(), "role"));
        activeColumn.setCellValueFactory(data -> value(data.getValue(), "last_active", "Never"));
        statusColumn.setCellValueFactory(data -> value(data.getValue(), "status", "Active"));
        manualColumn.setCellValueFactory(data -> value(data.getValue(), "manual_warnings_count", "0"));
        automaticColumn.setCellValueFactory(data -> value(data.getValue(), "auto_warnings_count", "0"));
        loadUsers();
    }

    private ReadOnlyStringWrapper value(JsonNode node, String field) {
        return value(node, field, "");
    }

    private ReadOnlyStringWrapper value(JsonNode node, String field, String fallback) {
        String text = node.path(field).isNull() ? fallback : node.path(field).asText(fallback);
        return new ReadOnlyStringWrapper(text);
    }

    @FXML private void handleSearch(ActionEvent event) { loadUsers(); }

    @FXML private void handleFilter(ActionEvent event) { loadUsers(); }

    private void loadUsers() {
        statusLabel.setText("Loading users...");
        String filter = filterBox.getValue() == null ? "all" : filterBox.getValue().toLowerCase();
        String search = searchField.getText() == null ? "" : searchField.getText().trim();
        runTask("Could not load users.", () -> adminUserService.fetchUsers(filter, search), result -> {
            JsonNode users = result.path("users");
            System.out.println("First user raw: " + (users.isArray() && users.size() > 0 ? users.get(0).toString() : "none"));
            usersTable.setItems(users.isArray()
                    ? FXCollections.observableArrayList(users)
                    : FXCollections.observableArrayList());
            JsonNode moderation = result.path("moderation");
            policyLabel.setText(String.format(
                    "Automatic inactivity policy: warning 1 after %s day(s), warning 2 after %s more day(s), " +
                            "blacklist after %s day(s), duration %s day(s).",
                    moderation.path("first_warning_days").asText("-"),
                    moderation.path("second_warning_days").asText("-"),
                    moderation.path("blacklist_after_days").asText("-"),
                    moderation.path("blacklist_duration_days").asText("-")));
            statusLabel.setText(users.isArray() && users.size() > 0
                    ? users.size() + " user(s) loaded."
                    : "No users found.");
        });
    }

    @FXML
    private void handleRunInactivityCheck(ActionEvent event) {
        runTask("Inactivity check failed.", adminUserService::runInactivityCheck,
                result -> { statusLabel.setText(result.path("message").asText("Inactivity check completed.")); loadUsers(); });
    }

    @FXML
    private void handleWarning(ActionEvent event) {
        JsonNode user = selectedUser();
        if (user == null) return;
        Optional<String> reason = prompt("Issue Warning", "Reason", "Enter the warning reason:");
        if (reason.isEmpty() || reason.get().isBlank()) return;
        runTask("Issuing warning...", () -> adminUserService.warn(user.path("id").asInt(), reason.get().trim()),
                result -> { statusLabel.setText("Warning issued to " + user.path("name").asText() + "."); loadUsers(); });
    }

    @FXML
    private void handleBlacklist(ActionEvent event) {
        JsonNode user = selectedUser();
        if (user == null) return;
        Optional<String> reason = prompt("Blacklist User", "Reason", "Enter the blacklist reason (optional):");
        if (reason.isEmpty()) return;
        TextInputDialog durationDialog = new TextInputDialog("30");
        durationDialog.setTitle("Blacklist User");
        durationDialog.setHeaderText("Duration in days");
        durationDialog.setContentText("Enter a number of days:");
        Optional<String> duration = durationDialog.showAndWait();
        if (duration.isEmpty()) return;
        try {
            Integer days = Integer.valueOf(duration.get().trim());
            runTask("Blacklisting user...", () -> adminUserService.blacklist(user.path("id").asInt(), reason.get().trim(), days),
                    result -> { statusLabel.setText(user.path("name").asText() + " has been blacklisted."); loadUsers(); });
        } catch (NumberFormatException ex) {
            statusLabel.setText("Duration must be a number.");
        }
    }

    @FXML
    private void handleReinstate(ActionEvent event) {
        JsonNode user = selectedUser();
        if (user == null) return;
        runTask("Reinstating user...", () -> adminUserService.reinstate(user.path("id").asInt()),
                result -> { statusLabel.setText(user.path("name").asText() + " has been reinstated."); loadUsers(); });
    }

    private JsonNode selectedUser() {
        JsonNode user = usersTable.getSelectionModel().getSelectedItem();
        if (user == null) statusLabel.setText("Select a user first.");
        return user;
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
    private void handleStatistics(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mindshare/admin/ParticipationStatisticsView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) statisticsButton.getScene().getWindow();
            com.mindshare.utils.SceneUtils.switchScene(stage, root);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/mindshare/admin/AdminLandingView.fxml"));
            Stage stage = (Stage) backButton.getScene().getWindow();
            com.mindshare.utils.SceneUtils.switchScene(stage, root);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private Optional<String> prompt(String title, String header, String content) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(content);
        return dialog.showAndWait();
    }

    private interface IoAction { JsonNode run() throws Exception; }
    private interface SuccessAction { void accept(JsonNode value); }
}
