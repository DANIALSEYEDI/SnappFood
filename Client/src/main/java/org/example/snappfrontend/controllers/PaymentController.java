package org.example.snappfrontend.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import org.example.snappfrontend.utils.AuthManager;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
public class PaymentController {

    @FXML private Label additionalFeeLabel;
    @FXML private ComboBox<String> choosemethod;
    @FXML private Label courierFeeLabel;
    @FXML private Button payButton;
    @FXML private Label payPriceLabel;
    @FXML private Label taxFeeLabel;
    @FXML private Label rawPriceLabel;
    @FXML private Label walletLabel;
    @FXML private Label errorLabel;
    @FXML private Button homeButton;

    private Long currentOrderId;
    private Double currentPayPrice;
    private Double additionalFee;
    private Double courierFee;
    private Double taxFee;
    private Double rawPrice;

    @FXML
    public void initialize() {
        // Initialize payment method options
        choosemethod.setItems(FXCollections.observableArrayList("wallet", "online"));
        choosemethod.getSelectionModel().selectFirst();

        // Load user's wallet balance
        try {
            loadUserWallet();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            errorLabel.setText("Failed to load wallet balance.");
        }
    }

    public void setOrderDetails(Long orderId, Double payPrice, Double additionalFee, Double courierFee, Double taxFee, Double rawPrice) {
        this.currentOrderId = orderId;
        this.currentPayPrice = payPrice;
        this.additionalFee = additionalFee != null ? additionalFee : 0.0;
        this.courierFee = courierFee != null ? courierFee : 0.0;
        this.taxFee = taxFee != null ? taxFee : 0.0;
        this.rawPrice = rawPrice != null ? rawPrice : 0.0; // Fixed assignment logic
        updatePriceLabels();
    }


    private void updatePriceLabels() {
        payPriceLabel.setText(currentPayPrice != null ? String.format("%.2f", currentPayPrice) : "0.00");
        additionalFeeLabel.setText(additionalFee != null ? String.format("%.2f", additionalFee) : "0.00");
        courierFeeLabel.setText(courierFee != null ? String.format("%.2f", courierFee) : "0.00");
        taxFeeLabel.setText(taxFee != null ? String.format("%.2f", taxFee) : "0.00");
        rawPriceLabel.setText(rawPrice != null ? String.format("%.2f", rawPrice) : "0.00");
    }

    // Load user's wallet balance
    private void loadUserWallet() throws IOException, InterruptedException {
        String url = "http://localhost:8080/auth/profile";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + AuthManager.getJwtToken())
                .GET()
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(response.body());
            double wallet = node.get("walletbalance").asDouble();
            walletLabel.setText(String.format("%.2f", wallet));
        } else {
            walletLabel.setText("Failed to load wallet");
            errorLabel.setText("Error fetching wallet balance: HTTP " + response.statusCode());
        }
    }

    @FXML
    private void pay() {
        errorLabel.setText("");

        if (currentOrderId == null || currentPayPrice == null) {
            errorLabel.setText("No order to pay.");
            return;
        }

        String method = choosemethod.getValue();
        if (method == null || method.isEmpty()) {
            errorLabel.setText("Please select a payment method.");
            return;
        }

        Map<String, Object> paymentPayload = new HashMap<>();
        paymentPayload.put("order_id", currentOrderId);
        paymentPayload.put("method", method);

        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(paymentPayload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/payment/online"))
                    .header("Authorization", "Bearer " + AuthManager.getJwtToken())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                errorLabel.setText("Payment successful.");
                loadUserWallet();
                currentOrderId = null;
            } else {
                JsonNode rootNode = mapper.readTree(response.body());
                String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "Payment failed.";
                errorLabel.setText(errorMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Error during payment: " + e.getMessage());
        }
    }

    @FXML
    private void homeHandle(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/snappfrontend/pages/HomePage.fxml"));
            Parent homeRoot = loader.load();
            Scene homeScene = new Scene(homeRoot);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(homeScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Failed to load Home Page.");
        }
    }
}