package com.mindshare.auth.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController{

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private Button loginButton;

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
        System.out.println("Attempting login for:" +email);
        //Next step is calling Laravel Sanctum login endpoint here
    }
}



