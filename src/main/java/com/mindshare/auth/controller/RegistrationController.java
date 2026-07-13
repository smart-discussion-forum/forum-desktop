package com.mindshare.auth.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

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
        System.out.println("Registering user:" + nameField.getText());
        // Next is calling Laravel endpoint here
    }
}
