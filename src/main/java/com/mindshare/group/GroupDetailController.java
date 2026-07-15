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
        loadPlaceholderTopics();

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
            stage.setScene(new Scene(topicDetailRoot, 600, 420));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

        private void loadPlaceholderTopics() {
        // TODO: replace with a real API call once /api/groups/{id}/topics exists
        List<Topic> placeholderTopics = List.of(
                new Topic(1, "Assignment 2 clarification", "Coursework"),
                new Topic(2, "Best resources for OOP revision", "General"),
                new Topic(3, "Project deadline reminder", "Announcements")
        );
        topicListView.setItems(FXCollections.observableArrayList(placeholderTopics));
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
            stage.setScene(new Scene(groupsRoot, 600, 420));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}