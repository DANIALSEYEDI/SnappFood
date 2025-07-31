package org.example.snappfrontend.controllers;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.example.snappfrontend.models.OrderTrackingResponse;
import org.example.snappfrontend.utils.AuthManager;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class TrackingController {


    @FXML private TableView<OrderTrackingResponse> transactionsTable;
    @FXML private TableColumn<OrderTrackingResponse, Long> orderIDColumn;
    @FXML private TableColumn<OrderTrackingResponse, String> restaurantStatusColumn;
    @FXML private TableColumn<OrderTrackingResponse, String> deliveryStatusColumn;
    @FXML private TableColumn<OrderTrackingResponse, String> StatusColumn;
    @FXML private Button homeButton;
    @FXML private Button ratingButton;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @FXML
    public void initialize() throws IOException, InterruptedException {
        orderIDColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().id));
        restaurantStatusColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getRestaurantStatus() != null ? data.getValue().getRestaurantStatus().toLowerCase().replace("_", " ") : "-"
        ));
        deliveryStatusColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getDeliveryStatus() != null ? data.getValue().getDeliveryStatus().toLowerCase().replace("_", " ") : "-"
        ));
        StatusColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().status));
        loadTrackingData();
    }



    private void loadTrackingData() throws IOException, InterruptedException {
        String url = "http://localhost:8080/orders/history";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + AuthManager.getJwtToken())
                .GET()
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            ObjectMapper mapper = new ObjectMapper();
            List<OrderTrackingResponse> orders = mapper.readValue(response.body(), new TypeReference<>() {});
            transactionsTable.getItems().setAll(orders);
        } else {
            System.out.println("Failed to load tracking data: " + response.statusCode());
        }
    }


    @FXML
    void ratingHandle(ActionEvent event) {
        OrderTrackingResponse selectedOrder = transactionsTable.getSelectionModel().getSelectedItem();
        if (selectedOrder == null) {
            showAlert("No order Select!");
            return;
        }
        if (!"completed".equalsIgnoreCase(selectedOrder.status)) {
            showAlert("Order is Not Completed");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/snappfrontend/pages/RatingPage.fxml"));
            Parent ratingRoot = loader.load();
            RatingController ratingController = loader.getController();
            ratingController.setOrderId(selectedOrder.id);
            Scene ratingScene = new Scene(ratingRoot);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(ratingScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("");
        alert.setHeaderText(null);
        alert.setContentText(message);
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