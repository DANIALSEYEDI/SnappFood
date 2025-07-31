package org.example.snappfrontend.controllers;
import com.fasterxml.jackson.core.type.TypeReference;
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
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.snappfrontend.models.FoodItemModel;
import org.example.snappfrontend.utils.AuthManager;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MenuPageController {
    @FXML public TableView<FoodItemModel> foodItemTable;
    @FXML public TableColumn<FoodItemModel, Number> foodItemIdColumn;
    @FXML public TableColumn<FoodItemModel, String> foodItemNameColumn;
    @FXML public TableColumn<FoodItemModel, String> foodItemDescription;
    @FXML public TableColumn<FoodItemModel, String> foodItemMenuTitle;
    @FXML public TableColumn<FoodItemModel, Number> foodItemPrice;
    @FXML public TableColumn<FoodItemModel, Number> foodItemSupply;
    @FXML public TableColumn<FoodItemModel, String> foodItemKeywords;
    @FXML public TableColumn<FoodItemModel, Void> selectFoodItem;
    @FXML public TableColumn<FoodItemModel, Void> foodItemchoosequantity;

    @FXML public Button homeButton;
    @FXML public Label errorLabel;
    @FXML public Button payButton;
    @FXML public Button submitorder;
    @FXML public Label totalPriceLabel;
    @FXML public TextField boxforaddress;
    @FXML public TextField boxforcoupon;
    public Button RatingButton;
    public Slider filterprice;

    @FXML private Long restaurantId;
    private Long currentOrderId = null;
    private Double currentPayPrice = null;
    private Double additionalFee = null;
    private Double courierFee = null;
    private Double taxFee = null;
    private Double rawPrice = null;
    private Long selectedFoodId = null;

    @FXML
    public void initialize() {
        RatingButton.setOnAction(event -> {
            try {
                handleGoToRatingPage(event);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        filterprice.valueProperty().addListener((obs, oldVal, newVal) -> {
            try {
                loadMenuForRestaurant(restaurantId);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    public void setRestaurantId(Long id) throws IOException, InterruptedException {
        this.restaurantId = id;
        loadMenuForRestaurant(this.restaurantId);
    }

    public void loadMenuForRestaurant(Long restaurantId) throws IOException, InterruptedException {
        String url = "http://localhost:8080/vendors/" + restaurantId;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + AuthManager.getJwtToken())
                .GET()
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            System.err.println("Failed to load menu: " + response.statusCode());
            return;
        }
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.body());
        JsonNode menuTitles = root.get("menu_titles");
        ObservableList<FoodItemModel> allFoodItems = FXCollections.observableArrayList();
        for (JsonNode titleNode : menuTitles) {
            String title = titleNode.asText();
            JsonNode items = root.get(title);
            if (items != null && items.isArray()) {
                for (JsonNode item : items) {
                    List<String> keywords = mapper.convertValue(item.get("keywords"), new TypeReference<>() {});
                    allFoodItems.add(new FoodItemModel(
                            item.get("id").asLong(),
                            item.get("name").asText(),
                            item.get("description").asText(),
                            item.get("price").asDouble(),
                            item.get("supply").asInt(),
                            keywords,
                            title
                    ));
                }
            }
        }
        double maxPrice = filterprice.getValue() > 0 ? filterprice.getValue() : Double.MAX_VALUE;
        ObservableList<FoodItemModel> filteredItems = allFoodItems.stream()
                .filter(item -> item.getPrice() <= maxPrice)
                .collect(Collectors.collectingAndThen(Collectors.toList(), FXCollections::observableArrayList));
        foodItemIdColumn.setCellValueFactory(data -> data.getValue().idProperty());
        foodItemPrice.setCellValueFactory(data -> data.getValue().priceProperty());
        foodItemSupply.setCellValueFactory(data -> data.getValue().supplyProperty());
        foodItemNameColumn.setCellValueFactory(data -> data.getValue().nameProperty());
        foodItemDescription.setCellValueFactory(data -> data.getValue().descriptionProperty());
        foodItemMenuTitle.setCellValueFactory(data -> data.getValue().menuTitleProperty());
        foodItemKeywords.setCellValueFactory(data -> data.getValue().keywordsAsStringProperty());
        addSelectButtonToTable();
        addQuantitySpinnerToTable();
        foodItemTable.setItems(filteredItems);
        updateTotalPrice();
    }

    private void addQuantitySpinnerToTable() {
        foodItemchoosequantity.setCellFactory(column -> new TableCell<>() {
            private final Spinner<Integer> spinner = new Spinner<>(0, 100, 0);
            {
                spinner.setEditable(true);
                spinner.valueProperty().addListener((obs, oldVal, newVal) -> {
                    FoodItemModel item = getTableView().getItems().get(getIndex());
                    item.setSelectedQuantity(newVal);
                    updateTotalPrice();
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    FoodItemModel model = getTableView().getItems().get(getIndex());
                    spinner.getValueFactory().setValue(model.getSelectedQuantity());
                    setGraphic(spinner);
                }
            }
        });
    }
    private void addSelectButtonToTable() {
        selectFoodItem.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Select");
            {
                btn.setOnAction(e -> {
                    FoodItemModel item = getTableView().getItems().get(getIndex());
                    selectedFoodId = item.idProperty().get();
                    System.out.println("Selected food ID: " + selectedFoodId);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    private void updateTotalPrice() {
        double total = foodItemTable.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getSelectedQuantity())
                .sum();
        totalPriceLabel.setText(String.format("%.2f", total));
    }

    @FXML
    public void handleSubmitOrder() {
        errorLabel.setText("");
        String address = boxforaddress.getText();
        String couponText = boxforcoupon.getText();
        if (address == null || address.isEmpty()) {
            errorLabel.setText("Please enter address.");
            return;
        }
        List<Map<String, Object>> items = new ArrayList<>();
        for (FoodItemModel item : foodItemTable.getItems()) {
            if (item.getSelectedQuantity() > 0) {
                Map<String, Object> orderItem = new HashMap<>();
                orderItem.put("item_id", item.idProperty().get());
                orderItem.put("quantity", item.getSelectedQuantity());
                items.add(orderItem);
            }
        }
        if (items.isEmpty()) {
            errorLabel.setText("No food item selected.");
            return;
        }
        Map<String, Object> orderPayload = new HashMap<>();
        orderPayload.put("delivery_address", address);
        orderPayload.put("vendor_id", restaurantId);
        orderPayload.put("items", items);
        if (!couponText.isEmpty()) {
            try {
                orderPayload.put("coupon_id", Integer.parseInt(couponText));
            } catch (NumberFormatException e) {
                errorLabel.setText("Invalid coupon code.");
                return;
            }
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(orderPayload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/orders"))
                    .header("Authorization", "Bearer " + AuthManager.getJwtToken())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode jsonNode = mapper.readTree(response.body());
                currentOrderId = jsonNode.get("id").asLong();
                currentPayPrice = jsonNode.get("pay_price").asDouble();
                additionalFee = jsonNode.has("additional_fee") ? jsonNode.get("additional_fee").asDouble() : 0.0;
                courierFee = jsonNode.has("courier_fee") ? jsonNode.get("courier_fee").asDouble() : 0.0;
                taxFee = jsonNode.has("tax_fee") ? jsonNode.get("tax_fee").asDouble() : 0.0;
                rawPrice = jsonNode.has("raw_price") ? jsonNode.get("raw_price").asDouble() :
                        jsonNode.has("total_price") ? jsonNode.get("total_price").asDouble() :
                                foodItemTable.getItems().stream().mapToDouble(item -> item.getPrice() * item.getSelectedQuantity()).sum();
                errorLabel.setText("Order submitted. Please proceed to payment.");
            } else {
                JsonNode rootNode = mapper.readTree(response.body());
                if (rootNode.has("error")) {
                    errorLabel.setText(rootNode.get("error").asText());
                } else {
                    errorLabel.setText("Failed to submit order: HTTP " + response.statusCode());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Error during order submission: " + e.getMessage());
        }
    }

    @FXML
    private void pay(ActionEvent event) {
        errorLabel.setText("");
        if (currentOrderId == null) {
            errorLabel.setText("No order submitted. Please submit an order first.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/snappfrontend/pages/PaymentPage.fxml"));
            Parent paymentRoot = loader.load();
            PaymentController controller = loader.getController();
            controller.setOrderDetails(currentOrderId, currentPayPrice, additionalFee, courierFee, taxFee, rawPrice);
            Scene paymentScene = new Scene(paymentRoot);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(paymentScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Failed to load Payment Page.");
        }
    }
    private void handleGoToRatingPage(ActionEvent event) throws IOException {
        if (selectedFoodId == null) {
            errorLabel.setText("Please select a food item to rate.");
            return;
        }
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/snappfrontend/pages/ViewRatingsPage.fxml"));
        Parent ratingRoot = loader.load();
        ViewRatingsController controller = loader.getController();
        controller.setFoodId(selectedFoodId, restaurantId);
        Scene ratingScene = new Scene(ratingRoot);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(ratingScene);
        stage.show();
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
            errorLabel.setText("Can not load HomePage.");
        }
    }
}