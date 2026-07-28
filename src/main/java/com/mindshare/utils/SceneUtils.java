package com.mindshare.utils;

import com.mindshare.auth.model.UserSession;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class SceneUtils {

    public static final double DEFAULT_MIN_WIDTH = 1000;
    public static final double DEFAULT_MIN_HEIGHT = 700;

    private static final String DEFAULT_STYLE =
            SceneUtils.class.getResource("/com/mindshare/style.css").toExternalForm();

    private static final String NOTIFICATION_BELL_FXML =
            "/com/mindshare/notification/NotificationBellView.fxml";

    public static Scene createStyledScene(Parent root, double width, double height) {
        Scene scene = new Scene(wrapWithNotificationBell(root), width, height);
        scene.setFill(Color.TRANSPARENT);

        if (!scene.getStylesheets().contains(DEFAULT_STYLE)) {
            scene.getStylesheets().add(DEFAULT_STYLE);
        }
        return scene;
    }

    public static void switchScene(Stage stage, Parent root) {
        switchScene(stage, root, DEFAULT_MIN_WIDTH, DEFAULT_MIN_HEIGHT);
    }

    public static void switchScene(Stage stage, Parent root, double fallbackWidth, double fallbackHeight) {
        if (stage == null) return;

        if (stage.getMinWidth() < DEFAULT_MIN_WIDTH) {
            stage.setMinWidth(DEFAULT_MIN_WIDTH);
        }
        if (stage.getMinHeight() < DEFAULT_MIN_HEIGHT) {
            stage.setMinHeight(DEFAULT_MIN_HEIGHT);
        }

        Parent displayRoot = wrapWithNotificationBell(root);

        Scene currentScene = stage.getScene();
        if (currentScene != null) {
            currentScene.setFill(Color.TRANSPARENT);
            currentScene.setRoot(displayRoot);

            if (!currentScene.getStylesheets().contains(DEFAULT_STYLE)) {
                currentScene.getStylesheets().add(DEFAULT_STYLE);
            }
        } else {
            Scene scene = new Scene(displayRoot, Math.max(fallbackWidth, DEFAULT_MIN_WIDTH), Math.max(fallbackHeight, DEFAULT_MIN_HEIGHT));
            scene.setFill(Color.TRANSPARENT);
            if (!scene.getStylesheets().contains(DEFAULT_STYLE)) {
                scene.getStylesheets().add(DEFAULT_STYLE);
            }
            stage.setScene(scene);
        }
    }

    private static Parent wrapWithNotificationBell(Parent screenRoot) {
        if (screenRoot == null || !UserSession.isLoggedIn()) {
            return screenRoot;
        }

        try {
            Node bell = new FXMLLoader(SceneUtils.class.getResource(NOTIFICATION_BELL_FXML)).load();
            AnchorPane.setTopAnchor(screenRoot, 0.0);
            AnchorPane.setRightAnchor(screenRoot, 0.0);
            AnchorPane.setBottomAnchor(screenRoot, 0.0);
            AnchorPane.setLeftAnchor(screenRoot, 0.0);

            AnchorPane.setTopAnchor(bell, 12.0);
            AnchorPane.setRightAnchor(bell, 16.0);

            AnchorPane overlay = new AnchorPane(screenRoot, bell);
            overlay.setPickOnBounds(false);
            return overlay;
        } catch (Exception e) {
            e.printStackTrace();
            return screenRoot;
        }
    }
}
