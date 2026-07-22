package com.mindshare.chat;

import com.mindshare.auth.model.UserSession;
import com.fasterxml.jackson.databind.JsonNode;
import com.mindshare.api.GroupMessageService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class ChatController {

    @FXML private ListView<String> chatListView;
    @FXML private TextField messageField;
    @FXML private Button sendButton;
    @FXML private Button backButton;
    private final GroupMessageService groupMessageService = new GroupMessageService();
    private int currentGroupId = -1;

    @FXML
    public void initialize() {
        chatListView.setItems(FXCollections.observableArrayList());
    }

    public void setGroupId(int groupId) {
        this.currentGroupId = groupId;
        loadMessages();
    }

    private void loadMessages() {
        if (currentGroupId <= 0) {
            return;
        }

        javafx.concurrent.Task<List<String>> task = new javafx.concurrent.Task<>() {
            @Override
            protected List<String> call() throws Exception {
                JsonNode response = groupMessageService.fetchMessages(currentGroupId);
                JsonNode messagesNode = response.isArray() ? response : response.path("messages");
                List<String> messages = new ArrayList<>();
                if (messagesNode != null && messagesNode.isArray()) {
                    for (JsonNode node : messagesNode) {
                        String sender = node.path("sender_name").asText(node.path("user").path("name").asText("System"));
                        String body = node.path("message").asText(node.path("content").asText(""));
                        messages.add(sender + ": " + body);
                    }
                }
                return messages;
            }
        };

        task.setOnSucceeded(event -> chatListView.setItems(FXCollections.observableArrayList(task.getValue())));
        task.setOnFailed(event -> task.getException().printStackTrace());
        new Thread(task).start();
    }

    @FXML
    private void handleSend(ActionEvent event) {
        String text = messageField.getText();
        if (text.isBlank()) return;
        if (currentGroupId <= 0) return;

        javafx.concurrent.Task<JsonNode> task = new javafx.concurrent.Task<>() {
            @Override
            protected JsonNode call() throws Exception {
                return groupMessageService.sendMessage(currentGroupId, text);
            }
        };

        task.setOnSucceeded(workerEvent -> {
            chatListView.getItems().add(UserSession.getUserName() + ": " + text);
            messageField.clear();
        });
        task.setOnFailed(workerEvent -> task.getException().printStackTrace());
        new Thread(task).start();
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mindshare/dashboard/DashboardView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(com.mindshare.utils.SceneUtils.createStyledScene(root, 600, 420));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
