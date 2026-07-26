package com.mindshare.utils;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class SceneUtils {

    public static final double DEFAULT_MIN_WIDTH = 1000;
    public static final double DEFAULT_MIN_HEIGHT = 700;

    private static final String DEFAULT_STYLE =
            SceneUtils.class.getResource("/com/mindshare/style.css").toExternalForm();

    public static Scene createStyledScene(Parent root, double width, double height) {
        Scene scene = new Scene(root, width, height);
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

        Scene currentScene = stage.getScene();
        if (currentScene != null) {
            // Make sure the existing scene's base fill is transparent so the root background shows through
            currentScene.setFill(Color.TRANSPARENT);
            currentScene.setRoot(root);

            if (!currentScene.getStylesheets().contains(DEFAULT_STYLE)) {
                currentScene.getStylesheets().add(DEFAULT_STYLE);
            }
        } else {
            Scene scene = createStyledScene(root, Math.max(fallbackWidth, DEFAULT_MIN_WIDTH), Math.max(fallbackHeight, DEFAULT_MIN_HEIGHT));
            stage.setScene(scene);
        }
    }
}