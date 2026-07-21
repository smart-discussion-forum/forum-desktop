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
                        quizButton.setText("Configure Quiz");
                        // to be linked to a Quiz configuration screen once built
                }

        }

        @FXML
        private void handleDiscussions(ActionEvent event) {
                try {
                        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                                getClass().getResource("/com/mindshare/group/MyGroupsView.fxml"));
                        javafx.scene.Parent groupsRoot = loader.load();
                        javafx.stage.Stage stage = (javafx.stage.Stage) discussionButton.getScene().getWindow();
                        stage.setScene(new javafx.scene.Scene(groupsRoot, 600, 420));
                } catch (Exception e) {
                        e.printStackTrace();
                }
        }

        @FXML
        private void handleQuiz(ActionEvent event) {
                try {
                        String role = UserSession.getUserRole();
                        String fxmlPath = role.equalsIgnoreCase("lecturer")
                                ? "/com/mindshare/quiz/QuizConfigurationView.fxml"
                                : "/com/mindshare/quiz/QuizTakingView.fxml";

                        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(fxmlPath));
                        javafx.scene.Parent root = loader.load();

                        if (!role.equalsIgnoreCase("lecturer")) {
                                com.mindshare.quiz.QuizTakingController controller = loader.getController();
                                controller.setQuiz(new com.mindshare.quiz.Quiz(1, "Sample Quiz - OOP Concepts", 1));
                        }

                        javafx.stage.Stage stage = (javafx.stage.Stage) quizButton.getScene().getWindow();
                        stage.setScene(com.mindshare.utils.SceneUtils.createStyledScene(root, 600, 420));
        }

                catch (Exception e) {
                        e.printStackTrace();
                }

        }

        @FXML
        private void handleStatistics(ActionEvent event) {
                try {
                        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/mindshare/admin/AdminDashboardView.fxml"));
                        javafx.scene.Parent root = loader.load();
                        javafx.stage.Stage stage = (javafx.stage.Stage) statisticsButton.getScene().getWindow();
                        stage.setScene(com.mindshare.utils.SceneUtils.createStyledScene(root, 600, 420));
                } catch (Exception e) {
                        e.printStackTrace();
                }
        }

        @FXML
        private void handleNotifications(ActionEvent event) {
                try {
                        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/mindshare/chat/ChatView.fxml"));
                        javafx.scene.Parent root = loader.load();
                        javafx.stage.Stage stage = (javafx.stage.Stage) notificationsButton.getScene().getWindow();
                        stage.setScene(new javafx.scene.Scene(root, 600, 420));
                } catch (Exception e) {
                        e.printStackTrace();
                }
        }
    }

