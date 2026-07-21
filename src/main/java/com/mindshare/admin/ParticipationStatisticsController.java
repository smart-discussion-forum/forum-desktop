package com.mindshare.admin;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ParticipationStatisticsController {
    @FXML private TableView<UserStat> statsTable;
    @FXML private TableColumn<UserStat, String> userColumn;
    @FXML private TableColumn<UserStat, String> postsColumn;
    @FXML private TableColumn<UserStat, String> scoreColumn;
    @FXML private TextField searchField;
    @FXML private Button exportButton;
    @FXML private Button backButton;

    @FXML
    public void initialize() {
        userColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().name));
        postsColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().posts)));
        scoreColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().score)));

        // TODO: replace with real GET /api/admin/participation-stats
        statsTable.setItems(FXCollections.observableArrayList(
                new UserStat("Noerine Namuganza", 12, 85),
                new UserStat("Joel Agaba", 8, 70),
                new UserStat("Justine Peace", 15, 92)
        ));
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        System.out.println("Search not yet implemented for: " + searchField.getText());
    }

    @FXML
    private void handleExport(ActionEvent event) {
        System.out.println("Export not yet implemented");
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mindshare/admin/AdminDashboardView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(com.mindshare.utils.SceneUtils.createStyledScene(root, 600, 420));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class UserStat {
        String name; int posts; int score;
        UserStat(String name, int posts, int score) {
            this.name = name; this.posts = posts; this.score = score;
        }
    }
}
