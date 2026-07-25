package com.mindshare.recommendation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindshare.api.RecommendationServiceClient;
import com.mindshare.discussion.controller.TopicDetailController;
import com.mindshare.discussion.model.Topic;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class RecommendationController {

    @FXML private Label statusLabel;
    @FXML private Label personalizedCountLabel;
    @FXML private Label trendingCountLabel;
    @FXML private FlowPane personalizedContainer;
    @FXML private FlowPane trendingContainer;
    @FXML private Button refreshButton;
    @FXML private Button backButton;

    private final RecommendationServiceClient recommendationService = new RecommendationServiceClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @FXML
    public void initialize() {
        loadRecommendations();
    }

    private void loadRecommendations() {
        statusLabel.setText("Loading recommendations...");

        Task<CombinedResult> task = new Task<>() {
            @Override
            protected CombinedResult call() throws Exception {
                JsonNode response = recommendationService.fetchCombinedRecommendations();

                List<RecommendationItem> personalized = extractItems(response.path("personalized"));
                List<RecommendationItem> trending = extractItems(response.path("trending"));

                return new CombinedResult(personalized, trending);
            }

            private List<RecommendationItem> extractItems(JsonNode arrayNode) throws Exception {
                List<RecommendationItem> items = new ArrayList<>();
                if (arrayNode.isArray()) {
                    for (JsonNode entry : arrayNode) {
                        // Backend always wraps as {"topic": {...}, "reason": "...", "score": ...}
                        JsonNode topicNode = entry.path("topic");
                        if (topicNode.isMissingNode() || topicNode.isNull()) {
                            topicNode = entry; // defensive fallback if shape ever changes
                        }
                        Topic topic = objectMapper.treeToValue(topicNode, Topic.class);
                        String reason = entry.path("reason").isMissingNode() ? null : entry.path("reason").asText(null);
                        items.add(new RecommendationItem(topic, reason));
                    }
                }
                return items;
            }
        };

        task.setOnSucceeded(event -> {
            CombinedResult result = task.getValue();
            // Personalized topics carry posts_count; trending topics carry recent_posts_count.
            populateSection(personalizedContainer, result.personalized, "posts", Topic::getPostsCount);
            populateSection(trendingContainer, result.trending, "recent posts", Topic::getRecentPostsCount);

            personalizedCountLabel.setText(String.valueOf(result.personalized.size()));
            trendingCountLabel.setText(String.valueOf(result.trending.size()));

            boolean empty = result.personalized.isEmpty() && result.trending.isEmpty();
            statusLabel.setText(empty ? "No recommendations available." : "Recommendations loaded.");
        });

        task.setOnFailed(event -> {
            Throwable failure = task.getException();
            statusLabel.setText(failure != null && failure.getMessage() != null
                    ? failure.getMessage()
                    : "Could not load recommendations.");
            if (failure != null) {
                failure.printStackTrace();
            }
        });

        new Thread(task).start();
    }

    private void populateSection(FlowPane container, List<RecommendationItem> items, String countUnit,
                                 java.util.function.ToIntFunction<Topic> countExtractor) {
        container.getChildren().clear();
        for (RecommendationItem item : items) {
            container.getChildren().add(createCard(item, countUnit, countExtractor));
        }
    }

    private VBox createCard(RecommendationItem item, String countUnit,
                            java.util.function.ToIntFunction<Topic> countExtractor) {
        Topic topic = item.topic;

        Label titleLabel = new Label(topic.getTitle());
        titleLabel.getStyleClass().add("card-title");
        titleLabel.setMaxWidth(200);

        // Backend Topic model doesn't guarantee category is set; fall back like the web view does.
        String category = topic.getCategory() != null ? topic.getCategory() : "General";
        Label tagLabel = new Label(category);
        tagLabel.getStyleClass().add("tag-pill");
        tagLabel.getStyleClass().add(isProgramming(category) ? "tag-programming" : "tag-default");

        Region titleSpacer = new Region();
        HBox.setHgrow(titleSpacer, javafx.scene.layout.Priority.ALWAYS);
        HBox topRow = new HBox(titleLabel, titleSpacer, tagLabel);
        topRow.setAlignment(Pos.TOP_LEFT);

        // reason comes straight from the backend (e.g. "Recent activity in your groups"),
        Label descriptionLabel = new Label(item.reason != null ? item.reason : "—");
        descriptionLabel.getStyleClass().add("card-description");

        Label countLabel = new Label(countExtractor.applyAsInt(topic) + " " + countUnit);
        countLabel.getStyleClass().add("card-count");

        Button openButton = new Button("Open");
        openButton.getStyleClass().add("open-button");
        openButton.setOnAction(e -> openTopicDetail(topic));

        Region bottomSpacer = new Region();
        HBox.setHgrow(bottomSpacer, javafx.scene.layout.Priority.ALWAYS);
        HBox bottomRow = new HBox(countLabel, bottomSpacer, openButton);
        bottomRow.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(10, topRow, descriptionLabel, bottomRow);
        card.getStyleClass().add("rec-card");
        card.setPadding(new Insets(18));
        return card;
    }

    private boolean isProgramming(String category) {
        return category != null && category.toLowerCase().contains("program");
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadRecommendations();
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mindshare/dashboard/DashboardView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            com.mindshare.utils.SceneUtils.switchScene(stage, root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openTopicDetail(Topic topic) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mindshare/discussion/TopicDetailView.fxml"));
            Parent root = loader.load();
            TopicDetailController controller = loader.getController();
            controller.setTopic(topic);
            Stage stage = (Stage) refreshButton.getScene().getWindow();
            com.mindshare.utils.SceneUtils.switchScene(stage, root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class CombinedResult {
        final List<RecommendationItem> personalized;
        final List<RecommendationItem> trending;

        CombinedResult(List<RecommendationItem> personalized, List<RecommendationItem> trending) {
            this.personalized = personalized;
            this.trending = trending;
        }
    }

    private static class RecommendationItem {
        final Topic topic;
        final String reason;

        RecommendationItem(Topic topic, String reason) {
            this.topic = topic;
            this.reason = reason;
        }
    }
}