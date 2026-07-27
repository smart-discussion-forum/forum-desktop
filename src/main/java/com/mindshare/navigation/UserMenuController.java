package com.mindshare.navigation;

import com.mindshare.auth.model.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;

public class UserMenuController {

    @FXML private StackPane root;
    @FXML private Button avatarButton;

    private final Popup popup = new Popup();
    private final VBox popupContent = new VBox();

    @FXML
    public void initialize() {
        avatarButton.setText(initialsFrom(UserSession.getUserName()));
        String name = UserSession.getUserName();
        if (name != null && !name.isBlank()) {
            avatarButton.setTooltip(new javafx.scene.control.Tooltip(name));
        }
        buildPopup();
    }

    private void buildPopup() {
        popupContent.getStyleClass().add("user-dropdown");
        popupContent.setPrefWidth(200);

        String name = safe(UserSession.getUserName(), "User");
        String role = capitalize(safe(UserSession.getUserRole(), ""));

        Label info = new Label(name + (role.isBlank() ? "" : "\n" + role));
        info.getStyleClass().add("user-dropdown-info");
        info.setWrapText(true);
        info.setMaxWidth(Double.MAX_VALUE);

        Button viewProfile = new Button("View Profile");
        viewProfile.getStyleClass().add("user-dropdown-item");
        viewProfile.setMaxWidth(Double.MAX_VALUE);
        viewProfile.setAlignment(Pos.CENTER_LEFT);
        viewProfile.setOnAction(e -> {
            popup.hide();
            navigate("/com/mindshare/profile/ProfileView.fxml");
        });

        Button logout = new Button("Logout");
        logout.getStyleClass().add("user-dropdown-item");
        logout.setMaxWidth(Double.MAX_VALUE);
        logout.setAlignment(Pos.CENTER_LEFT);
        logout.setOnAction(e -> {
            popup.hide();
            handleLogout();
        });

        popupContent.getChildren().setAll(info, viewProfile, logout);
        popupContent.setPadding(new Insets(0));
        popup.getContent().setAll(popupContent);
        popup.setAutoHide(true);

        if (avatarButton.getScene() != null) {
            popupContent.getStylesheets().setAll(avatarButton.getScene().getStylesheets());
        } else {
            avatarButton.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    popupContent.getStylesheets().setAll(newScene.getStylesheets());
                }
            });
        }
    }

    @FXML
    private void handleAvatarClick(ActionEvent event) {
        if (popup.isShowing()) {
            popup.hide();
            return;
        }
        // Refresh labels in case session changed
        buildPopup();
        var bounds = avatarButton.localToScreen(avatarButton.getBoundsInLocal());
        popup.show(avatarButton, bounds.getMaxX() - 200, bounds.getMaxY() + 6);
    }

    private void handleLogout() {
        UserSession.clear();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/mindshare/auth/WelcomeView.fxml"));
            Stage stage = (Stage) avatarButton.getScene().getWindow();
            com.mindshare.utils.SceneUtils.switchScene(stage, root);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void navigate(String path) {
        try {
            Parent next = FXMLLoader.load(getClass().getResource(path));
            Stage stage = (Stage) avatarButton.getScene().getWindow();
            com.mindshare.utils.SceneUtils.switchScene(stage, next);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    static String initialsFrom(String name) {
        if (name == null || name.isBlank()) {
            return "?";
        }
        StringBuilder initials = new StringBuilder();
        for (String part : name.trim().split("\\s+")) {
            if (part.isEmpty()) continue;
            initials.append(Character.toUpperCase(part.charAt(0)));
            if (initials.length() >= 2) break;
        }
        return initials.length() == 0 ? "?" : initials.toString();
    }

    private static String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private static String capitalize(String value) {
        if (value == null || value.isBlank()) return "";
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }
}
