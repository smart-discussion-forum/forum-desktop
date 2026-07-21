package com.mindshare.group;

import com.mindshare.discussion.model.Topic;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import com.mindshare.api.TopicService;

import java.util.List;

public class GroupDetailController {

    @FXML
    private Label groupNameLabel;

    @FXML
    private TextField searchField;

    @FXML
    private ListView<Topic> topicListView;

    @FXML
    private Button createTopicButton;

    @FXML
    private Button backButton;

    private Group currentGroup;

    //Called from MyGroupsController right after loading this screen. The controller needs to be told explicitly which group was clicked.
    public void setGroup(Group group) {
        this.currentGroup = group;
        groupNameLabel.setText(group.getName());
        loadTopics();

        topicListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Topic selected = topicListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    openTopicDetail(selected);
                }
            }
        });
    }
    private void openTopicDetail(Topic topic) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/mindshare/discussion/TopicDetailView.fxml"));
            Parent topicDetailRoot = loader.load();

            com.mindshare.discussion.controller.TopicDetailController controller = loader.getController();
            controller.setTopic(topic);

            Stage stage = (Stage) topicListView.getScene().getWindow();
            stage.setScene(com.mindshare.utils.SceneUtils.createStyledScene(topicDetailRoot, 600, 420));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final TopicService topicService = new TopicService();

    private void loadTopics() {
        javafx.concurrent.Task<List<Topic>> fetchTask = new javafx.concurrent.Task<>() {
            @Override
            protected List<Topic> call() throws Exception {
                return topicService.fetchTopics(currentGroup.getId());
            }
        };

        fetchTask.setOnSucceeded(event -> {
            topicListView.setItems(FXCollections.observableArrayList(fetchTask.getValue()));
        });

        fetchTask.setOnFailed(event -> {
            fetchTask.getException().printStackTrace();
            // TODO: show a status label like MyGroupsController does
        });

        new Thread(fetchTask).start();
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        System.out.println("Search clicked - not yet implemented");
    }

    @FXML
    private void handleCreateTopic(ActionEvent event) {
        System.out.println("Create Topic clicked - not yet implemented");
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/mindshare/group/MyGroupsView.fxml"));
            Parent groupsRoot = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(com.mindshare.utils.SceneUtils.createStyledScene(groupsRoot, 600, 420));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}