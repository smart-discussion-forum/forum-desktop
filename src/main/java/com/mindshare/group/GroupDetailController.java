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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GroupDetailController {

    @FXML private Label groupNameLabel;
    @FXML private TextField searchField;
    @FXML private ListView<Topic> topicListView;
    @FXML private Button createTopicButton;
    @FXML private Button groupChatButton;
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
        // Format the topics to display cleanly like on the web: "Title [Category] (X posts)"
        topicListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Topic topic, boolean empty) {
                super.updateItem(topic, empty);
                if (empty || topic == null) {
                    setText(null);
                } else {
                    String categoryStr = (topic.getCategory() != null && !topic.getCategory().isBlank())
                            ? " [" + topic.getCategory() + "]"
                            : "";
                    int postCount = topic.getPostsCount();
                    setText(topic.getTitle() + categoryStr + " (" + postCount + " posts)");
                }
            }
        });

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
            com.mindshare.utils.SceneUtils.switchScene(stage, topicDetailRoot);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadTopics() {
        if (currentGroup == null) return;

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
            topicListView.setItems(FXCollections.observableArrayList(currentTopics));
        });

        fetchTask.setOnFailed(event -> fetchTask.getException().printStackTrace());

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
        if (currentGroup == null) return;

        // Prompt 1: Topic Title
        TextInputDialog titleDialog = new TextInputDialog();
        titleDialog.setTitle("Create New Topic");
        titleDialog.setHeaderText("Create Topic for " + currentGroup.getName());
        titleDialog.setContentText("Topic title:");

        Optional<String> titleResult = titleDialog.showAndWait();
        if (titleResult.isEmpty() || titleResult.get().isBlank()) {
            return;
        }

        // Prompt 2: Optional Category
        TextInputDialog categoryDialog = new TextInputDialog();
        categoryDialog.setTitle("Create New Topic");
        categoryDialog.setHeaderText("Add Category");
        categoryDialog.setContentText("Category (optional):");

        Optional<String> categoryResult = categoryDialog.showAndWait();
        String category = categoryResult.isPresent() ? categoryResult.get().trim() : null;

        Task<JsonNode> task = new Task<>() {
            @Override
            protected JsonNode call() throws Exception {
                return new com.mindshare.api.GroupTopicService().createTopic(
                        currentGroup.getId(),
                        titleResult.get().trim(),
                        category
                );
            }
        };

        task.setOnSucceeded(workerEvent -> loadTopics()); // Refresh list upon creation
        task.setOnFailed(workerEvent -> workerEvent.getSource().getException().printStackTrace());
        new Thread(task).start();
    }

    @FXML
    private void handleGroupChat(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mindshare/chat/ChatView.fxml"));
            Parent root = loader.load();

            com.mindshare.chat.ChatController controller = loader.getController();
            if (currentGroup != null) {
                controller.setGroupId(currentGroup.getId());
            }

            Stage stage = (Stage) groupChatButton.getScene().getWindow();
            com.mindshare.utils.SceneUtils.switchScene(stage, root);
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
            com.mindshare.utils.SceneUtils.switchScene(stage, groupsRoot);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}