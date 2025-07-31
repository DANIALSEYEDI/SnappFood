package org.example.snappfrontend.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.snappfrontend.dto.AuthLoginRequest;
import org.example.snappfrontend.dto.AuthLoginResponse;
import org.example.snappfrontend.http.HttpClientService;
import org.example.snappfrontend.utils.AuthManager;
import org.example.snappfrontend.utils.SceneLoader;

public class LoginPageController {

    @FXML private TextField numberField;
    @FXML private PasswordField passwordField;
    @FXML private Hyperlink registerLink;
    @FXML private Button signInButton;
    @FXML private Label errorLabel;

    @FXML
    void handleLoginButton(ActionEvent event) {
        String phone = numberField.getText().trim();
        String password = passwordField.getText().trim();

        if (phone.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter phone and password.");
            return;
        }
        new Thread(() -> {
            try {
                if (phone.equalsIgnoreCase("admin") && password.equalsIgnoreCase("admin")) {
                    String token = HttpClientService.adminLogin(phone, password);
                    AuthManager.setJwtToken(token);
                    AuthManager.setCurrentUserRole("ADMIN");
                    AuthManager.setCurrentUserId("0");

                    javafx.application.Platform.runLater(() -> {
                        errorLabel.setText("Welcome Admin");
                    });

                    Thread.sleep(1000);

                    javafx.application.Platform.runLater(() -> {
                        SceneLoader.loadScene("/org/example/snappfrontend/pages/HomePage.fxml", signInButton);
                    });
                    return;
                }
                AuthLoginRequest req = new AuthLoginRequest(phone, password);
                AuthLoginResponse res = HttpClientService.login(req);

                AuthManager.setJwtToken(res.token);
                AuthManager.setCurrentUserId(String.valueOf(res.user.id));
                AuthManager.setCurrentUserRole(res.user.role.toUpperCase());
                javafx.application.Platform.runLater(() -> {
                    errorLabel.setText(res.message.replaceAll("[\\[\\]\"]", "").trim());
                });
                Thread.sleep(1000);
                javafx.application.Platform.runLater(() -> {
                    SceneLoader.loadScene("/org/example/snappfrontend/pages/HomePage.fxml", signInButton);
                });
            } catch (Exception e) {
                e.printStackTrace();
                String errorMsg = e.getMessage();
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode node = mapper.readTree(errorMsg);
                    if (node.has("error")) {
                        errorMsg = node.get("error").asText();
                    }
                } catch (Exception ignored) {}

                String finalErrorMsg = errorMsg.trim();
                javafx.application.Platform.runLater(() -> {
                    errorLabel.setText(finalErrorMsg);
                });
            }
        }).start();
    }

    @FXML
    void handleRegisterLink(ActionEvent event) {
        SceneLoader.loadScene("/org/example/snappfrontend/pages/RegisterPage.fxml", registerLink);
    }
}