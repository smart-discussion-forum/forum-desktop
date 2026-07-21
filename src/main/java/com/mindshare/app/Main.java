package com.mindshare.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
       com.mindshare.database.SQLiteConnection.initializeSchema();
       // The initialization call could be through an import at the top outside the method.
        com.mindshare.sync.NetworkMonitor.refreshStatus();

        boolean online = com.mindshare.sync.NetworkMonitor.isServerReachable();
        System.out.println("Server reachable: " + online);

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/mindshare/auth/WelcomeView.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("MindShare Discussion Forum");
        primaryStage.setScene(com.mindshare.utils.SceneUtils.createStyledScene(root, 600, 420));
        primaryStage.show();


    }

    public static void main(String[] args) {
        launch(args);
    }
}