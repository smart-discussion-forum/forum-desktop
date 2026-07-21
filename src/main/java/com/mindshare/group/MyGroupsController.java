package com.mindshare.group;

import com.mindshare.api.GroupService;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.util.List;

public class MyGroupsController {

        @FXML
        private ListView<Group> groupListView;

        @FXML
        private Label statusLabel;

        @FXML
        private Button backButton;

    private final GroupService groupService = new GroupService();

    @FXML
    public void initialize() {
        loadGroups();

        //To be removed after getting the real routes to groups from the backend team.
        Group fakeGroup = new Group();
        fakeGroup.setId(1);
        fakeGroup.setName("BSSE Year 2 - Group G22");
        fakeGroup.setDescription("Test group for UI development");
        groupListView.getItems().add(fakeGroup);

        groupListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // double-click to open
                Group selected = groupListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    openGroupDetail(selected);
                }
            }
        });
    }

    private void openGroupDetail(Group group) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/mindshare/group/GroupDetailView.fxml"));
            Parent groupDetailRoot = loader.load();

            GroupDetailController controller = loader.getController();
            controller.setGroup(group);

            Stage stage = (Stage) groupListView.getScene().getWindow();
            stage.setScene(com.mindshare.utils.SceneUtils.createStyledScene(groupDetailRoot, 600, 420));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadGroups() {
        Task<List<Group>> fetchTask = new Task<>() {
            @Override
            protected List<Group> call() throws Exception {
                return groupService.fetchMyGroups();
            }
        };

        fetchTask.setOnSucceeded(event -> {
            List<Group> groups = fetchTask.getValue();
            groupListView.setItems(FXCollections.observableArrayList(groups));
        });

        fetchTask.setOnFailed(event -> {
            statusLabel.setText("Could not load groups. Check your connection.");
            statusLabel.setVisible(true);
            fetchTask.getException().printStackTrace();
        });

        new Thread(fetchTask).start();
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/mindshare/dashboard/DashboardView.fxml"));
            Parent dashboardRoot = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(com.mindshare.utils.SceneUtils.createStyledScene(dashboardRoot, 600, 420));
        } catch (Exception e) {
            e.printStackTrace();
        }
        }
    }


