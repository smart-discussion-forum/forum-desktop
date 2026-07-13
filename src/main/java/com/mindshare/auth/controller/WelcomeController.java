package com.mindshare.auth.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class WelcomeController {

    @FXML
    private Button viewRulesButton;

    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    @FXML
    private void handleViewRules(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Forum Rules");
        alert.setHeaderText("MindShare Discussion Forum Rules");
        alert.setContentText(
                "1. Be respectful to other members.\n" +
                        "2. Keep posts relevant to the topic.\n" +
                        "3. No spam or repeated flooding of threads.\n" +
                        "4. Two warnings result in blacklisting."
        );
alert.showAndWait();
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        try {
            Parent loginRoot = FXMLLoader.load(getClass().getResource("/com/mindshare/auth/LoginView.fxml"));
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(loginRoot, 600,420));
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }
    @FXML
    private void handleRegister(ActionEvent event) {
        try {
            Parent registerRoot = FXMLLoader.load(getClass().getResource("/com/mindshare/auth/RegistrationView.fxml"));
            Stage stage = (Stage) registerButton.getScene().getWindow();
            stage.setScene(new Scene(registerRoot, 600, 420));
                }
        catch (Exception e) {
            e.printStackTrace();
        }
            }
}
