package org.example.snappfrontend.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.snappfrontend.dto.AuthRegisterRequest;
import org.example.snappfrontend.dto.AuthRegisterResponse;
import org.example.snappfrontend.http.HttpClientService;
import org.example.snappfrontend.http.HttpResponseData;
import org.example.snappfrontend.utils.AuthManager;
import org.example.snappfrontend.utils.SceneLoader;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Base64;

public class RegisterPageController {
    @FXML private TextField accountField;
    @FXML private TextField addressField;
    @FXML private TextField bankField;
    @FXML private TextField emailField;
    @FXML private ImageView imageView;
    private String imageBase64 = null;
    @FXML private Hyperlink loginLink;
    @FXML private TextField nameField;
    @FXML private TextField numberField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleField;
    @FXML private Button submitButton;
    @FXML private Button uploadButton;
    @FXML private Label errorLabel;

    @FXML
    void handleButton(ActionEvent event) {
        String name = nameField.getText().trim();
        String number = numberField.getText().trim();
        String password = passwordField.getText().trim();
        String email = emailField.getText().trim();
        String address = addressField.getText().trim();
        String role = roleField.getValue();
        String bankName = bankField.getText().trim();
        String accountNumber = accountField.getText().trim();

        if (name.isEmpty() || number.isEmpty() || password.isEmpty() || role == null || role.isEmpty()) {
            errorLabel.setText("Please fill in all required fields.");
            return;
        }

        try {
            AuthRegisterRequest.BankInfo bankInfo = null;
            if (!bankName.isEmpty() && !accountNumber.isEmpty()) {
                bankInfo = new AuthRegisterRequest.BankInfo();
                bankInfo.bank_name = bankName;
                bankInfo.account_number = accountNumber;
            }
            AuthRegisterRequest request = new AuthRegisterRequest();
            request.full_name = name;
            request.phone = number;
            request.email = email;
            request.password = password;
            request.address = address;
            request.role = role;
            request.profileImageBase64 = imageBase64;
            request.bank_info = bankInfo;

            HttpResponseData response = HttpClientService.registerRawResponse(request);
            if (response.getStatusCode() == 200) {
                AuthRegisterResponse authResponse = new ObjectMapper().readValue(response.getBody(), AuthRegisterResponse.class);
                AuthManager.setJwtToken(authResponse.getToken());
                AuthManager.setCurrentUserId(String.valueOf(authResponse.getUser_id()));
                AuthManager.setCurrentUserRole(role.toUpperCase());
                errorLabel.setText(authResponse.getMessage());
                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    javafx.application.Platform.runLater(() -> {
                        SceneLoader.loadScene("/org/example/snappfrontend/pages/HomePage.fxml", submitButton);
                    });
                }).start();

            } else {
                try {
                    JsonNode errorNode = new ObjectMapper().readTree(response.getBody());
                    if (errorNode.has("error")) {
                        errorLabel.setText(errorNode.get("error").asText());
                    } else {
                        errorLabel.setText(response.getBody());
                    }
                } catch (Exception ex) {
                    errorLabel.setText(response.getBody());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText(e.getMessage());
        }
    }

    @FXML
    void handleLink(ActionEvent event) {
        SceneLoader.loadScene("/org/example/snappfrontend/pages/LoginPage.fxml", loginLink);
    }


    @FXML
    void handleUploadImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select an Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(((Stage) uploadButton.getScene().getWindow()));
        if (selectedFile != null) {
            try {
                Image fxImage = new Image(selectedFile.toURI().toString());
                BufferedImage original = SwingFXUtils.fromFXImage(fxImage, null);
                int targetWidth = 300;
                int targetHeight = (original.getHeight() * targetWidth) / original.getWidth();
                BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = resized.createGraphics();
                g2d.drawImage(original, 0, 0, targetWidth, targetHeight, null);
                g2d.dispose();
                Image resizedFxImage = SwingFXUtils.toFXImage(resized, null);
                imageView.setImage(resizedFxImage);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(resized, "jpg", baos);
                byte[] bytes = baos.toByteArray();
                imageBase64 = Base64.getEncoder().encodeToString(bytes);
                errorLabel.setText("");
            } catch (Exception e) {
                e.printStackTrace();
                errorLabel.setText("Upload image failed");
            }
        }
    }
}