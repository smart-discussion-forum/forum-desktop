package com.mindshare.utils;

import javafx.scene.Parent;
import javafx.scene.Scene;

public class SceneUtils {

    public static Scene createStyledScene(Parent root, double width, double height) {
        Scene scene = new Scene(root, width, height);
        scene.getStylesheets().add(SceneUtils.class.getResource("/com/mindshare/style.css").toExternalForm());
        return scene;
    }
}
