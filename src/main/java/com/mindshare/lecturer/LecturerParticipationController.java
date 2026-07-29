package com.mindshare.lecturer;

import com.fasterxml.jackson.databind.JsonNode;
import com.mindshare.api.LecturerParticipationService;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Lecturer "Student Participation" leaderboard for a single group — mirrors
 * resources/views/groups/statistics.blade.php on the webapp. Distinct from
 * the admin-only Group Statistics (overall activity) screen.
 */
public class LecturerParticipationController {
    @FXML private Label statusLabel;
    @FXML private Label eyebrowLabel;
    @FXML private Label groupNameLabel;
    @FXML private Label subtitleLabel;
    @FXML private HBox statsRowTop;
    @FXML private HBox statsRowBottom;
    @FXML private HBox sortTabsRow;
    @FXML private VBox leaderboardContainer;
    @FXML private Button backButton;
    @FXML private ToggleButton sortScoreTab;
    @FXML private ToggleButton sortPostsTab;
    @FXML private ToggleButton sortNameTab;
    @FXML private ToggleButton sortActivityTab;

    private final LecturerParticipationService participationService = new LecturerParticipationService();
    private int groupId;
    private String sortBy = "participation_score";
    private final String sortOrder = "desc";

    @FXML
    public void initialize() {
        ToggleGroup group = new ToggleGroup();
        sortScoreTab.setToggleGroup(group);
        sortPostsTab.setToggleGroup(group);
        sortNameTab.setToggleGroup(group);
        sortActivityTab.setToggleGroup(group);
        sortScoreTab.setSelected(true);

        sortScoreTab.setOnAction(e -> changeSort("participation_score"));
        sortPostsTab.setOnAction(e -> changeSort("post_count"));
        sortNameTab.setOnAction(e -> changeSort("name"));
        sortActivityTab.setOnAction(e -> changeSort("activity_status"));
    }

    public void loadGroup(int groupId) {
        this.groupId = groupId;
        fetch();
    }

    private void changeSort(String field) {
        if (field.equals(sortBy)) return;
        sortBy = field;
        fetch();
    }

    private void fetch() {
        statusLabel.setText("Loading participation...");
        Task<JsonNode> task = new Task<>() {
            @Override protected JsonNode call() throws Exception {
                return participationService.fetchParticipation(groupId, sortBy, sortOrder);
            }
        };
        task.setOnSucceeded(e -> render(task.getValue()));
        task.setOnFailed(e -> {
            Throwable failure = task.getException();
            statusLabel.setText(failure == null || failure.getMessage() == null
                    ? "Could not load participation." : failure.getMessage());
        });
        new Thread(task).start();
    }

    private void render(JsonNode data) {
        statusLabel.setText("");
        groupNameLabel.setText(data.path("group_name").asText("Group"));
        subtitleLabel.setText("Marks for students in this group \u00b7 created by "
                + data.path("created_by").asText("Unknown"));

        statsRowTop.getChildren().setAll(
                statBox("Students", data.path("student_count").asText("0")),
                statBox("Topics", data.path("topic_count").asText("0")),
                statBox("Discussion posts", data.path("post_count").asText("0")));
        statsRowBottom.getChildren().setAll(
                statBox("Average mark", data.path("average_score").asText("0")),
                statBox("Top mark", data.path("top_score").asText("0")));
        for (var node : statsRowTop.getChildren()) HBox.setHgrow(node, Priority.ALWAYS);
        for (var node : statsRowBottom.getChildren()) HBox.setHgrow(node, Priority.ALWAYS);

        double topScore = data.path("top_score").asDouble(0);
        leaderboardContainer.getChildren().clear();
        JsonNode rows = data.path("participation_rows");
        if (!rows.isArray() || rows.isEmpty()) {
            Label empty = new Label("No students in this group yet.");
            empty.getStyleClass().add("group-card-subtitle");
            leaderboardContainer.getChildren().add(empty);
            return;
        }
        int rank = 1;
        for (JsonNode row : rows) {
            leaderboardContainer.getChildren().add(buildRow(rank, row, topScore));
            rank++;
        }
    }

    private VBox buildRow(int rank, JsonNode row, double topScore) {
        Label rankBadge = new Label(String.valueOf(rank));
        rankBadge.getStyleClass().add("rank-badge");
        if (rank == 1) rankBadge.getStyleClass().add("rank-badge-gold");

        Label nameLabel = new Label(row.path("name").asText(""));
        nameLabel.getStyleClass().add("group-card-title");
        String email = row.path("email").asText("");
        int posts = row.path("post_count").asInt(0);
        Label metaLabel = new Label(email + " \u00b7 " + posts + " post" + (posts == 1 ? "" : "s"));
        metaLabel.getStyleClass().add("group-card-subtitle");
        VBox nameBox = new VBox(2, nameLabel, metaLabel);

        Label statusPill = new Label(row.path("activity_status").asText("Inactive"));
        statusPill.getStyleClass().add("status-pill");

        HBox header = new HBox(14, rankBadge, nameBox, spacer(), statusPill);
        header.setAlignment(Pos.CENTER_LEFT);

        double score = row.path("participation_score").asDouble(0);
        double progress = topScore > 0 ? Math.max(0, Math.min(1, score / topScore)) : 0;
        ProgressBar bar = new ProgressBar(progress);
        bar.getStyleClass().add("participation-progress-bar");
        bar.setMaxWidth(Double.MAX_VALUE);

        Label scoreLabel = new Label(String.valueOf(score));
        scoreLabel.getStyleClass().add("stat-box-value");

        HBox footer = new HBox(12, scoreLabel);
        footer.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(10, header, bar, footer);
        card.getStyleClass().add("group-card");
        return card;
    }

    private VBox statBox(String label, String value) {
        Label labelNode = new Label(label);
        labelNode.getStyleClass().add("stat-box-label");
        Label valueNode = new Label(value);
        valueNode.getStyleClass().add("stat-box-value");
        VBox box = new VBox(6, labelNode, valueNode);
        box.getStyleClass().add("stat-box");
        box.setMaxWidth(Double.MAX_VALUE);
        return box;
    }

    private Region spacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mindshare/lecturer/LecturerGroupPickerView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            com.mindshare.utils.SceneUtils.switchScene(stage, root);
        } catch (Exception e) { e.printStackTrace(); }
    }
}
