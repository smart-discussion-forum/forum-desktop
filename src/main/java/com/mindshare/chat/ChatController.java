package com.mindshare.chat;

import com.mindshare.auth.model.UserSession;
import com.mindshare.database.CachedMessage;
import com.mindshare.database.SQLiteConnection;
import com.mindshare.sync.NetworkMonitor;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChatController {

    @FXML private ListView<String> chatListView;
    @FXML private TextField messageField;
    @FXML private Button sendButton;
    @FXML private Button backButton;

    @FXML
    public void initialize() {
        chatListView.setItems(FXCollections.observableArrayList(
                "System: Welcome to the group chat"
        ));
    }

    @FXML
    private void handleSend(ActionEvent event) {
        String text = messageField.getText();
        if (text.isBlank()) return;

        String sentAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        chatListView.getItems().add(UserSession.getUserName() + ": " + text);
        messageField.clear();

        //The offline caching ties directly with this, if the server is unreachable, the message is saved locally.
        if (!NetworkMonitor.isOnline()) {
            SQLiteConnection.insertCachedMessage(
                    new CachedMessage(1, 5, text, sentAt) // TODO: use real user/group IDs
            );
        }
        // TODO: if online, POST to /api/groups/{id}/messages instead
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