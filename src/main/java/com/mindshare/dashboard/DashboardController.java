package com.mindshare.dashboard;

import com.mindshare.auth.model.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;


public class DashboardController {

        @FXML
        private Label welcomeLabel;

        @FXML
        private Button groupChatButton;

        @FXML
        private Button quizButton;

        @FXML
        private Button recommendationsButton;

        @FXML
        private Button statisticsButton;

        @FXML
        private Button profileButton;

        @FXML
        public void initialize() {
            welcomeLabel.setText("Welcome, " + UserSession.getUserName() + " (" + UserSession.getUserRole() + ")");
            String role = UserSession.getUserRole();

                boolean isAdmin = role.equalsIgnoreCase("admin");
                boolean isLecturer = role.equalsIgnoreCase("lecturer");
                boolean isStudent = role.equalsIgnoreCase("student");

            //Statistics Button visible to admins
                statisticsButton.setVisible(isAdmin);
                statisticsButton.setManaged(isAdmin);

                //Quiz button label (lectures configure, students take)
               quizButton.setVisible(isStudent || isLecturer);
               quizButton.setManaged(isStudent || isLecturer);

                if (isLecturer) {
                        quizButton.setText("Create Quiz");
                }
        }
        @FXML
        private void handleGroupChat(ActionEvent event) {
                navigateTo("/com/mindshare/group/MyGroupsView.fxml", groupChatButton);
        }

        @FXML
        private void handleQuiz(ActionEvent event) {
                String role = UserSession.getUserRole();
                String fxmlPath = role.equalsIgnoreCase("lecturer")
                        ? "/com/mindshare/quiz/QuizConfigurationView.fxml"
                        : "/com/mindshare/quiz/QuizListView.fxml";
                navigateTo(fxmlPath, quizButton);
        }

        @FXML
        private void handleRecommendations(ActionEvent event) {
                navigateTo("/com/mindshare/recommendation/RecommendationView.fxml", recommendationsButton);
        }

        @FXML
        private void handleStatistics(ActionEvent event) {
                navigateTo("/com/mindshare/admin/AdminDashboardView.fxml", statisticsButton);
        }

        @FXML
        private void handleProfile(ActionEvent event) {
                navigateTo("/com/mindshare/profile/ProfileView.fxml", profileButton);
        }

        private void navigateTo(String fxmlPath, Button sourceButton) {
                try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                        Parent root = loader.load();
                        Stage stage = (Stage) sourceButton.getScene().getWindow();
                        com.mindshare.utils.SceneUtils.switchScene(stage, root);
                } catch (Exception e) {
                        e.printStackTrace();
                }
        }
}



