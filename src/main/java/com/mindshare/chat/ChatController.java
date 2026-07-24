package com.mindshare.chat;

import com.fasterxml.jackson.databind.JsonNode;
import com.mindshare.api.GroupMessageService;
import com.mindshare.api.GroupService;
import com.mindshare.group.Group;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class ChatController {

    @FXML private ListView<Group> groupsListView;
    @FXML private Button browseGroupsButton;
    @FXML private Button backButton;
    @FXML private Label activeGroupLabel;
    @FXML private Button topicsButton;
    @FXML private ListView<String> chatListView;
    @FXML private TextField messageField;
    @FXML private Button sendButton;

    private int currentGroupId;

    private final GroupService groupService = new GroupService();
    private final GroupMessageService groupMessageService = new GroupMessageService();

    @FXML
    public void initialize() {
        loadUserGroups();

        groupsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldGroup, newGroup) -> {
            if (newGroup != null && newGroup.getId() != currentGroupId) {
                setGroupId(newGroup.getId());
            }
        });
    }

    public void setGroupId(int groupId) {
        this.currentGroupId = groupId;
        if (activeGroupLabel != null) {
            Group selected = groupsListView.getSelectionModel().getSelectedItem();
            activeGroupLabel.setText(selected != null ? selected.getName() : "Group #" + groupId);
        }
        loadChatMessages(groupId);
    }

    private void loadUserGroups() {
        Task<List<Group>> task = new Task<>() {
            @Override
            protected List<Group> call() throws Exception {
                return groupService.fetchMyGroups();
            }
        };

        task.setOnSucceeded(e -> {
            List<Group> groups = task.getValue();
            groupsListView.setItems(FXCollections.observableArrayList(groups));
            if (!groups.isEmpty()) {
                groupsListView.getSelectionModel().selectFirst();
                setGroupId(groups.get(0).getId());
            }
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            ex.printStackTrace();
            showError("Couldn't load your groups. Check your connection and try again.");
        });

        Thread thread = new Thread(task, "load-user-groups");
        thread.setDaemon(true);
        thread.start();
    }

    private void loadChatMessages(int groupId) {
        Task<JsonNode> task = new Task<>() {
            @Override
            protected JsonNode call() throws Exception {
                return groupMessageService.fetchMessages(groupId);
            }
        };

        task.setOnSucceeded(e -> {
            // Ignore a stale response if the user already switched groups again
            if (groupId != currentGroupId) return;
            chatListView.setItems(FXCollections.observableArrayList(parseMessages(task.getValue())));
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            ex.printStackTrace();
            showError("Couldn't load messages for this group.");
        });

        Thread thread = new Thread(task, "load-chat-messages-" + groupId);
        thread.setDaemon(true);
        thread.start();
    }

    private List<String> parseMessages(JsonNode root) {
        List<String> result = new ArrayList<>();
        if (root == null) return result;

        JsonNode items = root.isArray() ? root
                : root.has("data") ? root.path("data")
                  : root.has("messages") ? root.path("messages")
                    : root;

        if (!items.isArray()) return result;

        for (JsonNode item : items) {
            result.add(formatMessage(item));
        }
        return result;
    }

    private String formatMessage(JsonNode item) {
        String sender = firstNonBlank(
                item.path("user").path("name").asText(""),
                item.path("sender_name").asText(""),
                item.path("user_name").asText(""),
                item.path("author").asText("")
        );
        String content = firstNonBlank(
                item.path("message").asText(""),
                item.path("content").asText(""),
                item.path("text").asText("")
        );
        if (sender.isBlank()) return content;
        return sender + ": " + content;
    }

    private String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) return v;
        }
        return "";
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, message);
            alert.showAndWait();
        });
    }

    @FXML
    private void handleSend(ActionEvent event) {
        String message = messageField.getText();
        if (message != null && !message.isBlank() && currentGroupId != 0) {
            String trimmed = message.trim();
            Task<JsonNode> task = new Task<>() {
                @Override
                protected JsonNode call() throws Exception {
                    return groupMessageService.sendMessage(currentGroupId, trimmed);
                }
            };
            task.setOnSucceeded(e -> {
                chatListView.getItems().add("Me: " + trimmed);
                messageField.clear();
            });
            task.setOnFailed(e -> {
                task.getException().printStackTrace();
                showError("Message failed to send. Please try again.");
            });
            Thread thread = new Thread(task, "send-message");
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    private void handleViewTopics(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mindshare/group/GroupDetailView.fxml"));
            Parent root = loader.load();

            com.mindshare.group.GroupDetailController controller = loader.getController();
            // Pass group object to topics view
            // controller.setGroup(currentGroup);

            Stage stage = (Stage) topicsButton.getScene().getWindow();
            com.mindshare.utils.SceneUtils.switchScene(stage, root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBrowseGroups(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mindshare/group/BrowseGroupsView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) browseGroupsButton.getScene().getWindow();
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
}