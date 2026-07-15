package com.mindshare.dashboard;

import com.mindshare.auth.model.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class DashboardController {

        @FXML
        private Label welcomeLabel;

        @FXML
        private Button discussionButton;

        @FXML
        private Button quizButton;

        @FXML
        private Button statisticsButton;

        @FXML
        private Button notificationsButton;

        @FXML
        public void initialize() {
            welcomeLabel.setText("Welcome, " + UserSession.getUserName() + " (" + UserSession.getUserRole() + ")");
        }

        @FXML
        private void handleDiscussions(ActionEvent event) {
            System.out.println("Discussions clicked - screen not built yet");
        }

        @FXML
        private void handleQuiz(ActionEvent event) {
            System.out.println("Quiz clicked - screen not built yet");
        }

        @FXML
        private void handleStatistics(ActionEvent event) {
            System.out.println("Statistics clicked - screen not built yet");
        }

        @FXML
        private void handleNotifications(ActionEvent event) {
            System.out.println("Notifications clicked - screen not built yet");
        }
    }

