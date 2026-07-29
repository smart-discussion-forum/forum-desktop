package com.mindshare.chat;

import com.fasterxml.jackson.databind.JsonNode;
import com.mindshare.api.GroupMessageService;
import com.mindshare.api.GroupService;
import com.mindshare.group.Group;
import com.mindshare.auth.model.UserSession;
import javafx.application.Platform;
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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;

public class ChatController {

    @FXML private ListView<Group> groupsListView;
    @FXML private Button backButton;
    @FXML private Label activeGroupLabel;
    @FXML private Button topicsButton;
    @FXML private ListView<ChatMessage> chatListView;
    @FXML private TextField messageField;
    @FXML private Button sendButton;
    @FXML private MenuButton excludeMembersButton;

    private int currentGroupId;
    private final List<Integer> excludedUserIds = new ArrayList<>();

    private final GroupService groupService = new GroupService();
    private final GroupMessageService groupMessageService = new GroupMessageService();

    @FXML
    public void initialize() {
        loadUserGroups();
        setupChatCellFactory();
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
        loadGroupMembers(groupId);
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

    private void loadGroupMembers(int groupId) {
        Task<JsonNode> task = new Task<>() {
            @Override
            protected JsonNode call() throws Exception {
                return groupService.fetchGroupMembers(groupId);
            }
        };

        task.setOnSucceeded(e -> {
            System.out.println("Members JSON received: " + task.getValue());
            Platform.runLater(() -> populateExcludeMembersMenu(task.getValue()));
        });

        task.setOnFailed(e -> {
            System.err.println("Failed to fetch members for group " + groupId);
            task.getException().printStackTrace();
        });

        Thread thread = new Thread(task, "load-group-members-" + groupId);
        thread.setDaemon(true);
        thread.start();
    }

    private void populateExcludeMembersMenu(JsonNode root) {
        excludeMembersButton.getItems().clear();
        excludedUserIds.clear();
        updateMenuButtonText();

        if (root == null) {
            System.out.println("Members root JSON is null!");
            return;
        }

        JsonNode membersNode = root.isArray() ? root
                : root.has("members") ? root.path("members")
                  : root.has("users") ? root.path("users")
                    : root.has("data") ? root.path("data")
                      : root.has("group_members") ? root.path("group_members")
                        : root;

        if (!membersNode.isArray()) {
            System.out.println("No array found in members response shape: " + root.toString());
            return;
        }

        String currentUserName = UserSession.getUserName();

        for (JsonNode member : membersNode) {
            JsonNode userObj = member.has("user") ? member.path("user") : member;

            int id = userObj.path("id").asInt(userObj.path("user_id").asInt(0));
            String name = firstNonBlank(
                    userObj.path("name").asText(""),
                    userObj.path("username").asText(""),
                    userObj.path("full_name").asText("")
            );

            if (id == 0) continue;

            // Skip current user so they don't exclude themselves
            if (currentUserName != null && currentUserName.equalsIgnoreCase(name)) {
                continue;
            }

            CheckMenuItem item = new CheckMenuItem(name.isBlank() ? "User #" + id : name);

            item.setOnAction(e -> {
                if (item.isSelected()) {
                    excludedUserIds.add(id);
                } else {
                    excludedUserIds.remove(Integer.valueOf(id));
                }
                updateMenuButtonText();
            });

            excludeMembersButton.getItems().add(item);
        }
        System.out.println("Populated " + excludeMembersButton.getItems().size() + " members into menu.");
    }

    private void updateMenuButtonText() {
        int count = excludedUserIds.size();
        if (count == 0) {
            excludeMembersButton.setText("Exclude Members");
        } else {
            excludeMembersButton.setText("Exclude Members (" + count + ")");
        }
    }

    private void loadChatMessages(int groupId) {
        Task<JsonNode> task = new Task<>() {
            @Override
            protected JsonNode call() throws Exception {
                return groupMessageService.fetchMessages(groupId);
            }
        };

        task.setOnSucceeded(e -> {
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

    private List<ChatMessage> parseMessages(JsonNode root) {
        List<ChatMessage> result = new ArrayList<>();
        if (root == null) return result;

        JsonNode items = root.isArray() ? root
                : root.has("data") ? root.path("data")
                  : root.has("messages") ? root.path("messages")
                    : root;

        if (!items.isArray()) return result;
        String currentUserName = UserSession.getUserName();

        for (JsonNode item : items) {
            String sender = firstNonBlank(
                    item.path("sender").path("name").asText(""),
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
            boolean mine = currentUserName != null && currentUserName.equalsIgnoreCase(sender);
            result.add(new ChatMessage(sender, content, mine));
        }
        return result;
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
                    return groupMessageService.sendMessage(currentGroupId, trimmed, excludedUserIds);
                }
            };

            task.setOnSucceeded(e -> {
                chatListView.getItems().add(new ChatMessage(UserSession.getUserName(), trimmed, true));
                if (task.getValue().path("queued").asBoolean(false)) {
                    showInfo(task.getValue().path("message").asText("Message saved for later delivery."));
                }
                messageField.clear();

                for (MenuItem item : excludeMembersButton.getItems()) {
                    if (item instanceof CheckMenuItem checkItem) {
                        checkItem.setSelected(false);
                    }
                }
                excludedUserIds.clear();
                updateMenuButtonText();
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

    private void showInfo(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, message);
            alert.setHeaderText(null);
            alert.showAndWait();
        });
    }

    @FXML
    private void handleViewTopics(ActionEvent event) {
        Group selectedGroup = groupsListView.getSelectionModel().getSelectedItem();
        if (selectedGroup == null) {
            showError("Select a group first.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mindshare/group/GroupDetailView.fxml"));
            Parent root = loader.load();

            com.mindshare.group.GroupDetailController controller = loader.getController();
            controller.setGroup(selectedGroup);

            Stage stage = (Stage) topicsButton.getScene().getWindow();
            com.mindshare.utils.SceneUtils.switchScene(stage, root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupChatCellFactory() {
        chatListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(ChatMessage msg, boolean empty) {
                super.updateItem(msg, empty);
                if (empty || msg == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                Label content = new Label(msg.getContent());
                content.setWrapText(true);
                content.setMaxWidth(320);
                content.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");

                VBox bubble = new VBox(content);
                bubble.setPadding(new Insets(10, 14, 10, 14));
                bubble.setStyle("-fx-background-radius: 14; -fx-background-color: "
                        + (msg.isMine() ? "#3b82f6" : "#22c55e") + ";");

                VBox column = new VBox(4);
                if (!msg.isMine()) {
                    Label sender = new Label(msg.getSender());
                    sender.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 11px;");
                    column.getChildren().add(sender);
                } else {
                    Label youLabel = new Label("You");
                    youLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 11px;");
                    HBox youRow = new HBox(youLabel);
                    youRow.setAlignment(Pos.CENTER_RIGHT);
                    column.getChildren().add(youRow);
                }
                column.getChildren().add(bubble);
                column.setAlignment(msg.isMine() ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

                HBox row = new HBox(column);
                row.setAlignment(msg.isMine() ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
                row.setPadding(new Insets(4, 8, 4, 8));

                setGraphic(row);
                setText(null);
                setStyle("-fx-background-color: transparent;");
            }
        });
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
