package com.mindshare.auth.controller;

import com.mindshare.api.PasswordResetResult;
import com.mindshare.api.PasswordResetService;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class PasswordResetController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmationField;
    @FXML private Label errorLabel;
    @FXML private Button resetButton;

    private final PasswordResetService passwordResetService = new PasswordResetService();

    @FXML
    private void handleReset(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmation = confirmationField.getText();

        if (email.isEmpty() || password.isEmpty() || confirmation.isEmpty()) {
            showError("Email, new password, and confirmation are required.");
            return;
        }
        if (password.length() < 8) {
            showError("Your new password must be at least 8 characters.");
            return;
        }
        if (!password.equals(confirmation)) {
            showError("The password confirmation does not match.");
            return;
        }

        errorLabel.setVisible(false);
        resetButton.setDisable(true);

        Task<PasswordResetResult> resetTask = new Task<>() {
            @Override
            protected PasswordResetResult call() throws IOException {
                return passwordResetService.resetPassword(email, password, confirmation);
            }
        };
        resetTask.setOnSucceeded(e -> {
            resetButton.setDisable(false);
            PasswordResetResult result = resetTask.getValue();
            if (result.successful()) {
                returnToLogin(email, result.message());
            } else {
                showError(result.message());
            }
        });
        resetTask.setOnFailed(e -> {
            resetButton.setDisable(false);
            showError("Could not reach the server. Check your connection and try again.");
        });
        Thread thread = new Thread(resetTask, "password-reset-request");
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void handleBackToLogin(ActionEvent event) {
        returnToLogin(null, null);
    }

    private void returnToLogin(String email, String status) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mindshare/auth/LoginView.fxml"));
            Parent loginRoot = loader.load();
            LoginController loginController = loader.getController();
            if (email != null) {
                loginController.setEmail(email);
            }
            if (status != null) {
                loginController.showStatus(status);
            }
            Stage stage = (Stage) resetButton.getScene().getWindow();
            com.mindshare.utils.SceneUtils.switchScene(stage, loginRoot);
        } catch (IOException e) {
            showError("Could not return to the login screen.");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
