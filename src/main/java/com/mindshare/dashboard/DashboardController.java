package com.mindshare.dashboard;

import com.mindshare.api.GroupService;
import com.mindshare.auth.model.UserSession;
import com.mindshare.group.CreateTopicController;
import com.mindshare.group.Group;
import com.mindshare.group.GroupDetailController;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class DashboardController {
    @FXML private Label titleLabel;
    @FXML private Label welcomeLabel;
    @FXML private Label actionsTitle;
    @FXML private Label statusLabel;
    @FXML private VBox lecturerGroupsPanel;
    @FXML private FlowPane lecturerGroupChips;
    @FXML private FlowPane studentActions;
    @FXML private FlowPane lecturerActions;
    @FXML private ComboBox<Group> groupSelector;
    @FXML private Button groupChatButton;
    @FXML private Button quizButton;
    @FXML private Button recommendationsButton;

    private final GroupService groupService = new GroupService();
    private boolean lecturer;

    @FXML
    public void initialize() {
        lecturer = "lecturer".equalsIgnoreCase(UserSession.getUserRole());
        titleLabel.setText(lecturer ? "Lecturer Dashboard" : "Dashboard");
        welcomeLabel.setText(lecturer
                ? "Welcome back, " + UserSession.getUserName() + ". You're signed in as Lecturer."
                : "Choose a section to continue.");
        actionsTitle.setText("Lecturer actions");
        actionsTitle.setVisible(lecturer);
        actionsTitle.setManaged(lecturer);

        studentActions.setVisible(!lecturer);
        studentActions.setManaged(!lecturer);
        lecturerGroupsPanel.setVisible(lecturer);
        lecturerGroupsPanel.setManaged(lecturer);
        lecturerActions.setVisible(lecturer);
        lecturerActions.setManaged(lecturer);

        if (lecturer) loadGroups();
    }

    private void loadGroups() {
        showStatus("Loading your groups…");
        Task<List<Group>> task = new Task<>() {
            @Override protected List<Group> call() throws Exception { return groupService.fetchMyGroups(); }
        };
        task.setOnSucceeded(event -> displayGroups(task.getValue()));
        task.setOnFailed(event -> {
            Throwable error = task.getException();
            showStatus(error != null && error.getMessage() != null
                    ? error.getMessage() : "Could not load your groups.");
        });
        Thread thread = new Thread(task, "dashboard-groups-loader");
        thread.setDaemon(true);
        thread.start();
    }

    private void displayGroups(List<Group> groups) {
        List<Group> safeGroups = groups == null ? List.of() : groups;
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);

        if (lecturer) {
            lecturerGroupChips.getChildren().clear();
            for (Group group : safeGroups) {
                Label chip = new Label(group.getName());
                chip.getStyleClass().add("dashboard-group-chip");
                lecturerGroupChips.getChildren().add(chip);
            }
            groupSelector.setItems(FXCollections.observableArrayList(safeGroups));
            if (!safeGroups.isEmpty()) groupSelector.getSelectionModel().selectFirst();
            if (safeGroups.isEmpty()) showStatus("No groups yet. Use + New group to create one.");
        }
    }

    @FXML private void handleBrowseGroups(ActionEvent event) { navigate("/com/mindshare/group/BrowseGroupsView.fxml", event); }
    @FXML private void handleNewGroup(ActionEvent event) {
        if (!lecturer) {
            showStatus("Only lecturers can create groups.");
            return;
        }
        navigate("/com/mindshare/group/CreateGroupView.fxml", event);
    }
    @FXML private void handleGroupChat(ActionEvent event) { navigate("/com/mindshare/group/MyGroupsView.fxml", event); }
    @FXML private void handleQuiz(ActionEvent event) { navigate("/com/mindshare/quiz/QuizListView.fxml", event); }
    @FXML private void handleRecommendations(ActionEvent event) { navigate("/com/mindshare/recommendation/RecommendationView.fxml", event); }
    @FXML private void handleProfile(ActionEvent event) { navigate("/com/mindshare/profile/ProfileView.fxml", event); }
    @FXML private void handleParticipation(ActionEvent event) { navigate("/com/mindshare/admin/ParticipationStatisticsView.fxml", event); }
    @FXML private void handleScheduleQuiz(ActionEvent event) { navigate("/com/mindshare/quiz/QuizConfigurationView.fxml", event); }

    @FXML
    private void handleCreateTopic(ActionEvent event) {
        Group group = selectedGroup();
        if (group == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mindshare/group/CreateTopicView.fxml"));
            Parent root = loader.load();
            loader.<CreateTopicController>getController().setGroup(group);
            switchScene(root, event);
        } catch (Exception error) { showNavigationError(error); }
    }

    @FXML private void handleViewTopics(ActionEvent event) {
        Group group = selectedGroup();
        if (group != null) openGroup(group);
    }

    private Group selectedGroup() {
        Group group = groupSelector.getSelectionModel().getSelectedItem();
        if (group == null) showStatus("Choose a group first.");
        return group;
    }

    private void openGroup(Group group) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mindshare/group/GroupDetailView.fxml"));
            Parent root = loader.load();
            loader.<GroupDetailController>getController().setGroup(group);
            Stage stage = (Stage) titleLabel.getScene().getWindow();
            com.mindshare.utils.SceneUtils.switchScene(stage, root);
        } catch (Exception error) { showNavigationError(error); }
    }

    private void navigate(String path, ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            switchScene(root, event);
        } catch (Exception error) { showNavigationError(error); }
    }

    private void switchScene(Parent root, ActionEvent event) {
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        com.mindshare.utils.SceneUtils.switchScene(stage, root);
    }

    private void showStatus(String message) {
        statusLabel.setText(message);
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);
    }

    private void showNavigationError(Exception error) {
        error.printStackTrace();
        showStatus("Could not open that screen: " + error.getMessage());
    }
}
