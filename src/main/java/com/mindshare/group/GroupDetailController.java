package com.mindshare.group;

import com.mindshare.discussion.model.Topic;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableCell;
import java.util.ArrayList;
import java.util.List;

public class GroupDetailController {

    @FXML private Label groupNameLabel;
    @FXML private TableView<Topic> topicTableView;
    @FXML private Label statusLabel;
    @FXML private TableColumn<Topic, String> topicColumn;
    @FXML private TableColumn<Topic, String> categoryColumn;
    @FXML private TableColumn<Topic, String> creatorColumn;
    @FXML private TableColumn<Topic, Number> repliesColumn;
    @FXML private TableColumn<Topic, String> latestColumn;
    @FXML private TableColumn<Topic, Void> actionColumn;
    @FXML private Button createTopicButton;
    @FXML private Button backButton;

    private Group currentGroup;
    private List<Topic> currentTopics = new ArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void setGroup(Group group) {
        this.currentGroup = group;
        if (groupNameLabel != null && group != null) {
            groupNameLabel.setText(group.getName());
        }
        setupTopicListView();
        loadTopics();
    }

    private void setupTopicListView() {
        topicColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getTitle()));

        categoryColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getCategory() != null ? data.getValue().getCategory() : "General"));

        creatorColumn.setCellValueFactory(data -> {
            Topic.Creator creator = data.getValue().getCreator();
            return new javafx.beans.property.SimpleStringProperty(creator != null ? creator.getName() : "Unknown");
        });

        repliesColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleIntegerProperty(data.getValue().getPostsCount()));

        latestColumn.setCellValueFactory(data -> {
            Topic.LatestPost latest = data.getValue().getLatestPost();
            return new javafx.beans.property.SimpleStringProperty(latest != null ? latest.getContent() : "No replies yet");
        });

        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button openButton = new Button("Open Thread");

            {
                openButton.setOnAction(e -> {
                    Topic topic = getTableView().getItems().get(getIndex());
                    openTopicDetail(topic);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : openButton);
            }
        });

        topicTableView.setRowFactory(tv -> {
            TableRow<Topic> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1 && !row.isEmpty()) {
                    openTopicDetail(row.getItem());
                }
            });
            return row;
        });
    }
        private void openTopicDetail(Topic topic) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/mindshare/discussion/TopicDetailView.fxml"));
            Parent topicDetailRoot = loader.load();

            com.mindshare.discussion.controller.TopicDetailController controller = loader.getController();
            controller.setTopic(topic);
            controller.setGroup(currentGroup);


            Stage stage = (Stage) topicTableView.getScene().getWindow();
            com.mindshare.utils.SceneUtils.switchScene(stage, topicDetailRoot);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadTopics() {
        if (currentGroup == null) return;
        if (statusLabel != null) {
            statusLabel.setText("Loading topics...");
        }

        Task<List<Topic>> fetchTask = new Task<>() {
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
            topicTableView.setItems(FXCollections.observableArrayList(currentTopics));
        });

        fetchTask.setOnFailed(event -> fetchTask.getException().printStackTrace());

        new Thread(fetchTask).start();
    }

    @FXML
    private void handleCreateTopic(ActionEvent event) {
        if (currentGroup == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mindshare/group/CreateTopicView.fxml"));
            Parent root = loader.load();

            CreateTopicController controller = loader.getController();
            controller.setGroup(currentGroup);

            Stage stage = (Stage) createTopicButton.getScene().getWindow();
            com.mindshare.utils.SceneUtils.switchScene(stage, root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/mindshare/chat/ChatView.fxml"));
            Parent root = loader.load();

            com.mindshare.chat.ChatController controller = loader.getController();
            if (currentGroup != null) {
                controller.setGroupId(currentGroup.getId());
            }

            Stage stage = (Stage) backButton.getScene().getWindow();
            com.mindshare.utils.SceneUtils.switchScene(stage, root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}