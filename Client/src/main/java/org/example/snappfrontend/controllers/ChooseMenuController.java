package org.example.snappfrontend.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import javafx.util.Duration;
import javafx.animation.PauseTransition;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import org.example.snappfrontend.dto.MenuResponse;
import org.example.snappfrontend.utils.ApiClient;
import org.example.snappfrontend.utils.AuthManager;
import org.example.snappfrontend.utils.JsonUtil;

public class ChooseMenuController {
    @FXML private ComboBox<String> menuComboBox;
    @FXML private Button submitButton;
    @FXML private Button backButton;
    private long restaurantId;
    private long foodId;
    private ExecutorService executorService;
    private boolean isSubmitted = false;
    public void setData(long restaurantId, long foodId, ExecutorService executorService) {
        this.restaurantId = restaurantId;
        this.foodId = foodId;
        this.executorService = executorService;
        initialize();
    }
    private void initialize() {
        loadMenus();
        submitButton.setOnAction(event -> addItemToMenu());
        backButton.setOnAction(event -> backHandle());
    }
    private void loadMenus() {
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                String apiUrl = "/restaurants/" + restaurantId + "/menus/list";
                Optional<HttpResponse<String>> responseOpt = ApiClient.get(apiUrl, token);
                if (responseOpt.isPresent()) {
                    int statusCode = responseOpt.get().statusCode();
                    String responseBody = responseOpt.get().body();
                    if (statusCode == 200) {
                        List<MenuResponse> menus = JsonUtil.getObjectMapper().readValue(
                                responseBody, new TypeReference<List<MenuResponse>>() {});
                        List<String> menuItems = menus.stream()
                                .map(m -> m.getId() + ": " + m.getTitle())
                                .toList();
                        Platform.runLater(() -> {
                            menuComboBox.setItems(FXCollections.observableArrayList(menuItems));
                            if (!menuItems.isEmpty()) {
                                menuComboBox.setValue(menuItems.get(0));
                            } else {
                                showAlert("Warning", "No menus found for this restaurant", AlertType.WARNING);
                            }
                        });
                    } else {
                        Platform.runLater(() -> showAlert("Error", "Failed to load menus: " + statusCode + " - " + responseBody, AlertType.ERROR));
                    }
                } else {
                    Platform.runLater(() -> showAlert("Error", "No response from server", AlertType.ERROR));
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert("Error", "Error loading menus: " + e.getMessage(), AlertType.ERROR));
            }
        });
    }

    private void addItemToMenu() {
        String selectedMenu = menuComboBox.getValue();
        if (selectedMenu == null || selectedMenu.isEmpty()) {
            showAlert("Error", "Please select a menu", AlertType.ERROR);
            return;
        }
        String menuTitle = selectedMenu.split(":")[1].trim();
        String encodedMenuTitle = URLEncoder.encode(menuTitle, StandardCharsets.UTF_8);
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                String requestBody = String.format("{\"item_id\": %d}", foodId);
                Optional<HttpResponse<String>> responseOpt = ApiClient.put(
                        "/restaurants/" + restaurantId + "/menu/" + encodedMenuTitle,
                        requestBody,
                        token
                );
                if (responseOpt.isPresent()) {
                    int statusCode = responseOpt.get().statusCode();
                    String responseBody = responseOpt.get().body();
                    Platform.runLater(() -> {
                        if (statusCode == 200) {
                            showAlert("Success", "Food item added to menu successfully", AlertType.INFORMATION);
                            isSubmitted = true;
                            PauseTransition pause = new PauseTransition(Duration.seconds(2));
                            pause.setOnFinished(e -> {
                                if (isSubmitted) {
                                    backHandle();
                                }
                            });
                            pause.play();
                        } else {
                            showAlert("Error", "Failed to add item to menu: " + statusCode + " - " + responseBody, AlertType.ERROR);
                        }
                    });
                } else {
                    Platform.runLater(() -> showAlert("Error", "No response from server", AlertType.ERROR));
                }
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error", "Error adding item to menu: " + e.getMessage(), AlertType.ERROR));
            }
        });
    }
    @FXML
    private void backHandle() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/snappfrontend/pages/SellersPage.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
            TabPane tabPane = loader.getController() instanceof SellersController ? ((SellersController) loader.getController()).getMainTabPane() : null;
            if (tabPane != null) {
                tabPane.getSelectionModel().select(1);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load seller page: " + e.getMessage(), AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}