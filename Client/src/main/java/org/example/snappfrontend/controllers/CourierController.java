package org.example.snappfrontend.controllers;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.cell.ComboBoxTableCell;
import org.example.snappfrontend.models.Order;
import org.example.snappfrontend.utils.AuthManager;
import org.example.snappfrontend.utils.ApiClient;
import org.example.snappfrontend.utils.JsonUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.scene.control.TabPane;
import javafx.scene.Node;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class CourierController {

    @FXML private ListView<String> actionList;
    @FXML private Label errorLabel;
    @FXML private TabPane mainTabPane;
    @FXML private TableView<Order> availableDeliveriesTable;
    @FXML private TableColumn<Order, Integer> availableOrderIdColumn;
    @FXML private TableColumn<Order, Integer> availableVendorIdColumn;
    @FXML private TableColumn<Order, String> availableDeliveryAddressColumn;
    @FXML private TableColumn<Order, String> availableStatusColumn;
    @FXML private TableColumn<Order, String> availableDeliveryStatus;
    @FXML private TableView<Order> deliveryHistoryTable;
    @FXML private TableColumn<Order, Integer> historyOrderIdColumn;
    @FXML private TableColumn<Order, Integer> historyVendorIdColumn;
    @FXML private TableColumn<Order, String> historyDeliveryAddressColumn;
    @FXML private TableColumn<Order, String> historyStatusColumn;
    @FXML private TableColumn<Order, String> historyUpdatedAtColumn;

    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final List<Order> historyOrders = new ArrayList<>();


    @FXML
    public void initialize() {
        ObservableList<String> actions = FXCollections.observableArrayList("Available Deliveries", "Delivery History");
        availableDeliveriesTable.setEditable(true);
        actionList.setItems(actions);
        actionList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) handleActionSelection(newValue);
        });
        if (availableOrderIdColumn != null)
            availableOrderIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (availableVendorIdColumn != null)
            availableVendorIdColumn.setCellValueFactory(new PropertyValueFactory<>("vendorId"));
        if (availableDeliveryAddressColumn != null)
            availableDeliveryAddressColumn.setCellValueFactory(new PropertyValueFactory<>("deliveryAddress"));
        if (availableStatusColumn != null)
            availableStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        if (availableDeliveryStatus != null) {
            availableDeliveryStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDeliveryStatus()));
            availableDeliveryStatus.setCellFactory(ComboBoxTableCell.forTableColumn("ACCEPTED", "RECEIVED", "DELIVERED"));
            availableDeliveryStatus.setOnEditCommit(event -> {
                Order order = event.getRowValue();
                String newStatus = event.getNewValue();
                order.setDeliveryStatus(newStatus);
                updateDeliveryStatus(order.getId(), newStatus);
            });
        }
        if (historyOrderIdColumn != null)
            historyOrderIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (historyVendorIdColumn != null)
            historyVendorIdColumn.setCellValueFactory(new PropertyValueFactory<>("vendorId"));
        if (historyDeliveryAddressColumn != null)
            historyDeliveryAddressColumn.setCellValueFactory(new PropertyValueFactory<>("deliveryAddress"));
        if (historyStatusColumn != null)
            historyStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        if (historyUpdatedAtColumn != null)
            historyUpdatedAtColumn.setCellValueFactory(new PropertyValueFactory<>("updatedAt"));
        viewAvailableDeliveries();
        viewDeliveryHistory();
    }

    private void handleActionSelection(String action) {
        if ("Available Deliveries".equals(action)) {
            if (mainTabPane != null) mainTabPane.getSelectionModel().select(0);
            viewAvailableDeliveries();
        } else if ("Delivery History".equals(action)) {
            if (mainTabPane != null) mainTabPane.getSelectionModel().select(1);
            viewDeliveryHistory();
        }
    }

    @FXML
    private void viewAvailableDeliveries() {
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> errorLabel.setText("Authentication token missing."));
                    return;
                }
                Optional<HttpResponse<String>> responseOpt = ApiClient.get("/deliveries/available", token);
                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());
                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            try {
                                List<Order> orders = JsonUtil.getObjectMapper().readerForListOf(Order.class).readValue(rootNode);
                                ObservableList<Order> orderList = FXCollections.observableArrayList(orders);
                                availableDeliveriesTable.setItems(orderList);
                            } catch (IOException e) {
                                errorLabel.setText("Parsing error: " + e.getMessage());
                                e.printStackTrace();
                            }
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

    private void updateDeliveryStatus(Long orderId, String newStatus) {
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> errorLabel.setText("Authentication token missing."));
                    return;
                }
                Map<String, String> requestData = Map.of("status", newStatus.toLowerCase());
                String json = JsonUtil.getObjectMapper().writeValueAsString(requestData);
                Optional<HttpResponse<String>> responseOpt = ApiClient.patch("/deliveries/" + orderId, json, token);
                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    if (response.statusCode() == 200) {
                        JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());
                        Order updatedOrder = JsonUtil.getObjectMapper().treeToValue(rootNode.get("order"), Order.class);
                        Platform.runLater(() -> {
                            updatedOrder.setDeliveryStatus(newStatus);
                            addToHistory(updatedOrder);
                            viewDeliveryHistory();
                            if ("DELIVERED".equalsIgnoreCase(newStatus)) {
                                availableDeliveriesTable.getItems().removeIf(o -> o.getId().equals(orderId));
                            } else {
                                for (int i = 0; i < availableDeliveriesTable.getItems().size(); i++) {
                                    if (availableDeliveriesTable.getItems().get(i).getId().equals(orderId)) {
                                        availableDeliveriesTable.getItems().set(i, updatedOrder);
                                        break;
                                    }
                                }
                            }
                        });
                    } else {
                        Platform.runLater(() -> errorLabel.setText("Update failed: " + response.body()));
                    }
                } else {
                    Platform.runLater(() -> errorLabel.setText("No response from server."));
                }
            } catch (Exception e) {
                Platform.runLater(() -> errorLabel.setText("Error updating status: " + e.getMessage()));
                e.printStackTrace();
            }
        });
    }

    private void addToHistory(Order order) {
        if (deliveryHistoryTable != null) {
            ObservableList<Order> currentHistory = deliveryHistoryTable.getItems();
            if (currentHistory == null) {
                currentHistory = FXCollections.observableArrayList();
                deliveryHistoryTable.setItems(currentHistory);
            }
            currentHistory.add(order);
        }
    }

    @FXML
    private void viewDeliveryHistory() {
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> errorLabel.setText("Authentication token missing."));
                    return;
                }
                Optional<HttpResponse<String>> responseOpt = ApiClient.get("/deliveries/history", token);
                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());
                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            try {
                                List<Order> orders = JsonUtil.getObjectMapper().readerForListOf(Order.class).readValue(rootNode);
                                ObservableList<Order> orderList = FXCollections.observableArrayList(orders);
                                deliveryHistoryTable.setItems(orderList);
                            } catch (IOException e) {
                                errorLabel.setText("Error parsing delivery history: " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            String msg = rootNode.has("error") ? rootNode.get("error").asText() : "Unknown error.";
                            errorLabel.setText("Error: " + msg);
                        }
                    });
                } else {
                    Platform.runLater(() -> errorLabel.setText("Failed to connect to server."));
                }
            } catch (Exception e) {
                Platform.runLater(() -> errorLabel.setText("Exception: " + e.getMessage()));
                e.printStackTrace();
            }
        });
    }


    @FXML
    private void handleHomeButton(ActionEvent event) {
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