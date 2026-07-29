package com.mindshare.navigation;

import com.mindshare.auth.model.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class NavigationBarController {
    @FXML private Button manageUsersButton;
    @FXML private Button statisticsButton;
    @FXML private Button participationButton;

    @FXML public void initialize() {
        String role = UserSession.getUserRole();
        setVisible(manageUsersButton, "admin".equalsIgnoreCase(role));
        setVisible(statisticsButton, "admin".equalsIgnoreCase(role));
        setVisible(participationButton, "lecturer".equalsIgnoreCase(role));
    }

    @FXML private void handleDashboard(ActionEvent e) {
        navigate("admin".equalsIgnoreCase(UserSession.getUserRole())
                ? "/com/mindshare/admin/AdminLandingView.fxml"
                : "/com/mindshare/dashboard/DashboardView.fxml", source(e));
    }
    @FXML private void handleGroups(ActionEvent e) { navigate("/com/mindshare/group/BrowseGroupsView.fxml", source(e)); }
    @FXML private void handleQuiz(ActionEvent e) {
        navigate("/com/mindshare/quiz/QuizListView.fxml", source(e));
    }
    @FXML private void handleRecommended(ActionEvent e) { navigate("/com/mindshare/recommendation/RecommendationView.fxml", source(e)); }
    @FXML private void handleManageUsers(ActionEvent e) { navigate("/com/mindshare/admin/AdminDashboardView.fxml", source(e)); }
    @FXML private void handleStatistics(ActionEvent e) { navigate("/com/mindshare/admin/ParticipationStatisticsView.fxml", source(e)); }
    @FXML private void handleParticipation(ActionEvent e) { navigate("/com/mindshare/lecturer/LecturerGroupPickerView.fxml", source(e)); }

    private Button source(ActionEvent e) { return (Button) e.getSource(); }
    private void navigate(String path, Button source) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            Stage stage = (Stage) source.getScene().getWindow();
            com.mindshare.utils.SceneUtils.switchScene(stage, root);
        } catch (Exception ex) { ex.printStackTrace(); }
    }
    private void setVisible(Button button, boolean visible) { button.setVisible(visible); button.setManaged(visible); }
}
