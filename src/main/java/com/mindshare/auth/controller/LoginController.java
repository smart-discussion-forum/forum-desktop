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
    private Label statusLabel;
    @FXML
    private Button loginButton;
    private final AuthService authService = new AuthService();

    @FXML
    private TextField passwordVisibleField;

    @FXML
    private Button togglePasswordButton;

    @FXML
    private void initialize() {
        // Keep both fields in sync as the user types
        passwordVisibleField.textProperty().bindBidirectional(passwordField.textProperty());
    }

    @FXML
    private void handleTogglePasswordVisibility() {
        boolean willShow = !passwordVisibleField.isVisible();

        passwordVisibleField.setVisible(willShow);
        passwordVisibleField.setManaged(willShow);

        passwordField.setVisible(!willShow);
        passwordField.setManaged(!willShow);

        togglePasswordButton.setText(willShow ? "🙈" : "👁");
    }

    @FXML
    private void handleForgotPassword(ActionEvent event) {
        try {
            javafx.scene.Parent resetRoot = javafx.fxml.FXMLLoader.load(
                    getClass().getResource("/com/mindshare/auth/ResetPasswordView.fxml"));
            javafx.stage.Stage stage = (javafx.stage.Stage) loginButton.getScene().getWindow();
            com.mindshare.utils.SceneUtils.switchScene(stage, resetRoot);
        } catch (IOException e) {
            errorLabel.setText("Could not open password reset.");
            errorLabel.setVisible(true);
        }
    }

    public void setEmail(String email) {
        usernameField.setText(email);
    }

    public void showStatus(String message) {
        statusLabel.setText(message);
        statusLabel.setVisible(true);
    }

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
                JsonNode userNode = response.get("user");
                String userName = userNode.get("name").asText();
                String userRole = userNode.get("role").asText();
                String userEmail = userNode.has("email") ? userNode.get("email").asText() : null;
                int userId = userNode.has("id") ? userNode.get("id").asInt() : -1;
                UserSession.set(token, userName, userRole, userEmail, userId);
                System.out.println("Login successful. Token stored for: " + userName);
                String dashboardPath = userRole.equalsIgnoreCase("admin")
                        ? "/com/mindshare/admin/AdminLandingView.fxml"
                        : "/com/mindshare/dashboard/DashboardView.fxml";
                java.net.URL dashboardUrl = getClass().getResource(dashboardPath);
                
                //Navigating to the dashboard screen (admins go straight to the admin dashboard)
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(dashboardUrl);
                javafx.scene.Parent dashboardRoot = loader.load();
                javafx.stage.Stage stage = (javafx.stage.Stage) loginButton.getScene().getWindow();
                com.mindshare.utils.SceneUtils.switchScene(stage, dashboardRoot);
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