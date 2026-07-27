package com.mindshare.auth.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.mindshare.api.AuthService;
import com.mindshare.auth.model.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class RegistrationController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private CheckBox acceptRulesCheckBox;

    @FXML
    private Label errorLabel;

    @FXML
    private Button registerButton;

    private final AuthService authService = new AuthService();

    @FXML
    private void handleRegister(ActionEvent event) {
        if (nameField.getText().isBlank() || emailField.getText().isBlank() || passwordField.getText().isBlank()) {
            errorLabel.setText("All fields are required.");
            errorLabel.setVisible(true);
            return;
        }

        //FR-05: Registration is declined if rules aren't accepted
        if (!acceptRulesCheckBox.isSelected()) {
            errorLabel.setText("You must accept rules to register.");
            errorLabel.setVisible(true);
            return;
        }
        errorLabel.setVisible(false);

        try {
            JsonNode response = authService.register(
                    nameField.getText().trim(),
                    emailField.getText().trim(),
                    passwordField.getText(),
                    true
            );

            if (response.has("success") && response.get("success").asBoolean()) {
                String token = response.get("token").asText();
                JsonNode userNode = response.get("user");
                String userName = userNode.get("name").asText();
                String userRole = userNode.get("role").asText();
                String userEmail = userNode.has("email") ? userNode.get("email").asText() : null;
                int userId = userNode.has("id") ? userNode.get("id").asInt() : -1;

                UserSession.set(token, userName, userRole, userEmail, userId);

                String dashboardPath = userRole.equalsIgnoreCase("admin")
                        ? "/com/mindshare/admin/AdminLandingView.fxml"
                        : "/com/mindshare/dashboard/DashboardView.fxml";
                Parent dashboardRoot = FXMLLoader.load(getClass().getResource(dashboardPath));
                Stage stage = (Stage) registerButton.getScene().getWindow();
                com.mindshare.utils.SceneUtils.switchScene(stage, dashboardRoot);
                return;
            }

            errorLabel.setText(extractErrorMessage(response));
            errorLabel.setVisible(true);
        } catch (IOException e) {
            errorLabel.setText("Could not reach the server. Check your connection.");
            errorLabel.setVisible(true);
            e.printStackTrace();
        }
    }

    private static String extractErrorMessage(JsonNode response) {
        if (response.has("errors") && response.get("errors").isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = response.get("errors").fields();
            if (fields.hasNext()) {
                JsonNode first = fields.next().getValue();
                if (first.isArray() && first.size() > 0) {
                    return first.get(0).asText();
                }
            }
        }
        if (response.has("message")) {
            return response.get("message").asText();
        }
        return "Registration failed.";
    }
}
