package org.example.snappfrontend.controllers;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.example.snappfrontend.models.Restaurant;
import org.example.snappfrontend.models.RestaurantTableModel;
import org.example.snappfrontend.utils.ApiClient;
import org.example.snappfrontend.utils.AuthManager;
import org.example.snappfrontend.utils.JsonUtil;
import org.example.snappfrontend.utils.SceneLoader;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomePageController {
    public Button selectButton;
    @FXML private TableView<RestaurantTableModel> Tableviewrestaurants;
    @FXML private TableColumn<RestaurantTableModel, Number> id;
    @FXML private TableColumn<RestaurantTableModel, String> name;
    @FXML private TableColumn<RestaurantTableModel, String> address;
    @FXML private TableColumn<RestaurantTableModel, String> phone;
    @FXML private TableColumn<RestaurantTableModel, ImageView> logo;

    @FXML private Button adminsButton;
    @FXML private Button trackingButton;
    @FXML private Button couriersButton;
    @FXML private Button historyButton;
    @FXML private Button logoutButton;
    @FXML private Button profileButton;
    @FXML private Label errorLabel;
    @FXML private Button searchButton;
    @FXML private TextField searchField;
    @FXML private Button sellersButton;
    @FXML private Button walletButton;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();


    @FXML
    private void initialize() {
        setupTableColumns();
        loadRestaurants("");
        searchButton.setOnAction(event -> {
            String search = searchField.getText().trim();
            loadRestaurants(search);
        });
    }

    private void setupTableColumns() {
        id.setCellValueFactory(cell -> cell.getValue().idProperty());
        name.setCellValueFactory(cell -> cell.getValue().nameProperty());
        address.setCellValueFactory(cell -> cell.getValue().addressProperty());
        phone.setCellValueFactory(cell -> cell.getValue().phoneProperty());
        logo.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getLogo()));
    }
    @FXML
    private void loadRestaurants(String searchQuery) {
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> errorLabel.setText("Authentication token missing. Please log in again."));
                    return;
                }
                Map<String, String> requestBody = new HashMap<>();
                requestBody.put("search", searchQuery);
                String jsonBody = JsonUtil.getObjectMapper().writeValueAsString(requestBody);
                Optional<HttpResponse<String>> responseOpt = ApiClient.post("/vendors", jsonBody, token);
                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            try {
                                List<Restaurant> restaurants = JsonUtil.getObjectMapper()
                                        .readerFor(new TypeReference<List<Restaurant>>() {})
                                        .readValue(rootNode);

                                ObservableList<RestaurantTableModel> tableData = FXCollections.observableArrayList();
                                for (Restaurant r : restaurants) {
                                    tableData.add(new RestaurantTableModel(r));
                                }
                                Tableviewrestaurants.setItems(tableData);
                            } catch (IOException e) {
                                errorLabel.setText("Error parsing data: " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            String error = rootNode.has("error") ? rootNode.get("error").asText() : "Unknown error.";
                            errorLabel.setText("Error loading: " + error);
                        }
                    });
                } else {
                    Platform.runLater(() -> errorLabel.setText("Failed to connect to server."));
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    errorLabel.setText("Exception: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }


    @FXML
    private void handleSelectRestaurant() {
        RestaurantTableModel selected = Tableviewrestaurants.getSelectionModel().getSelectedItem();
        if (selected == null) {
            errorLabel.setText("Please select a restaurant.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/snappfrontend/pages/MenuPage.fxml"));
            Parent root = loader.load();
            MenuPageController controller = loader.getController();
            controller.setRestaurantId(selected.idProperty().get());
            Stage stage = (Stage) Tableviewrestaurants.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            errorLabel.setText("Failed to load menu page.");
        }
    }

    @FXML
    void handleSearch(ActionEvent event) {
    }

    @FXML
    void adminsHandle(ActionEvent event) {
            if (Objects.equals(AuthManager.getCurrentUserRole(), "ADMIN")) {
                SceneLoader.loadScene("/org/example/snappfrontend/pages/AdminPage.fxml", adminsButton);
            } else {
                errorLabel.setText("You are not allowed to access this page.");
            }
    }

    @FXML
    void walletHandle(ActionEvent event) {
        if (Objects.equals(AuthManager.getCurrentUserRole(), "BUYER")) {
            SceneLoader.loadScene("/org/example/snappfrontend/pages/WalletTopUpPage.fxml", walletButton);
        }
        else {
            errorLabel.setText("You are not allowed to access this page.");
        }
    }

    @FXML
    void trackingHandler(ActionEvent event) {
        if (Objects.equals(AuthManager.getCurrentUserRole(), "BUYER")) {
            SceneLoader.loadScene("/org/example/snappfrontend/pages/TrackingPage.fxml", trackingButton);
        }
        else {
            errorLabel.setText("You are not allowed to access this page.");
        }
    }

    @FXML
    void couriersHandle(ActionEvent event) {
        if (Objects.equals(AuthManager.getCurrentUserRole(), "COURIER")) {
            SceneLoader.loadScene("/org/example/snappfrontend/pages/CourierPage.fxml", couriersButton);
        }
        else {
            errorLabel.setText("You are not a Courier.");
        }
    }


    @FXML
    void historyHandle(ActionEvent event) {
        if (Objects.equals(AuthManager.getCurrentUserRole(), "BUYER")) {
            SceneLoader.loadScene("/org/example/snappfrontend/pages/HistoryPage.fxml", historyButton);
        }
    }

    @FXML
    void logoutHandle(ActionEvent event) {
        AuthManager.logout();
        SceneLoader.loadScene("/org/example/snappfrontend/pages/LoginPage.fxml", logoutButton);
    }

    @FXML
    void prifileHandle(ActionEvent event) {
        if (Objects.equals(AuthManager.getCurrentUserRole(), "ADMIN")) {
            errorLabel.setText("You are not allowed to access this page.");
        }
        else {
            SceneLoader.loadScene("/org/example/snappfrontend/pages/ProfilePage.fxml", profileButton);
        }
    }

    @FXML
    void sellersHandle(ActionEvent event) {
        if (Objects.equals(AuthManager.getCurrentUserRole(), "BUYER") || Objects.equals(AuthManager.getCurrentUserRole(), "COURIER") || Objects.equals(AuthManager.getCurrentUserRole(), "ADMIN")) {
            errorLabel.setText("You are not allowed ");
        }
        else {
            SceneLoader.loadScene("/org/example/snappfrontend/pages/SellersPage.fxml", sellersButton);
        }
    }
}