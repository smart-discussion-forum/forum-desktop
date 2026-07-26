package com.mindshare.admin;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class AdminLandingController {
    @FXML private Button manageUsersButton;
    @FXML private Button statisticsButton;
    @FXML private Button manageGroupsButton;

    @FXML
    private void handleManageUsers(ActionEvent event) {
        navigate("/com/mindshare/admin/AdminDashboardView.fxml", manageUsersButton);
    }

    @FXML
    private void handleStatistics(ActionEvent event) {
        navigate("/com/mindshare/admin/ParticipationStatisticsView.fxml", statisticsButton);
    }

    @FXML
    private void handleManageGroups(ActionEvent event) {
        navigate("/com/mindshare/group/MyGroupsView.fxml", manageGroupsButton);
    }

    private void navigate(String path, Button source) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            Stage stage = (Stage) source.getScene().getWindow();
            com.mindshare.utils.SceneUtils.switchScene(stage, root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
