package org.example.snappfrontend.controllers;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.animation.PauseTransition;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.snappfrontend.dto.AuthProfileUpdateRequestDTO;
import org.example.snappfrontend.models.User;
import org.example.snappfrontend.utils.ApiClient;
import org.example.snappfrontend.utils.AuthManager;
import org.example.snappfrontend.utils.JsonUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileController {

    @FXML private TextField accountField;
    @FXML private TextField addressField;
    @FXML private TextField bankField;
    @FXML private TextField emailField;
    @FXML private Label errorLabel;
    @FXML private Button editButton;
    @FXML private Button saveButton;
    @FXML private ImageView imageView;
    @FXML private TextField nameField;
    @FXML private TextField numberField;
    @FXML private TextField roleField;

    private User currentUser;
    private String base64ImageString;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @FXML
    public void initialize() {
        setFieldsEditable(false);
        roleField.setEditable(false);
        saveButton.setVisible(false);
        loadUserProfile();
    }

    private void setFieldsEditable(boolean editable) {
        nameField.setEditable(editable);
        numberField.setEditable(editable);
        emailField.setEditable(editable);
        addressField.setEditable(editable);
        bankField.setEditable(editable);
        accountField.setEditable(editable);
    }

    private void loadUserProfile() {
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> errorLabel.setText("Authentication token missing. Please log in again."));
                    return;
                }

                Optional<HttpResponse<String>> responseOpt = ApiClient.get("/auth/profile", token);

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            try {
                                currentUser = JsonUtil.getObjectMapper().treeToValue(rootNode, User.class);
                                populateProfileFields(currentUser);
                            } catch (IOException e) {
                                errorLabel.setText("Error parsing profile data: " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                            errorLabel.setText("Error loading profile: " + errorMessage);
                        }
                    });
                } else {
                    Platform.runLater(() -> errorLabel.setText("Failed to connect to server to load profile."));
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    errorLabel.setText("An unexpected error occurred while loading profile: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }

    private void populateProfileFields(User user) {
        if (user != null) {
            nameField.setText(user.getFullName());
            numberField.setText(user.getPhone());
            emailField.setText(user.getEmail());
            addressField.setText(user.getAddress());
            roleField.setText(user.getRole());
            bankField.setText(user.getBankName());
            accountField.setText(user.getAccountNumber());

            if (user.getProfileImageBase64()!= null && !user.getProfileImageBase64().isEmpty()) {
                try {
                    byte[] imageBytes = Base64.getDecoder().decode(user.getProfileImageBase64());
                    InputStream is = new ByteArrayInputStream(imageBytes);
                    Image profileImage = new Image(is);
                    imageView.setImage(profileImage);
                    double radius = Math.min(imageView.getFitWidth(), imageView.getFitHeight()) /2;
                    Circle clip = new Circle(radius, radius, radius);
                    imageView.setClip(clip);

                } catch (Exception e) {
                    System.err.println("Error loading base64 image: " + e.getMessage());
                    loadDefaultProfileImage();
                }
            } else {
                loadDefaultProfileImage();
            }
        }
    }

    private void loadDefaultProfileImage() {
        try {
            InputStream imageStream = getClass().getResourceAsStream("/org/example/snappfrontend/images/default_profile.png");
            if (imageStream != null) {
                Image defaultImage = new Image(imageStream);
                imageView.setImage(defaultImage);
                double radius = Math.min(imageView.getFitWidth(), imageView.getFitHeight()) / 2;
                Circle clip = new Circle(radius, radius, radius);
                imageView.setClip(clip);

            } else {
                System.err.println("Default image not found.");
            }
        } catch (Exception e) {
            System.err.println("Exception loading default image: " + e.getMessage());
        }
    }

    @FXML
    void handleUploadImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select an Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(((Stage) ((Node) event.getSource()).getScene().getWindow()));
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
                base64ImageString = Base64.getEncoder().encodeToString(bytes);
                errorLabel.setText("Image uploaded successfully.");
                PauseTransition pause = new PauseTransition(Duration.seconds(2));
                pause.setOnFinished(event2 -> errorLabel.setText(""));
                pause.play();
                editButton.setVisible(false);
                saveButton.setVisible(true);
                setFieldsEditable(true);
            } catch (Exception e) {
                e.printStackTrace();
                errorLabel.setText("Image upload failed");
            }
        }
    }


    @FXML
    private void handleEditProfile(ActionEvent event) {
        setFieldsEditable(true);
        editButton.setVisible(false);
        saveButton.setVisible(true);
        errorLabel.setText("You can now edit your profile.");
    }

    @FXML
    private void handleSaveProfile(ActionEvent event) {
        errorLabel.setText("Editing your profile...");
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> errorLabel.setText("Login token missing"));
                    return;
                }

                AuthProfileUpdateRequestDTO dto = new AuthProfileUpdateRequestDTO();
                dto.full_name = nameField.getText();
                dto.phone = numberField.getText();
                dto.email = emailField.getText();
                dto.address = addressField.getText();

                if (base64ImageString != null) {
                    dto.profileImageBase64 = base64ImageString;
                }

                AuthProfileUpdateRequestDTO.BankInfo bankInfo = new AuthProfileUpdateRequestDTO.BankInfo();
                bankInfo.bank_name = bankField.getText();
                bankInfo.account_number = accountField.getText();
                dto.bank_info = bankInfo;

                String jsonBody = JsonUtil.getObjectMapper().writeValueAsString(dto);
                Optional<HttpResponse<String>> responseOpt = ApiClient.put("/auth/profile", jsonBody, token);

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            errorLabel.setText(rootNode.has("message") ? rootNode.get("message").asText() : "Profile updated successfully.");
                            PauseTransition pause = new PauseTransition(Duration.seconds(3));
                            pause.setOnFinished(event2 -> errorLabel.setText(""));
                            pause.play();
                            setFieldsEditable(false);
                            saveButton.setVisible(false);
                            editButton.setVisible(true);
                            base64ImageString = null;
                            loadUserProfile();
                        } else {
                            String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "Something went wrong";
                            errorLabel.setText( errorMessage);
                        }
                    });
                } else {
                    Platform.runLater(() -> errorLabel.setText("Not connected to server"));
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    errorLabel.setText(e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }
    @FXML
    private void handleGoHome(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/snappfrontend/pages/HomePage.fxml"));
            Parent homeRoot = loader.load();
            Scene homeScene = new Scene(homeRoot);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(homeScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Can not load HomePage");
        }
    }
}
