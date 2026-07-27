package com.mindshare.utils;

import com.mindshare.auth.model.UserSession;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
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
            // Make sure the existing scene's base fill is transparent so the root background shows through
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

    /**
     * Overlays the notification bell in the true top-right corner of the window on top of
     * {@code screenRoot}, without touching the screen's own layout in any way. Applied here,
     * once, since every screen transition in the app already funnels through this class —
     * that avoids splicing the bell into each screen's FXML individually (which previously
     * stole growable space from HBox rows and pushed titles off-center).
     *
     * Only shown once a user is signed in (pre-auth screens like Welcome/Login/Registration
     * have no token yet, and notifications wouldn't resolve to anyone).
     */
    private static Parent wrapWithNotificationBell(Parent screenRoot) {
        if (screenRoot == null || !UserSession.isLoggedIn()) {
            return screenRoot;
        }

        try {
            Node bell = new FXMLLoader(SceneUtils.class.getResource(NOTIFICATION_BELL_FXML)).load();
            StackPane.setAlignment(bell, Pos.TOP_RIGHT);
            StackPane.setMargin(bell, new Insets(18, 22, 0, 0));

            StackPane overlay = new StackPane(screenRoot, bell);
            // Let the wrapped screen own sizing; the overlay itself shouldn't add its own padding/background.
            overlay.setPickOnBounds(false);
            return overlay;
        } catch (Exception e) {
            // If the bell fails to load for any reason, fall back to the plain screen rather than
            // breaking navigation.
            e.printStackTrace();
            return screenRoot;
        }
    }
}