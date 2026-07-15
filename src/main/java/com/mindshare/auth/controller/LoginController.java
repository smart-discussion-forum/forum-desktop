package com.mindshare.auth.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.mindshare.api.AuthService;
import com.mindshare.auth.model.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.io.IOException;

public class LoginController{

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private Button loginButton;

    private final AuthService authService = new AuthService();

    @FXML
    private void handleLogin(ActionEvent event) {
        String email =usernameField.getText();
        String password =passwordField.getText();

        if (email.isBlank() || password.isBlank())
        {
            errorLabel.setText("Email and password re required.");
            errorLabel.setVisible(true);
            return;
        }
        errorLabel.setVisible(false);

        try {
            JsonNode response = authService.login(email, password);
            System.out.println("Raw response: " + response.toString());

            if (response.get("success").asBoolean()) {
                String token = response.get("token").asText();
                String userName = response.get("user").get("name").asText();
                String userRole = response.get("user").get("role").asText();

                UserSession.set(token, userName, userRole);

                System.out.println("Login successful. Token stored for: " + userName);
// Next is navigating to the Dashboard screen from here.

            } else {
                String message = response.has("message") ? response.get("message").asText() : "Login failed.";
                errorLabel.setText(message);
                errorLabel.setVisible(true);
            }

        } catch (IOException e) {
            errorLabel.setText("Could not reach the server. Check your connection.");
            errorLabel.setVisible(true);
            e.printStackTrace();
        }
    }
}



