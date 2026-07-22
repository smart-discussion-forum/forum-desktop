package com.mindshare.group;

import com.mindshare.discussion.model.Topic;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.ArrayList;
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
    private Button groupChatButton;

    @FXML
    private Button backButton;

    private Group currentGroup;
    private List<Topic> currentTopics = new ArrayList<>();

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

    private final ObjectMapper objectMapper = new ObjectMapper();

    private void loadTopics() {
        javafx.concurrent.Task<List<Topic>> fetchTask = new javafx.concurrent.Task<>() {
            @Override
            protected List<Topic> call() throws Exception {
                JsonNode response = new com.mindshare.api.GroupTopicService().fetchTopicsRaw(currentGroup.getId());
                JsonNode topicsNode = response.isArray() ? response : response.path("topics");
                List<Topic> topics = new ArrayList<>();
                if (topicsNode != null && topicsNode.isArray()) {
                    for (JsonNode node : topicsNode) {
                        topics.add(objectMapper.treeToValue(node, Topic.class));
                    }
                }
                return topics;
            }
        };

        fetchTask.setOnSucceeded(event -> {
            currentTopics = fetchTask.getValue();
            topicListView.setItems(FXCollections.observableArrayList(currentTopics));
        });

        fetchTask.setOnFailed(event -> {
            fetchTask.getException().printStackTrace();
            // TODO: show a status label like MyGroupsController does
        });

        new Thread(fetchTask).start();
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        if (query.isBlank()) {
            topicListView.setItems(FXCollections.observableArrayList(currentTopics));
            return;
        }

        List<Topic> filtered = currentTopics.stream()
                .filter(topic -> topic.getTitle() != null && topic.getTitle().toLowerCase().contains(query))
                .toList();
        topicListView.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    private void handleCreateTopic(ActionEvent event) {
        if (currentGroup == null) {
            return;
        }

        TextInputDialog titleDialog = new TextInputDialog();
        titleDialog.setTitle("Create Topic");
        titleDialog.setHeaderText("Topic title");
        titleDialog.setContentText("Enter the new topic title:");

        java.util.Optional<String> titleResult = titleDialog.showAndWait();
        if (titleResult.isEmpty() || titleResult.get().isBlank()) {
            return;
        }

        TextInputDialog categoryDialog = new TextInputDialog();
        categoryDialog.setTitle("Create Topic");
        categoryDialog.setHeaderText("Topic category");
        categoryDialog.setContentText("Enter a category (optional):");

        java.util.Optional<String> categoryResult = categoryDialog.showAndWait();

        javafx.concurrent.Task<com.fasterxml.jackson.databind.JsonNode> task = new javafx.concurrent.Task<>() {
            @Override
            protected com.fasterxml.jackson.databind.JsonNode call() throws Exception {
                return new com.mindshare.api.GroupTopicService().createTopic(
                        currentGroup.getId(),
                        titleResult.get().trim(),
                        categoryResult.isPresent() ? categoryResult.get().trim() : null
                );
            }
        };

        task.setOnSucceeded(workerEvent -> loadTopics());
        task.setOnFailed(workerEvent -> task.getException().printStackTrace());
        new Thread(task).start();
    }

    @FXML
    private void handleGroupChat(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mindshare/chat/ChatView.fxml"));
            Parent root = loader.load();
            com.mindshare.chat.ChatController controller = loader.getController();
            controller.setGroupId(currentGroup.getId());
            Stage stage = (Stage) groupChatButton.getScene().getWindow();
            stage.setScene(com.mindshare.utils.SceneUtils.createStyledScene(root, 900, 650));
        } catch (Exception e) {
            e.printStackTrace();
        }
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
