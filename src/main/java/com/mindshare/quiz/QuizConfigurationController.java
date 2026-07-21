package com.mindshare.quiz;

import com.mindshare.api.GroupService;
import com.mindshare.group.Group;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.util.List;

public class QuizConfigurationController {

    @FXML private TextField titleField;
    @FXML private TextField dateField;
    @FXML private TextField durationField;
    @FXML private ComboBox<Group> categoryBox;
    @FXML private Label statusLabel;
    @FXML private Button saveButton;
    @FXML private Button backButton;

    private final GroupService groupService = new GroupService();

    @FXML
    public void initialize() {
        loadGroups() ;
    }

    private void loadGroups() {
        Task<List<Group>> fetchTask = new Task<>() {
            @Override
            protected List<Group> call() throws Exception {
                return groupService.fetchMyGroups();
            }
        };

        fetchTask.setOnSucceeded(event -> {
            categoryBox.setItems(FXCollections.observableArrayList(fetchTask.getValue()));
        });

        fetchTask.setOnFailed(event -> {
            //TODO: remove fallback once /api/groups is live
            Group placeholder = new Group();
            placeholder.setId(1);
            placeholder.setName("BSSE Year 2 - Group G22");
            categoryBox.setItems(FXCollections.observableArrayList(placeholder));
            statusLabel.setText("Could not load real groups - showing placeholder.");
            statusLabel.setVisible(true);
        });
        new Thread(fetchTask).start();
    }

    @FXML
    private void handleSave(ActionEvent event) {
        if (titleField.getText().isBlank() || durationField.getText().isBlank() || categoryBox.getValue() == null) {
            statusLabel.setText("Title, and duration and target group are required.");
            statusLabel.setStyle("-fx-text-fill: red");
            statusLabel.setVisible(true);
            return;
        }
        Group targetGroup = categoryBox.getValue();

        // TODO: POST to real "create quiz" endpoint once lecturer-facing route exists
        System.out.println("Quiz configured: " + titleField.getText() + ", " + durationField.getText() + " min, target group: " + targetGroup.getName() + "(id=" + targetGroup.getId() + ")");
        statusLabel.setText("Quiz saved (placeholder - not yet sent to server).");
        statusLabel.setStyle("-fx-text-fill: green;");
        statusLabel.setVisible(true);
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
