package com.mindshare.admin;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.util.List;

public class AdminDashboardController {

    @FXML private ListView<String> userListView;
    @FXML private Button warningButton;
    @FXML private Button blacklistButton;
    @FXML private Button statisticsButton;
    @FXML private Button backButton;

    @FXML
    public void initialize() {
        // TODO: replace with real GET /api/admin/users once route exists
        userListView.setItems(FXCollections.observableArrayList(
                "Noerine Namuganza - Active",
                "Joel Agaba - Active",
                "Justine Peace - 1 Warning"
        ));
    }

    @FXML
    private void handleWarning(ActionEvent event) {
        String selected = userListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        // TODO: POST /api/admin/users/{id}/warn (FR-04)
        System.out.println("Warning issued to: " + selected);
    }

    @FXML
    private void handleBlacklist(ActionEvent event) {
        String selected = userListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        // TODO: POST /api/admin/users/{id}/blacklist (FR-04)
        System.out.println("Blacklisted: " + selected);
    }

    @FXML
    private void handleStatistics(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mindshare/admin/ParticipationStatisticsView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) statisticsButton.getScene().getWindow();
            stage.setScene(com.mindshare.utils.SceneUtils.createStyledScene(root, 600, 420));
        } catch (Exception e) {
            e.printStackTrace();
        }
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
