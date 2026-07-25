package com.mindshare.group;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CreateTopicController {
    @FXML private Label titleLabel;
    @FXML private TextField titleField;
    @FXML private TextField categoryField;
    @FXML private Label statusLabel;
    @FXML private Button saveButton;
    @FXML private Button backToTopicsButton;
    @FXML private Button backToChatButton;

    private Group currentGroup;
    private final com.mindshare.api.GroupTopicService topicService = new com.mindshare.api.GroupTopicService();

    public void setGroup(Group group) {
        this.currentGroup = group;
        if (titleLabel != null && group != null) {
            titleLabel.setText("New Topic in " + group.getName());
        }
    }

    @FXML
    private void handleSave(ActionEvent event) {
        if (currentGroup == null) return;

        String title = titleField.getText() == null ? "" : titleField.getText().trim();
        String category = categoryField.getText() == null ? "" : categoryField.getText().trim();

        if (title.isBlank()) {
            showStatus("Title is required.");
            return;
        }

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                topicService.createTopic(currentGroup.getId(), title, category.isBlank() ? null : category);
                return null;
            }
        };

        task.setOnSucceeded(e -> goBackToTopics());
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            showStatus(ex != null && ex.getMessage() != null ? ex.getMessage() : "Could not create topic.");
            if (ex != null) ex.printStackTrace();
        });

        new Thread(task).start();
    }

    @FXML
    private void handleBackToTopics(ActionEvent event) {
        goBackToTopics();
    }

    private void goBackToTopics() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mindshare/group/GroupDetailView.fxml"));
            Parent root = loader.load();

            GroupDetailController controller = loader.getController();
            controller.setGroup(currentGroup);

            Stage stage = (Stage) saveButton.getScene().getWindow();
            com.mindshare.utils.SceneUtils.switchScene(stage, root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBackToChat(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mindshare/chat/ChatView.fxml"));
            Parent root = loader.load();

            com.mindshare.chat.ChatController controller = loader.getController();
            if (currentGroup != null) {
                controller.setGroupId(currentGroup.getId());
            }

            Stage stage = (Stage) backToChatButton.getScene().getWindow();
            com.mindshare.utils.SceneUtils.switchScene(stage, root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showStatus(String msg) {
        statusLabel.setText(msg);
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);
    }
}

