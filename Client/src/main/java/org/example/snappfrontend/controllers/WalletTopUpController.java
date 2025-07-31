package org.example.snappfrontend.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.snappfrontend.utils.AuthManager;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;


public class WalletTopUpController {
    @FXML private Label currentWalletBalanceLabel;
    @FXML private Label errorLabel;
    @FXML private Button homeButton;
    @FXML private Button submitButton;
    @FXML private TextField topUpAmountField;

    @FXML
    public void initialize() {
        loadWalletBalance();
    }

    private void loadWalletBalance() {
        String url = "http://localhost:8080/auth/profile";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + AuthManager.getJwtToken())
                .GET()
                .build();

        HttpClient client = HttpClient.newHttpClient();
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            ObjectMapper mapper = new ObjectMapper();
                            JsonNode root = mapper.readTree(response.body());
                            double balance = root.get("walletbalance").asDouble();
                            PauseTransition pause = new PauseTransition(Duration.seconds(3));
                            pause.setOnFinished(event -> errorLabel.setText(""));
                            pause.play();
                            Platform.runLater(() -> currentWalletBalanceLabel.setText(String.format("%.2f", balance)));
                        } catch (Exception e) {
                            Platform.runLater(() -> errorLabel.setText("Failed to parse balance."));
                        }
                    } else {
                        Platform.runLater(() -> errorLabel.setText("Failed to load wallet balance."));
                    }
                })
                .exceptionally(e -> {
                    Platform.runLater(() -> errorLabel.setText("Error loading balance."));
                    return null;
                });
    }

    @FXML
    void handleSubmit(ActionEvent event) {
        errorLabel.setText("");
        String amountText = topUpAmountField.getText().trim();
        if (amountText.isEmpty()) {
            errorLabel.setText("Please enter an amount.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                errorLabel.setText("Amount must be greater than 0.");
                return;
            }
        } catch (NumberFormatException e) {
            errorLabel.setText("Invalid amount.");
            return;
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("amount", amount);
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(payload);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/wallet/top-up"))
                    .header("Authorization", "Bearer " + AuthManager.getJwtToken())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                errorLabel.setText("Top-up successful.");
                topUpAmountField.clear();
                loadWalletBalance();
            } else {
                errorLabel.setText("Top-up failed.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Error during top-up.");
        }
    }

    @FXML
    void handleHome(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/snappfrontend/pages/HomePage.fxml"));
            Parent homeRoot = loader.load();
            Scene homeScene = new Scene(homeRoot);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(homeScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Failed to return to Home Page.");
        }
    }
}