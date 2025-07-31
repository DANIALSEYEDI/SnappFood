package org.example.snappfrontend.controllers;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.example.snappfrontend.models.OrderHistoryModel;
import org.example.snappfrontend.models.TransactionModel;
import org.example.snappfrontend.utils.AuthManager;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HistoryController {


    @FXML private TableView<OrderHistoryModel> ordertable;
    @FXML private TableColumn<OrderHistoryModel, Number> IDColumn;
    @FXML private TableColumn<OrderHistoryModel, String> addressColumn;
    @FXML private TableColumn<OrderHistoryModel, Number> costumerIDColumn;
    @FXML private TableColumn<OrderHistoryModel, Number> vendorIDColumn;
    @FXML private TableColumn<OrderHistoryModel, Number> rawPriceColumn;
    @FXML private TableColumn<OrderHistoryModel, Number> taxFeeColumn;
    @FXML private TableColumn<OrderHistoryModel, Number> courierFeeColumn;
    @FXML private TableColumn<OrderHistoryModel, Number> additionalFeeColumn;
    @FXML private TableColumn<OrderHistoryModel, Number> payPriceColumn;
    @FXML private TableColumn<OrderHistoryModel, String> statusColumn;
    @FXML private TableColumn<OrderHistoryModel, String> createdAtColumn;
    @FXML private TableColumn<OrderHistoryModel, String> UpdatedAtColumn;
    @FXML private Label errorLabel;
    @FXML private Button homeButton;



    @FXML private TableColumn<TransactionModel, Number> transactionIdColumn;
    @FXML private TableColumn<TransactionModel, String> transactionMethodColumn;
    @FXML private TableColumn<TransactionModel, Number> transactionOrderIdColumn;
    @FXML private TableColumn<TransactionModel, String> transactionStatusColumn;
    @FXML private TableColumn<TransactionModel, Number> transactionUserIdColumn;
    @FXML private TableColumn<TransactionModel, String> transactionAmountColumn;
    @FXML private TableView<TransactionModel> transactionsTable;


    @FXML
    public void initialize() {
        loadTransactions();
        IDColumn.setCellValueFactory(data -> data.getValue().idProperty());
        addressColumn.setCellValueFactory(data -> data.getValue().addressProperty());
        costumerIDColumn.setCellValueFactory(data -> data.getValue().customerIdProperty());
        vendorIDColumn.setCellValueFactory(data -> data.getValue().vendorIdProperty());
        rawPriceColumn.setCellValueFactory(data -> data.getValue().rawPriceProperty());
        taxFeeColumn.setCellValueFactory(data -> data.getValue().taxFeeProperty());
        courierFeeColumn.setCellValueFactory(data -> data.getValue().courierFeeProperty());
        additionalFeeColumn.setCellValueFactory(data -> data.getValue().additionalFeeProperty());
        payPriceColumn.setCellValueFactory(data -> data.getValue().payPriceProperty());
        statusColumn.setCellValueFactory(data -> data.getValue().statusProperty());
        createdAtColumn.setCellValueFactory(data -> data.getValue().createdAtProperty());
        UpdatedAtColumn.setCellValueFactory(data -> data.getValue().updatedAtProperty());
        try {
            loadOrderHistory();
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Failed to load order history");
        }
    }



    private void loadOrderHistory() throws IOException, InterruptedException {
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
            JsonNode root = mapper.readTree(response.body());

            ObservableList<OrderHistoryModel> orders = FXCollections.observableArrayList();
            for (JsonNode node : root) {
                orders.add(new OrderHistoryModel(
                        node.get("id").asLong(),
                        node.get("delivery_address").asText(),
                        node.get("customer_id").asLong(),
                        node.get("vendor_id").asLong(),
                        node.get("raw_price").asInt(),
                        node.get("tax_fee").asInt(),
                        node.get("courier_fee").asInt(),
                        node.get("additional_fee").asInt(),
                        node.get("pay_price").asInt(),
                        node.get("status").asText(),
                        node.get("created_at").asText(),
                        node.get("updated_at").asText()
                ));
            }
            ordertable.setItems(orders);
        } else {
            errorLabel.setText("Error: " + response.statusCode());
        }
    }

    @FXML
    private void loadTransactions() {
        try {
            String url = "http://localhost:8080/transactions";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + AuthManager.getJwtToken())
                    .GET()
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonArray = mapper.readTree(response.body());

                ObservableList<TransactionModel> transactionModels = FXCollections.observableArrayList();
                for (JsonNode json : jsonArray) {
                    String amountStr = json.get("amount").asText();
                    transactionModels.add(new TransactionModel(
                            json.get("id").asLong(),
                            json.get("order_id").asLong(),
                            json.get("user_id").asLong(),
                            json.get("method").asText(),
                            json.get("status").asText(),
                            amountStr
                    ));
                }
                transactionIdColumn.setCellValueFactory(data -> data.getValue().idProperty());
                transactionOrderIdColumn.setCellValueFactory(data -> data.getValue().orderIdProperty());
                transactionUserIdColumn.setCellValueFactory(data -> data.getValue().userIdProperty());
                transactionMethodColumn.setCellValueFactory(data -> data.getValue().methodProperty());
                transactionStatusColumn.setCellValueFactory(data -> data.getValue().statusProperty());
                transactionAmountColumn.setCellValueFactory(data -> data.getValue().amountProperty());
                transactionsTable.setItems(transactionModels);
            } else if (response.statusCode() == 404) {
                errorLabel.setText("No transactions found.");
            } else {
                errorLabel.setText("Failed to load transactions.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Error loading transactions.");
        }
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