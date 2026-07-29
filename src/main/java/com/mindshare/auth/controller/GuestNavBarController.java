package com.mindshare.auth.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.Stage;

/** Top nav for unauthenticated screens: Home, Login, Register. */
public class GuestNavBarController {

    @FXML
    private void handleHome(ActionEvent event) {
        navigate(event, "/com/mindshare/auth/WelcomeView.fxml");
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        navigate(event, "/com/mindshare/auth/LoginView.fxml");
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        navigate(event, "/com/mindshare/auth/RegistrationView.fxml");
    }

    private void navigate(ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            com.mindshare.utils.SceneUtils.switchScene(stage, root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
