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
        private Button profileButton;

        @FXML
        public void initialize() {
            welcomeLabel.setText("Welcome, " + UserSession.getUserName() + " (" + UserSession.getUserRole() + ")");
            String role = UserSession.getUserRole();

                boolean isLecturer = role.equalsIgnoreCase("lecturer");
                boolean isStudent = role.equalsIgnoreCase("student");

                //Everyone opens the quiz list; lecturers can create from there.
               quizButton.setVisible(isStudent || isLecturer);
               quizButton.setManaged(isStudent || isLecturer);

                if (isLecturer) {
                        quizButton.setText("Quizzes");
                }
        }
        @FXML
        private void handleGroupChat(ActionEvent event) {
                navigateTo("/com/mindshare/group/MyGroupsView.fxml", groupChatButton);
        }

        @FXML
        private void handleQuiz(ActionEvent event) {
                navigateTo("/com/mindshare/quiz/QuizListView.fxml", quizButton);
        }

        @FXML
        private void handleRecommendations(ActionEvent event) {
                navigateTo("/com/mindshare/recommendation/RecommendationView.fxml", recommendationsButton);
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



