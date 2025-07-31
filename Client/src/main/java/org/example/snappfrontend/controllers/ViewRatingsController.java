package org.example.snappfrontend.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.example.snappfrontend.models.FoodRatingModel;
import javafx.application.Platform;
import org.example.snappfrontend.utils.ApiClient;
import org.example.snappfrontend.utils.AuthManager;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ViewRatingsController {

    @FXML private TableView<FoodRatingModel> ratingTable;
    @FXML private TableColumn<FoodRatingModel, Long> IDColumn;
    @FXML private TableColumn<FoodRatingModel, Integer> ratingColumn;
    @FXML private TableColumn<FoodRatingModel, String> commentColumn;
    @FXML private TableColumn<FoodRatingModel, String> createdAtColumn;
    @FXML private TableColumn<FoodRatingModel, Long> userIDColumn;
    @FXML private TableColumn<FoodRatingModel, ImageView> foodImageColumn;
    @FXML private Label averageRatingLabel;
    @FXML private Label errorLabel;
    @FXML private Button backButton;

    private Long foodId;
    private Long restaurantId;
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);

    public void setFoodId(Long foodId, Long restaurantId) {
        if (foodId == null || foodId <= 0) {
            errorLabel.setText("Invalid food ID.");
            return;
        }
        this.foodId = foodId;
        this.restaurantId = restaurantId;
        System.out.println("Selected food ID: " + foodId + ", Restaurant ID: " + restaurantId);
        loadRatingsForFood(foodId);
    }

    @FXML
    public void initialize() {
        if (ratingTable == null) {
            System.err.println("Error: ratingTable is null. Check FXML file for fx:id='ratingTable'.");
            return;
        }
        IDColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        ratingColumn.setCellValueFactory(cellData -> cellData.getValue().ratingProperty().asObject());
        commentColumn.setCellValueFactory(cellData -> cellData.getValue().commentProperty());
        createdAtColumn.setCellValueFactory(cellData -> cellData.getValue().createdAtProperty());
        userIDColumn.setCellValueFactory(cellData -> cellData.getValue().userIdProperty().asObject());

        foodImageColumn.setCellValueFactory(cellData -> cellData.getValue().foodImageProperty());
        foodImageColumn.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(ImageView imageView, boolean empty) {
                super.updateItem(imageView, empty);
                if (empty || imageView == null) {
                    setGraphic(null);
                } else {
                    imageView.setFitHeight(100);
                    imageView.setFitWidth(100);
                    setGraphic(imageView);
                }
            }
        });

        ratingTable.setPlaceholder(new Label("No ratings available."));
    }

    private void loadRatingsForFood(Long foodId) {
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> errorLabel.setText("Authentication token missing. Please log in."));
                    redirectToLogin();
                    return;
                }
                Optional<HttpResponse<String>> responseOpt = ApiClient.get("/ratings/items/" + foodId, token);
                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = ApiClient.getObjectMapper().readTree(response.body());
                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            JsonNode avgNode = rootNode.get("avg_rating");
                            if (avgNode != null && !avgNode.isNull()) {
                                averageRatingLabel.setText(String.format("%.2f", avgNode.asDouble()));
                            } else {
                                averageRatingLabel.setText("N/A");
                            }
                            JsonNode commentsNode = rootNode.get("comments");
                            if (commentsNode != null && commentsNode.isArray()) {
                                ObservableList<FoodRatingModel> ratings = FXCollections.observableArrayList();
                                for (JsonNode node : commentsNode) {
                                    FoodRatingModel model = new FoodRatingModel();
                                    model.setId(node.get("id").asLong());
                                    model.setRating(node.get("rating").asInt());
                                    model.setComment(node.get("comment").asText());
                                    model.setCreatedAt(node.get("createdAt").asText());
                                    model.setUserId(node.get("user_id").asLong());
                                    if (node.has("imageBase64") && node.get("imageBase64").isArray() && node.get("imageBase64").size() > 0) {
                                        String base64 = node.get("imageBase64").get(0).asText().replaceAll("\\s+", "");
                                        if (!base64.isEmpty()) {
                                            try {
                                                byte[] imageBytes = Base64.getDecoder().decode(base64);
                                                Image image = new Image(new ByteArrayInputStream(imageBytes));
                                                ImageView imageView = new ImageView(image);
                                                model.setFoodImage(imageView);
                                            } catch (IllegalArgumentException e) {
                                                System.err.println("Error decoding image: " + e.getMessage());
                                            }
                                        }
                                    }
                                    ratings.add(model);
                                }
                                ratingTable.setItems(ratings);
                            } else {
                                ratingTable.setPlaceholder(new Label("No ratings found."));
                            }
                        } else if (response.statusCode() == 401) {
                            errorLabel.setText("Unauthorized. Please log in again.");
                            redirectToLogin();
                        } else {
                            String msg = rootNode.has("error") ? rootNode.get("error").asText() : "Unknown error.";
                            errorLabel.setText("Error: " + msg);
                        }
                    });
                } else {
                    Platform.runLater(() -> errorLabel.setText("No response from server."));
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    errorLabel.setText("Exception: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }

    private void redirectToLogin() {
        Platform.runLater(() -> {
            try {
                AuthManager.logout();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/snappfrontend/pages/LoginPage.fxml"));
                Parent loginRoot = loader.load();
                Scene scene = new Scene(loginRoot);
                Stage stage = (Stage) ratingTable.getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                errorLabel.setText("Error redirecting to login: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @FXML
    public void backHandle(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/snappfrontend/pages/MenuPage.fxml"));
            Parent menuRoot = loader.load();
            MenuPageController controller = loader.getController();
            controller.setRestaurantId(restaurantId);
            Scene scene = new Scene(menuRoot);
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException | InterruptedException e) {
            errorLabel.setText("Error loading menu: " + e.getMessage());
            e.printStackTrace();
        } finally {
            executorService.shutdown();
        }
    }
}