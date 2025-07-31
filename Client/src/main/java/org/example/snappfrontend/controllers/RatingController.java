package org.example.snappfrontend.controllers;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.snappfrontend.utils.AuthManager;
import org.example.snappfrontend.utils.ApiClient;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.List;


public class RatingController {
    @FXML private Button handleImageButton;
    @FXML private TextField commentField;
    @FXML private Button homeButton;
    @FXML private ImageView imageField;
    @FXML private TextField orderIDField;
    @FXML private ComboBox<Integer> ratingField;
    @FXML private Button submitButton;
    private String base64ImageString;
    private Long orderId;

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
        if (orderIDField != null) {
            orderIDField.setText(orderId.toString());
            orderIDField.setDisable(true);
        }
    }

    @FXML
    public void initialize() {
        ratingField.getItems().addAll(1, 2, 3, 4, 5);
    }
    @FXML
    void handleImageButtonAction(ActionEvent event) {
        handleUploadImage(event);
    }

    private void handleUploadImage(ActionEvent event) {
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
                imageField.setImage(resizedFxImage);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(resized, "jpg", baos);
                byte[] bytes = baos.toByteArray();

                base64ImageString = Base64.getEncoder().encodeToString(bytes);
                showAlert("Image uploaded", "Image resized and encoded successfully.");
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error", "Image upload failed: " + e.getMessage());
            }
        }
    }


    @FXML
    private void handleSubmit() {
        String token = AuthManager.getJwtToken();
        if (token == null || token.isEmpty()) {
            showAlert("Authentication", "You must be logged in to rate.");
            return;
        }

        try {
            if (orderId == null) {
                showAlert("Validation", "Order ID is missing.");
                return;
            }

            Integer rating = ratingField.getValue();
            String comment = commentField.getText().trim();

            if (rating == null || comment.isEmpty()) {
                showAlert("Validation", "Please provide both rating and comment.");
                return;
            }
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("order_id", orderId);
            requestMap.put("rating", rating);
            requestMap.put("comment", comment);
            requestMap.put("imageBase64", base64ImageString != null ? List.of(base64ImageString) : List.of());

            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(requestMap);

            Optional<HttpResponse<String>> responseOpt = ApiClient.post("/ratings", json, token);
            if (responseOpt.isPresent()) {
                HttpResponse<String> response = responseOpt.get();
                if (response.statusCode() == 200) {
                    showAlert("Success", "Rating submitted successfully!");
                    clearFields();
                } else {
                    showAlert("Error", "Failed to submit rating: " + response.body());
                }
            } else {
                showAlert("Error", "No response from server.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Exception", "An error occurred: " + e.getMessage());
        }
    }

    private void clearFields() {
        commentField.clear();
        ratingField.getSelectionModel().clearSelection();
        imageField.setImage(null);
        base64ImageString = null;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    @FXML
    void homeHandle(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/snappfrontend/pages/HomePage.fxml"));
            Parent homeRoot = loader.load();
            Scene homeScene = new Scene(homeRoot);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(homeScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}