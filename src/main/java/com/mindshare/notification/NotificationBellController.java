package com.mindshare.notification;

import com.fasterxml.jackson.databind.JsonNode;
import com.mindshare.api.NotificationService;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Popup;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Bell icon + dropdown, embedded via fx:include on the main dashboard and
 * admin landing screens. The webapp has no notification UI of its own to
 * mirror, so this follows the typical bell-icon-with-dropdown pattern
 * against the existing /api/notifications endpoints.
 */
public class NotificationBellController {
    @FXML private StackPane root;
    @FXML private Button bellButton;
    @FXML private Label badgeLabel;

    private final NotificationService notificationService = new NotificationService();
    private final Popup popup = new Popup();
    private final VBox popupContent = new VBox(4);
    private final Label statusLabel = new Label();
    private final VBox listContainer = new VBox(2);

    @FXML
    public void initialize() {
        SVGPath bellIcon = new SVGPath();
        bellIcon.setContent("M12 22c1.1 0 2-.9 2-2h-4c0 1.1.9 2 2 2zm6-6v-5c0-3.07-1.63-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5S10.5 3.17 10.5 4v.68C7.63 5.36 6 7.93 6 11v5l-2 2v1h16v-1l-2-2z");
        bellIcon.setFill(Color.web("#fbbf24"));
        bellIcon.setScaleX(0.9);
        bellIcon.setScaleY(0.9);
        bellButton.setText("");
        bellButton.setGraphic(bellIcon);
        buildPopup();
        refreshUnreadCount();
    }

    private void buildPopup() {
        popupContent.getStyleClass().add("notification-popup");
        popupContent.setPrefWidth(320);
        popupContent.setPrefHeight(420);
        popupContent.setMaxHeight(420);

        Label header = new Label("Notifications");
        header.getStyleClass().add("notification-popup-title");
        Button markAllButton = new Button("Mark all as read");
        markAllButton.getStyleClass().add("notification-mark-all");
        markAllButton.setOnAction(this::handleMarkAllRead);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox headerRow = new HBox(8, header, spacer, markAllButton);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        headerRow.setPadding(new Insets(12, 14, 8, 14));

        statusLabel.getStyleClass().add("notification-empty");
        statusLabel.setPadding(new Insets(0, 14, 12, 14));

        ScrollPane scrollPane = new ScrollPane(listContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(330);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        scrollPane.getStyleClass().add("notification-scroll");

        Label footer = new Label("View all notifications");
        footer.getStyleClass().add("notification-popup-footer");
        footer.setMaxWidth(Double.MAX_VALUE);
        footer.setAlignment(Pos.CENTER);

        popupContent.getChildren().addAll(headerRow, statusLabel, scrollPane, footer);
        popup.getContent().add(popupContent);
        popup.setAutoHide(true);

        if (bellButton.getScene() != null) {
            popupContent.getStylesheets().setAll(bellButton.getScene().getStylesheets());
        } else {
            popupContent.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    popupContent.getStylesheets().setAll(newScene.getStylesheets());
                }
            });
        }
    }

    @FXML
    private void handleBellClick(ActionEvent event) {
        if (popup.isShowing()) {
            popup.hide();
            return;
        }
        loadNotifications();
        var bounds = bellButton.localToScreen(bellButton.getBoundsInLocal());
        popup.show(bellButton, bounds.getMinX() - 260, bounds.getMaxY() + 6);
    }

    private void refreshUnreadCount() {
        runTask(notificationService::fetchNotifications, result -> {
            int unread = countUnread(result);
            updateBadge(unread);
        }, failure -> { /* silent — badge just stays at its last known value */ });
    }

    private void loadNotifications() {
        statusLabel.setText("Loading...");
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);
        listContainer.getChildren().clear();

        runTask(notificationService::fetchNotifications, result -> {
            List<JsonNode> items = new ArrayList<>();
            if (result.isArray()) result.forEach(items::add);
            renderList(items);
            updateBadge(countUnread(result));
        }, failure -> {
            statusLabel.setText("Could not load notifications.");
            statusLabel.setVisible(true);
            statusLabel.setManaged(true);
        });
    }

    private void renderList(List<JsonNode> items) {
        listContainer.getChildren().clear();
        if (items.isEmpty()) {
            statusLabel.setText("No notifications yet.");
            statusLabel.setVisible(true);
            statusLabel.setManaged(true);
            return;
        }
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);
        for (JsonNode item : items) {
            listContainer.getChildren().add(buildItem(item));
        }
    }

    private VBox buildItem(JsonNode item) {
        boolean unread = item.path("read_at").isNull() || item.path("read_at").asText("").isBlank();
        String message = item.path("data").path("message").asText("Notification");
        String timeAgo = formatRelativeTime(item.path("created_at").asText(""));

        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.getStyleClass().add(unread ? "notification-item-message-unread" : "notification-item-message");

        Label timeLabel = new Label(timeAgo);
        timeLabel.getStyleClass().add("notification-item-time");

        VBox textBox = new VBox(2, messageLabel, timeLabel);
        textBox.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        HBox row = new HBox(8, textBox);
        if (unread) {
            Label dot = new Label("\u25CF");
            dot.getStyleClass().add("notification-unread-dot");
            row.getChildren().add(0, dot);
        }
        row.setAlignment(Pos.TOP_LEFT);

        VBox card = new VBox(row);
        card.getStyleClass().add(unread ? "notification-item-unread" : "notification-item");
        card.setPadding(new Insets(10, 12, 10, 12));
        if (unread) {
            String id = item.path("id").asText(null);
            card.setOnMouseClicked(e -> handleItemClick(id, card));
        }
        return card;
    }

    private void handleItemClick(String notificationId, VBox card) {
        if (notificationId == null) return;
        runTask(() -> notificationService.markRead(notificationId), result -> {
            card.getStyleClass().remove("notification-item-unread");
            card.getStyleClass().add("notification-item");
            refreshUnreadCount();
        }, failure -> { /* leave as unread on failure, no disruptive alert for a minor action */ });
    }

    private void handleMarkAllRead(ActionEvent event) {
        runTask(notificationService::markAllRead, result -> loadNotifications(), failure -> { });
    }

    private void updateBadge(int unreadCount) {
        if (unreadCount <= 0) {
            badgeLabel.setVisible(false);
            badgeLabel.setManaged(false);
        } else {
            badgeLabel.setText(unreadCount > 99 ? "99+" : String.valueOf(unreadCount));
            badgeLabel.setVisible(true);
            badgeLabel.setManaged(true);
        }
    }

    private int countUnread(JsonNode result) {
        if (!result.isArray()) return 0;
        int count = 0;
        for (JsonNode item : result) {
            if (item.path("read_at").isNull() || item.path("read_at").asText("").isBlank()) count++;
        }
        return count;
    }

    private String formatRelativeTime(String rawIso) {
        if (rawIso == null || rawIso.isBlank()) return "";
        try {
            Instant instant = Instant.parse(rawIso);
            Duration elapsed = Duration.between(instant, Instant.now());
            if (elapsed.toMinutes() < 1) return "Just now";
            if (elapsed.toMinutes() < 60) return elapsed.toMinutes() + "m ago";
            if (elapsed.toHours() < 24) return elapsed.toHours() + "h ago";
            if (elapsed.toDays() < 7) return elapsed.toDays() + "d ago";
            return elapsed.toDays() / 7 + "w ago";
        } catch (Exception e) {
            return "";
        }
    }

    private void runTask(IoAction action, SuccessAction success, FailureAction failure) {
        Task<JsonNode> task = new Task<>() {
            @Override protected JsonNode call() throws Exception { return action.run(); }
        };
        task.setOnSucceeded(event -> success.accept(task.getValue()));
        task.setOnFailed(event -> failure.accept(task.getException()));
        new Thread(task).start();
    }

    private interface IoAction { JsonNode run() throws Exception; }
    private interface SuccessAction { void accept(JsonNode value); }
    private interface FailureAction { void accept(Throwable failure); }
}
