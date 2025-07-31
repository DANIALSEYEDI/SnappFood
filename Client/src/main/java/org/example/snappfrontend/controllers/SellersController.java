package org.example.snappfrontend.controllers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.animation.PauseTransition;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import org.example.snappfrontend.dto.OrderDto;
import org.example.snappfrontend.dto.RestaurantCreateRequestDTO;
import org.example.snappfrontend.dto.RestaurantResponse;
import org.example.snappfrontend.dto.RestaurantUpdateRequest;
import org.example.snappfrontend.models.FoodItem;
import org.example.snappfrontend.models.Order;
import org.example.snappfrontend.utils.ApiClient;
import org.example.snappfrontend.utils.AuthManager;
import org.example.snappfrontend.utils.JsonUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import javax.imageio.ImageIO;
import java.io.*;
import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import org.example.snappfrontend.models.Menu;


public class SellersController {
    //create restaurant
    @FXML public TextField restaurantNameField;
    @FXML public TextField numberField;
    @FXML public TextField addressField;
    @FXML public Button restaurantUploadButton;
    @FXML public ImageView restaurantImageView;
    @FXML public TextField taxFeeField;
    @FXML public TextField additionalFeeField;
    @FXML public Button submitRestaurantButton;
    @FXML public ComboBox selectRestaurantForfinditem;
    @FXML private ListView<String> actionList;
    @FXML private Label errorMessageLabel;
    @FXML private TabPane mainTabPane;

    //view my restaurant
    @FXML private TableView<RestaurantResponse> myRestaurantsTable;
    @FXML private TableColumn<RestaurantResponse, Integer> myRestaurantIdColumn;
    @FXML private TableColumn<RestaurantResponse, String> myRestaurantNameColumn;
    @FXML private TableColumn<RestaurantResponse, String> myRestaurantAddressColumn;
    @FXML private TableColumn<RestaurantResponse, String> myRestaurantPhoneColumn;
    @FXML private TableColumn<RestaurantResponse, Double> myRestaurantTaxFeeColumn;
    @FXML private TableColumn<RestaurantResponse, Double> myRestaurantAdditionalFeeColumn;
    @FXML private Button saveRestaurantChangesButton;


    //food of specific restaurant and menu
    @FXML private TableView<FoodItem> foodItemTable;
    @FXML private TableColumn<FoodItem, Integer> foodItemIdColumn;
    @FXML private TableColumn<FoodItem, String> foodItemNameColumn;
    @FXML private TableColumn<FoodItem, String> foodItemDescription;
    @FXML private TableColumn<FoodItem, Integer> foodItemVendorId;
    @FXML private TableColumn<FoodItem, Integer> foodItemPrice;
    @FXML private TableColumn<FoodItem, Integer> foodItemSupply;
    @FXML private TableColumn<FoodItem, String[]> foodItemKeywords;
    @FXML public ComboBox<String> selectRestaurantForMenu;
    @FXML public ComboBox<String> selectMenuComboBox;
    @FXML private Button deletebutton;

// watch order of restaurant
    @FXML private TableView<OrderDto> restaurantOrdersTable;
    @FXML private TableColumn<OrderDto, Long> sellerOrderIdColumn;
    @FXML private TableColumn<OrderDto, Integer> sellerOrderCustomerColumn;
    @FXML private TableColumn<OrderDto, String> sellerOrderAddressColumn;
    @FXML private TableColumn<OrderDto, String> sellerOrderStatusColumn;
    @FXML private TableColumn<OrderDto, Integer> sellerOrderPriceColumn;
    @FXML private TableColumn<OrderDto, String> sellerOrderCreatedAtColumn;
    @FXML private TableColumn<OrderDto, String> sellerOrderRestaurantStatusColumn;
    @FXML private ComboBox<String> selectRestaurantForOrders;

//make fooditem
    @FXML private TextField nameField;
    @FXML private TextField descriptionField;
    @FXML private TextField priceField;
    @FXML private TextField supplyField;
    @FXML private TextField keywordsField;
    @FXML private Button uploadButton;
    @FXML private ImageView imageView;
    @FXML private Button submitButton;
    private String uploadedImageBase64;
    private String selectedRestaurantForMenuId;
    private String selectedMenuTitle;

    //male menu for restaurant
    @FXML private ComboBox<String> selectRestaurantForaddMenu;
    @FXML private TextField menuNameField;
    @FXML private Button menuSubmitButton;

    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private String imageBase64;
    private ObservableList<RestaurantResponse> sellerRestaurants = FXCollections.observableArrayList();
    private String selectedRestaurantId;


    @FXML
    public void initialize() {
        ObservableList<String> actions = FXCollections.observableArrayList(
                "Create Restaurant",
                "My Restaurants",
                "Manage menu",
                "Restaurant Orders",
                "Add FoodItem",
                "Add Menu");
        actionList.setItems(actions);
        actionList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                handleActionSelection(newValue);
            }
        });
        myRestaurantIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        myRestaurantNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        myRestaurantAddressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        myRestaurantPhoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        myRestaurantTaxFeeColumn.setCellValueFactory(new PropertyValueFactory<>("taxFee"));
        myRestaurantAdditionalFeeColumn.setCellValueFactory(new PropertyValueFactory<>("additionalFee"));

        myRestaurantsTable.setEditable(true);
        myRestaurantNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        myRestaurantNameColumn.setOnEditCommit(event -> {
            RestaurantResponse r = event.getRowValue();
            r.setName(event.getNewValue());
            updateRestaurant(r);
        });
        myRestaurantAddressColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        myRestaurantAddressColumn.setOnEditCommit(event -> {
            RestaurantResponse r = event.getRowValue();
            r.setAddress(event.getNewValue());
            updateRestaurant(r);
        });
        myRestaurantPhoneColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        myRestaurantPhoneColumn.setOnEditCommit(event -> {
            RestaurantResponse r = event.getRowValue();
            r.setPhone(event.getNewValue());
            updateRestaurant(r);
        });
        myRestaurantTaxFeeColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        myRestaurantTaxFeeColumn.setOnEditCommit(event -> {
            RestaurantResponse r = event.getRowValue();
            r.setTaxFee(event.getNewValue());
            updateRestaurant(r);
        });
        myRestaurantAdditionalFeeColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        myRestaurantAdditionalFeeColumn.setOnEditCommit(event -> {
            RestaurantResponse r = event.getRowValue();
            r.setAdditionalFee(event.getNewValue());
            updateRestaurant(r);
        });



        foodItemIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        foodItemNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        foodItemDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        foodItemVendorId.setCellValueFactory(new PropertyValueFactory<>("vendorId"));
        foodItemPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        foodItemSupply.setCellValueFactory(new PropertyValueFactory<>("supply"));
        foodItemKeywords.setCellValueFactory(new PropertyValueFactory<>("keywords"));
        selectRestaurantForMenu.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadMenus();
            }
        });
        selectMenuComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadFoodItems();
            }
        });
        foodItemKeywords.setCellFactory(column -> new TableCell<FoodItem, String[]>() {
            @Override
            protected void updateItem(String[] keywords, boolean empty) {
                super.updateItem(keywords, empty);
                if (empty || keywords == null) {
                    setText("");
                } else {
                    setText(String.join(", ", keywords));
                }
            }
        });


        sellerOrderIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        sellerOrderCustomerColumn.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        sellerOrderAddressColumn.setCellValueFactory(new PropertyValueFactory<>("deliveryAddress"));
        sellerOrderStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        sellerOrderPriceColumn.setCellValueFactory(new PropertyValueFactory<>("payPrice"));
        sellerOrderCreatedAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        sellerOrderRestaurantStatusColumn.setCellValueFactory(new PropertyValueFactory<>("restaurantStatus"));
        restaurantOrdersTable.setEditable(true);
        ObservableList<String> statusOptions = FXCollections.observableArrayList("ACCEPTED", "REJECTED", "SERVED", "PENDING");
        sellerOrderRestaurantStatusColumn.setCellFactory(ComboBoxTableCell.forTableColumn(statusOptions));
        sellerOrderRestaurantStatusColumn.setOnEditCommit(event -> {
            OrderDto order = event.getRowValue();
            String newStatus = event.getNewValue().toLowerCase();
            if (isValidStatus(newStatus)) {
                updateOrderStatus(order.getId(), newStatus);
            } else {
                showAlert("Error", "Invalid status. Use 'accepted', 'rejected', 'served', or 'pending'.", AlertType.ERROR);
                fetchOrdersForSelectedRestaurant();
            }
        });
        restaurantorder();
        viewMyRestaurants();
        initializeAddMenu();
    }

    private void handleActionSelection(String action) {
        switch (action) {
            case "Create Restaurant":
                if (mainTabPane != null) mainTabPane.getSelectionModel().select(0);
                createRestaurant();
                break;
            case "My Restaurants":
                if (mainTabPane != null) mainTabPane.getSelectionModel().select(1);
                viewMyRestaurants();
                break;
            case "Manage menu":
                if (mainTabPane != null) mainTabPane.getSelectionModel().select(2);
                managemenu();
                break;
            case "Restaurant Orders":
                if (mainTabPane != null) mainTabPane.getSelectionModel().select(3);
                restaurantorder();
                break;
            case "Add FoodItem":
                if (mainTabPane != null) mainTabPane.getSelectionModel().select(4);
                addFoodItem();
                break;
            case "Add Menu":
                if (mainTabPane != null) mainTabPane.getSelectionModel().select(5);
                initializeAddMenu();
                break;
            default:
                break;
        }
    }

    //Method of order restaurant
    private void restaurantorder() {
        loadRestaurantsForOrders();
    }
    private void loadRestaurantsForOrders() {
        executorService.submit(() -> {
            try {
                final String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> showAlert("Error", "Invalid token", AlertType.ERROR));
                    return;
                }
                final String apiUrl = "/restaurants/mine";
                Optional<HttpResponse<String>> responseOpt = ApiClient.get(apiUrl, token);
                if (responseOpt.isPresent() && responseOpt.get().statusCode() == 200) {
                    List<RestaurantResponse> list = JsonUtil.getObjectMapper().readValue(
                            responseOpt.get().body(), new TypeReference<List<RestaurantResponse>>() {});
                    List<String> restaurantItems = list.stream()
                            .map(r -> r.getId() + ": " + (r.getName() != null ? r.getName() : "Unnamed"))
                            .toList();

                    Platform.runLater(() -> {
                        sellerRestaurants.setAll(list);
                        selectRestaurantForOrders.setItems(FXCollections.observableArrayList(restaurantItems));
                        if (!restaurantItems.isEmpty()) {
                            String defaultItem = restaurantItems.get(0);
                            selectRestaurantForOrders.setValue(defaultItem);
                            selectedRestaurantId = extractRestaurantId(defaultItem);
                            fetchOrdersForSelectedRestaurant();
                        } else {
                            selectRestaurantForOrders.setValue(null);
                            selectedRestaurantId = null;
                            restaurantOrdersTable.setItems(FXCollections.observableArrayList());
                            showAlert("Warning", "No restaurants found", AlertType.WARNING);
                        }
                        selectRestaurantForOrders.valueProperty().addListener((obs, oldVal, newVal) -> {
                            if (newVal != null && !newVal.isEmpty()) {
                                selectedRestaurantId = extractRestaurantId(newVal);
                                fetchOrdersForSelectedRestaurant();
                            } else {
                                selectedRestaurantId = null;
                                restaurantOrdersTable.setItems(FXCollections.observableArrayList());
                            }
                        });
                    });
                } else {
                    Platform.runLater(() -> showAlert("Error", "Failed to load restaurants: " +
                            (responseOpt.isPresent() ? responseOpt.get().statusCode() : "No response"), AlertType.ERROR));
                }
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error", "Failed to load restaurants: " + e.getMessage(), AlertType.ERROR));
            }
        });
    }

    private void fetchOrdersForSelectedRestaurant() {
        String selectedRestaurant = selectRestaurantForOrders.getValue();
        if (selectedRestaurant == null || selectedRestaurant.isEmpty()) {
            Platform.runLater(() -> {
                restaurantOrdersTable.setItems(FXCollections.observableArrayList());
                showAlert("Warning", "No restaurant selected", AlertType.WARNING);
            });
            return;
        }
        final String restaurantId = extractRestaurantId(selectedRestaurant);
        if (restaurantId == null || restaurantId.isEmpty() || !restaurantId.matches("\\d+")) {
            Platform.runLater(() -> showAlert("Error", "Invalid restaurant ID: " + restaurantId, AlertType.ERROR));
            return;
        }
        final String token = AuthManager.getJwtToken();
        if (token == null || token.isEmpty()) {
            Platform.runLater(() -> showAlert("Error", "Invalid token", AlertType.ERROR));
            return;
        }
        final String finalUrl = "/restaurants/" + restaurantId.trim() + "/orders";
        executorService.submit(() -> {
            try {
                Optional<HttpResponse<String>> responseOpt = ApiClient.get(finalUrl, token);
                if (responseOpt.isEmpty()) {
                    Platform.runLater(() -> showAlert("Error", "No response from server", AlertType.ERROR));
                    return;
                }
                HttpResponse<String> response = responseOpt.get();
                if (response.statusCode() == 200) {
                    List<Order> orders = JsonUtil.getObjectMapper().readValue(
                            response.body(), new TypeReference<List<Order>>() {});
                    List<OrderDto> dtos = orders.stream()
                            .map(o -> new OrderDto(
                                    o.getId(),
                                    o.getCustomerId(),
                                    o.getDeliveryAddress(),
                                    o.getStatus(),
                                    o.getPayPrice(),
                                    o.getCreatedAt(),
                                    o.getRestaurantStatus() != null ? o.getRestaurantStatus() : "PENDING"
                            )).toList();
                    Platform.runLater(() -> restaurantOrdersTable.setItems(FXCollections.observableArrayList(dtos)));
                } else {
                    String message = switch (response.statusCode()) {
                        case 403 -> "Forbidden: Unauthorized access";
                        case 404 -> "Not Found: No orders or restaurant found";
                        case 500 -> "Server Error: Internal server error";
                        default -> "Unexpected error: " + response.statusCode();
                    };
                    Platform.runLater(() -> {
                        restaurantOrdersTable.setItems(FXCollections.observableArrayList());
                        showAlert("Info", message, AlertType.WARNING);
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error", "Failed to fetch orders: " + e.getMessage(), AlertType.ERROR));
            }
        });
    }
    private String extractRestaurantId(String selectedRestaurant) {
        if (selectedRestaurant == null || selectedRestaurant.isEmpty()) {
            return "";
        }
        selectedRestaurant = selectedRestaurant.trim();
        
        String[] parts = selectedRestaurant.split(":");
        if (parts.length > 0) {
            String potentialId = parts[0].trim();
            if (potentialId.matches("\\d+")) {
                return potentialId;
            }
        }
        Pattern pattern = Pattern.compile(".*\\(ID:\\s*(\\d+)\\)");
        Matcher matcher = pattern.matcher(selectedRestaurant);
        if (matcher.find()) {
            String potentialId = matcher.group(1);
            if (potentialId.matches("\\d+")) {
                return potentialId;
            }
        }
        return "";
    }
    private void updateOrderStatus(Long orderId, String newStatus) {
        executorService.submit(() -> {
            try {
                final String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> showAlert("Error", "Invalid token", AlertType.ERROR));
                    return;
                }
                String requestBody = String.format("{\"status\":\"%s\"}", newStatus);
                Optional<HttpResponse<String>> responseOpt = ApiClient.patch(
                        "/restaurants/orders/" + orderId,
                        requestBody,
                        token
                );
                if (responseOpt.isPresent() && responseOpt.get().statusCode() == 200) {
                    Platform.runLater(() -> {
                        OrderDto updatedOrder = restaurantOrdersTable.getItems().stream()
                                .filter(o -> o.getId().equals(orderId))
                                .findFirst()
                                .orElse(null);
                        if (updatedOrder != null) {
                            updatedOrder.setRestaurantStatus(newStatus.toUpperCase());
                            switch (newStatus.toLowerCase()) {
                                case "rejected":
                                    updatedOrder.setStatus("CANCELLED");
                                    break;
                                case "accepted":
                                    updatedOrder.setStatus("WAITING_VENDOR");
                                    break;
                                case "served":
                                    updatedOrder.setStatus("FINDING_COURIER");
                                    break;
                            }
                        }
                        showAlert("Success", "Order status changed successfully", AlertType.INFORMATION);
                        fetchOrdersForSelectedRestaurant();
                    });
                } else {
                    Platform.runLater(() -> showAlert("Error", "Failed to update status: " +
                            (responseOpt.isPresent() ? responseOpt.get().statusCode() : "No response"), AlertType.ERROR));
                }
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error", "Failed to update status: " + e.getMessage(), AlertType.ERROR));
            }
        });
    }

    private boolean isValidStatus(String status) {
        return status != null && List.of("accepted", "rejected", "served").contains(status.toLowerCase());
    }

    private void showAlert(String title, String message, AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    //method of update of restaurant field
    private void updateRestaurant(RestaurantResponse restaurant) {
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> showAlert("Error", "Authentication token missing. Please log in again.", AlertType.ERROR));
                    return;
                }
                String apiUrl = "/restaurants/" + restaurant.getId();
                RestaurantUpdateRequest request = new RestaurantUpdateRequest();
                request.setName(restaurant.getName());
                request.setAddress(restaurant.getAddress());
                request.setPhone(restaurant.getPhone());
                request.setTax_fee(restaurant.getTaxFee());
                request.setAdditional_fee(restaurant.getAdditionalFee());
                request.setLogoBase64(restaurant.getLogoBase64() != null ? restaurant.getLogoBase64() : "");

                String jsonBody = JsonUtil.getObjectMapper().writeValueAsString(request);

                Optional<HttpResponse<String>> responseOpt = ApiClient.put(apiUrl, jsonBody, token);
                if (responseOpt.isPresent()) {
                    int statusCode = responseOpt.get().statusCode();
                    String responseBody = responseOpt.get().body();
                    if (statusCode == 200) {
                        Platform.runLater(() -> {
                            showAlert("Success", "Restaurant ID " + restaurant.getId() + " updated successfully.", AlertType.INFORMATION);
                            RestaurantResponse updated = null;
                            try {
                                updated = JsonUtil.getObjectMapper().readValue(responseBody, RestaurantResponse.class);
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException(e);
                            }
                            restaurant.setName(updated.getName());
                            restaurant.setAddress(updated.getAddress());
                            restaurant.setPhone(updated.getPhone());
                            restaurant.setTaxFee(updated.getTaxFee());
                            restaurant.setAdditionalFee(updated.getAdditionalFee());
                            myRestaurantsTable.refresh();
                        });
                    } else {
                        final String errorMessage = "Failed to update restaurant ID " + restaurant.getId() + ": " + statusCode +
                                (statusCode == 401 ? " (Unauthorized - Please log in again.)" : "");
                        Platform.runLater(() -> showAlert("Error", errorMessage, AlertType.ERROR));
                    }
                } else {
                    Platform.runLater(() -> showAlert("Error", "No response from server for restaurant ID " + restaurant.getId() + ".", AlertType.ERROR));
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert("Error", "Unexpected error updating restaurant ID " + restaurant.getId() + ": " + e.getMessage(), AlertType.ERROR));
            }
        });
    }
    @FXML
    private void saveRestaurantChanges() {
        executorService.submit(() -> {
            try {
                List<RestaurantResponse> allRestaurants = new ArrayList<>(myRestaurantsTable.getItems());
                List<RestaurantResponse> changedRestaurants = new ArrayList<>();
                RestaurantResponse selected = myRestaurantsTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    changedRestaurants.add(selected);
                } else {
                    changedRestaurants.addAll(allRestaurants);
                }
                AtomicInteger successCount = new AtomicInteger(0);
                int total = changedRestaurants.size();
                CountDownLatch latch = new CountDownLatch(total);
                for (RestaurantResponse restaurant : changedRestaurants) {
                    final RestaurantResponse r = restaurant;
                    executorService.submit(() -> {
                        try {
                            updateRestaurant(r);
                            successCount.incrementAndGet();
                        } finally {
                            latch.countDown();
                        }
                    });
                }
                latch.await(5, TimeUnit.SECONDS);
                Platform.runLater(() -> {
                    if (successCount.get() == total) {
                        showAlert("Success", "All restaurant changes saved.", AlertType.INFORMATION);
                        myRestaurantsTable.refresh();
                    } else if (successCount.get() > 0) {
                        showAlert("Partial Success", successCount.get() + " of " + total + " restaurants saved.", AlertType.WARNING);
                        myRestaurantsTable.refresh();
                    } else {
                        showAlert("Error", "No restaurant changes saved.", AlertType.ERROR);
                    }
                });
            } catch (InterruptedException e) {
                Platform.runLater(() -> showAlert("Error", "Save process interrupted: " + e.getMessage(), AlertType.ERROR));
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error", "Failed to save changes: " + e.getMessage(), AlertType.ERROR));
            }
        });
    }
//view my restaurants
    @FXML
    private void viewMyRestaurants() {
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> errorMessageLabel.setText("Authentication token missing. Please log in again."));
                    return;
                }
                Optional<HttpResponse<String>> responseOpt = ApiClient.get("/restaurants/mine", token);
                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    if (response.statusCode() == 200) {
                        List<RestaurantResponse> list = JsonUtil.getObjectMapper()
                                .readerForListOf(RestaurantResponse.class)
                                .readValue(response.body());
                        Platform.runLater(() -> {
                            sellerRestaurants.setAll(list);
                            myRestaurantsTable.setItems(sellerRestaurants);
                            ObservableList<String> restaurantNames = FXCollections.observableArrayList();
                            for (RestaurantResponse r : list) {
                                restaurantNames.add(r.getName() + " (ID: " + r.getId() + ")");
                            }
                            selectRestaurantForOrders.setItems(restaurantNames);
                            selectRestaurantForMenu.setItems(restaurantNames);
                            selectRestaurantForfinditem.setItems(FXCollections.observableArrayList(sellerRestaurants));
                            selectRestaurantForfinditem.setCellFactory(lv -> new ListCell<RestaurantResponse>() {
                                @Override
                                protected void updateItem(RestaurantResponse item, boolean empty) {
                                    super.updateItem(item, empty);
                                    setText(empty || item == null ? null : item.getName() + " (ID: " + item.getId() + ")");
                                }
                            });

                            selectRestaurantForfinditem.setButtonCell(new ListCell<RestaurantResponse>() {
                                @Override
                                protected void updateItem(RestaurantResponse item, boolean empty) {
                                    super.updateItem(item, empty);
                                    setText(empty || item == null ? null : item.getName() + " (ID: " + item.getId() + ")");
                                }
                            });


                            if (!restaurantNames.isEmpty()) {
                                selectRestaurantForOrders.getSelectionModel().selectFirst();
                                selectRestaurantForMenu.getSelectionModel().selectFirst();
                            }
                        });
                    } else {
                        JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());
                        String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                        Platform.runLater(() -> errorMessageLabel.setText("Error viewing your restaurants: " + errorMessage));
                    }
                } else {
                    Platform.runLater(() -> errorMessageLabel.setText("Failed to connect to server for restaurants."));
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    errorMessageLabel.setText("An unexpected error occurred while fetching your restaurants: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }

// method of watch food in menu

@FXML
private void managemenu() {
    if (mainTabPane != null) {
        mainTabPane.getSelectionModel().select(2);
    }
    String selectedRestaurant = selectRestaurantForMenu.getValue();
    if (selectedRestaurant != null) {
        loadMenus();
    } else {
        Platform.runLater(() -> showAlert("Warning", "Please select a restaurant first", AlertType.WARNING));
    }
}

    private void loadMenus() {
        executorService.submit(() -> {
            try {
                String selectedRestaurant = selectRestaurantForMenu.getValue();
                if (selectedRestaurant == null) {
                    Platform.runLater(() -> showAlert("Error", "Please select a restaurant", AlertType.ERROR));
                    return;
                }
                String idPart = selectedRestaurant.substring(selectedRestaurant.indexOf("ID: ") + 4);
                Long restaurantId = Long.parseLong(idPart.replace(")", "").trim());
                String apiUrl = "/restaurants/" + restaurantId + "/menus/list";

                Optional<HttpResponse<String>> responseOpt = ApiClient.get(apiUrl, AuthManager.getJwtToken());
                if (responseOpt.isPresent()) {
                    int statusCode = responseOpt.get().statusCode();
                    if (statusCode == 200) {
                        List<Menu> menus = JsonUtil.getObjectMapper().readValue(
                                responseOpt.get().body(), new TypeReference<List<Menu>>() {});
                        List<String> menuItems = menus.stream()
                                .map(m -> m.getId() + ": " + m.getTitle())
                                .toList();
                        Platform.runLater(() -> {
                            selectMenuComboBox.setItems(FXCollections.observableArrayList(menuItems));
                            if (!menuItems.isEmpty()) {
                                selectMenuComboBox.setValue(menuItems.get(0));
                                loadFoodItems();
                            } else {
                                selectMenuComboBox.setItems(null);
                                showAlert("Warning", "No menus found for this restaurant", AlertType.WARNING);
                            }
                        });
                    } else {
                        Platform.runLater(() -> showAlert("Error", "Failed to load menus: " + statusCode, AlertType.ERROR));
                    }
                } else {
                    Platform.runLater(() -> showAlert("Error", "No response from server for menus", AlertType.ERROR));
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert("Error", "Invalid restaurant ID format: " + e.getMessage(), AlertType.ERROR));
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert("Error", "Error loading menus: " + e.getMessage(), AlertType.ERROR));
            }
        });
    }
    private void loadFoodItems() {
        executorService.submit(() -> {
            try {
                String selectedRestaurant = selectRestaurantForMenu.getValue();
                String selectedMenu = selectMenuComboBox.getValue();
                if (selectedRestaurant == null || selectedMenu == null) {
                    Platform.runLater(() -> showAlert("Error", "Please select a restaurant and menu", AlertType.ERROR));
                    return;
                }
                String idPart = selectedRestaurant.substring(selectedRestaurant.indexOf("ID: ") + 4);
                Long restaurantId = Long.parseLong(idPart.replace(")", "").trim());
                String menuTitle = selectedMenu.split(":")[1].trim();
                String encodedMenuTitle = URLEncoder.encode(menuTitle, StandardCharsets.UTF_8);
                String apiUrl = "/restaurants/" + restaurantId + "/menu/" + encodedMenuTitle + "/items";
                Optional<HttpResponse<String>> responseOpt = ApiClient.get(apiUrl, AuthManager.getJwtToken());
                if (responseOpt.isPresent()) {
                    int statusCode = responseOpt.get().statusCode();
                    System.out.println("Status Code for food items: " + statusCode);
                    if (statusCode == 200) {
                        List<FoodItem> foodItems = JsonUtil.getObjectMapper().readValue(
                                responseOpt.get().body(), new TypeReference<List<FoodItem>>() {});
                        foodItems.forEach(item -> System.out.println("FoodItem - ID: " + item.getId() + ", VendorId: " + item.getVendorId()));
                        ObservableList<FoodItem> itemsList = FXCollections.observableArrayList(foodItems);
                        Platform.runLater(() -> {
                            foodItemKeywords.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getKeywords()));
                            foodItemTable.setEditable(true);
                            foodItemNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
                            foodItemNameColumn.setOnEditCommit(event -> {
                                event.getRowValue().setName(event.getNewValue());
                                handleEditItem(event.getRowValue());
                            });
                            foodItemDescription.setCellFactory(TextFieldTableCell.forTableColumn());
                            foodItemDescription.setOnEditCommit(event -> {
                                event.getRowValue().setDescription(event.getNewValue());
                                handleEditItem(event.getRowValue());
                            });
                            foodItemPrice.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
                            foodItemPrice.setOnEditCommit(event -> {
                                event.getRowValue().setPrice(event.getNewValue());
                                handleEditItem(event.getRowValue());
                            });
                            foodItemSupply.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
                            foodItemSupply.setOnEditCommit(event -> {
                                event.getRowValue().setSupply(event.getNewValue());
                                handleEditItem(event.getRowValue());
                            });
                            foodItemKeywords.setCellFactory(column -> new TableCell<FoodItem, String[]>() {
                                private TextField textField = new TextField();
                                {
                                    textField.setOnAction(e -> commitEdit(textField.getText().split(",\\s*")));
                                }
                                @Override
                                protected void updateItem(String[] keywords, boolean empty) {
                                    super.updateItem(keywords, empty);
                                    if (empty || keywords == null) {
                                        setText("");
                                        setGraphic(null);
                                    } else {
                                        setText(String.join(", ", keywords));
                                        setGraphic(null);
                                    }
                                }
                                @Override
                                public void startEdit() {
                                    super.startEdit();
                                    textField.setText(String.join(", ", getItem() != null ? getItem() : new String[0]));
                                    setGraphic(textField);
                                    setText(null);
                                }
                                @Override
                                public void cancelEdit() {
                                    super.cancelEdit();
                                    setText(String.join(", ", getItem() != null ? getItem() : new String[0]));
                                    setGraphic(null);
                                }
                            });
                            foodItemKeywords.setOnEditCommit(event -> {
                                event.getRowValue().setKeywords(event.getNewValue());
                                handleEditItem(event.getRowValue());
                            });
                            foodItemTable.setItems(itemsList);
                        });
                    } else {
                        Platform.runLater(() -> showAlert("Error", "Failed to load food items: " + statusCode, AlertType.ERROR));
                    }
                } else {
                    Platform.runLater(() -> showAlert("Error", "No response from server for food items", AlertType.ERROR));
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert("Error", "Error loading food items: " + e.getMessage(), AlertType.ERROR));
            }
        });
    }
    @FXML
    private void handleDeleteItem() {
        executorService.submit(() -> {
            try {
                FoodItem selectedItem = foodItemTable.getSelectionModel().getSelectedItem();
                if (selectedItem == null) {
                    Platform.runLater(() -> showAlert("Error", "Please select a food item to delete", AlertType.ERROR));
                    return;
                }
                String selectedRestaurant = selectRestaurantForMenu.getValue();
                String selectedMenu = selectMenuComboBox.getValue();
                if (selectedRestaurant == null || selectedMenu == null) {
                    Platform.runLater(() -> showAlert("Error", "Please select a restaurant and menu", AlertType.ERROR));
                    return;
                }
                String idPart = selectedRestaurant.substring(selectedRestaurant.indexOf("ID: ") + 4);
                Long restaurantId = Long.parseLong(idPart.replace(")", "").trim());
                String menuTitle = selectedMenu.split(":")[1].trim();
                String encodedMenuTitle = URLEncoder.encode(menuTitle, StandardCharsets.UTF_8);
                Long itemId = selectedItem.getId().longValue();
                String apiUrl = "/restaurants/" + restaurantId + "/menu/" + encodedMenuTitle + "/" + itemId;
                Optional<HttpResponse<String>> responseOpt = ApiClient.delete(apiUrl, AuthManager.getJwtToken());
                if (responseOpt.isPresent()) {
                    int statusCode = responseOpt.get().statusCode();
                    if (statusCode == 200) {
                        Platform.runLater(() -> {
                            showAlert("Success", "Item removed from restaurant menu successfully", AlertType.INFORMATION);
                            loadFoodItems();
                        });
                    } else {
                        Platform.runLater(() -> showAlert("Error", "Failed to delete item: " + statusCode, AlertType.ERROR));
                    }
                } else {
                    Platform.runLater(() -> showAlert("Error", "No response from server for delete", AlertType.ERROR));
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert("Error", "Error deleting item: " + e.getMessage(), AlertType.ERROR));
            }
        });
    }
    private void handleEditItem(FoodItem item) {
        executorService.submit(() -> {
            try {
                String selectedRestaurant = selectRestaurantForMenu.getValue();
                if (selectedRestaurant == null) {
                    Platform.runLater(() -> showAlert("Error", "Please select a restaurant", AlertType.ERROR));
                    return;
                }
                String idPart = selectedRestaurant.substring(selectedRestaurant.indexOf("ID: ") + 4);
                Long restaurantId = Long.parseLong(idPart.replace(")", "").trim());
                Long itemId = item.getId() != null ? item.getId().longValue() : null;

                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("name", item.getName() != null ? item.getName() : "");
                requestBody.put("imageBase64", item.getImageBase64() != null ? item.getImageBase64() : "");
                requestBody.put("description", item.getDescription() != null ? item.getDescription() : "");
                requestBody.put("price", item.getPrice() != null ? item.getPrice() : 0);
                requestBody.put("supply", item.getSupply() != null ? item.getSupply() : 0);
                requestBody.put("keywords", item.getKeywords() != null && item.getKeywords().length > 0 ? Arrays.asList(item.getKeywords()) : new ArrayList<>());

                String jsonBody = JsonUtil.getObjectMapper().writeValueAsString(requestBody);
                String apiUrl = "/restaurants/" + restaurantId + "/item/" + itemId;
                Optional<HttpResponse<String>> responseOpt = ApiClient.put(apiUrl, jsonBody, AuthManager.getJwtToken());
                if (responseOpt.isPresent()) {
                    int statusCode = responseOpt.get().statusCode();
                    if (statusCode == 200) {
                        ObjectMapper mapper = JsonUtil.getObjectMapper();
                        JsonNode jsonNode = mapper.readTree(responseOpt.get().body());
                        FoodItem updatedItem = mapper.treeToValue(jsonNode, FoodItem.class);
                        if (updatedItem.getKeywords() == null) {
                            updatedItem.setKeywords(new String[0]);
                        }
                        Platform.runLater(() -> {
                            showAlert("Success", "Food item edited successfully", AlertType.INFORMATION);
                            int index = foodItemTable.getItems().indexOf(item);
                            if (index >= 0) {
                                foodItemTable.getItems().set(index, updatedItem);
                            } else {
                                loadFoodItems();
                            }
                        });
                    } else {
                        Platform.runLater(() -> showAlert("Error", "Failed to edit item: " + statusCode, AlertType.ERROR));
                    }
                } else {
                    Platform.runLater(() -> showAlert("Error", "No response from server for edit", AlertType.ERROR));
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert("Error", "Error editing item: " + e.getMessage(), AlertType.ERROR));
            }
        });
    }
    //Method of create restaurant

    @FXML
    private void createRestaurant() {
        String name = restaurantNameField.getText();
        String address = addressField.getText();
        String phone = numberField.getText();
        String taxFeeStr = taxFeeField.getText();
        String additionalFeeStr = additionalFeeField.getText();
        if (name.isEmpty() || address.isEmpty() || phone.isEmpty()) {
            errorMessageLabel.setText("Please fill required fields (name, address, phone)");
            return;
        }
        try {
            RestaurantCreateRequestDTO dto = new RestaurantCreateRequestDTO();
            dto.name = name;
            dto.address = address;
            dto.phone = phone;

            if (!taxFeeStr.isEmpty()) {
                dto.tax_fee = Double.parseDouble(taxFeeStr);
            }

            if (!additionalFeeStr.isEmpty()) {
                dto.additionalFee = Double.parseDouble(additionalFeeStr);
            }
            dto.logoBase64 = imageBase64;
            String token = AuthManager.getJwtToken();
            if (token == null || token.isEmpty()) {
                errorMessageLabel.setText("Please login again");
                return;
            }
            String jsonBody = JsonUtil.getObjectMapper().writeValueAsString(dto);
            Optional<HttpResponse<String>> responseOpt = ApiClient.post("/restaurants", jsonBody, token);
            if (responseOpt.isPresent()) {
                HttpResponse<String> response = responseOpt.get();
                errorMessageLabel.setText("Status: " + response.statusCode() + "\nBody: " + response.body());
                if (response.statusCode() == 201) {
                    errorMessageLabel.setText("Restaurant created");
                    clearRestaurantForm();
                    PauseTransition pause = new PauseTransition(Duration.seconds(3));
                    pause.setOnFinished(e -> errorMessageLabel.setText(""));
                    pause.play();
                }
            } else {
                errorMessageLabel.setText("No response from server");
            }
        } catch (NumberFormatException e) {
            errorMessageLabel.setText("Please enter valid numbers for tax and additional fee");
        } catch (Exception e) {
            e.printStackTrace();
            errorMessageLabel.setText("Error: " + e.getMessage());
        }
    }


    @FXML
    void handleUploadImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select an Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(((Stage) restaurantUploadButton.getScene().getWindow()));
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

                imageBase64 = encodeImage(resized);
                restaurantImageView.setImage(SwingFXUtils.toFXImage(resized, null));
                errorMessageLabel.setText("");
            } catch (IOException e) {
                errorMessageLabel.setText("Image upload failed");
            }
        }
    }
    private String encodeImage(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }
    private void clearRestaurantForm() {
        restaurantNameField.clear();
        addressField.clear();
        numberField.clear();
        taxFeeField.clear();
        additionalFeeField.clear();
        imageBase64 = null;
        restaurantImageView.setImage(null);
        taxFeeField.clear();
        additionalFeeField.clear();
        errorMessageLabel.setText("");
    }

//add fooditem ............................................................
public TabPane getMainTabPane() {
    return mainTabPane;
}
@FXML
private void addFoodItem() {
    RestaurantResponse selectedRestaurant = (RestaurantResponse) selectRestaurantForfinditem.getSelectionModel().getSelectedItem();
    if (selectedRestaurant == null) {
        errorMessageLabel.setText("Please select a restaurant.");
        return;
    }
    if (priceField.getText().isEmpty() || supplyField.getText().isEmpty() || nameField.getText().isEmpty() ||
            descriptionField.getText().isEmpty() || keywordsField.getText().isEmpty()) {
        errorMessageLabel.setText("Fields cannot be empty");
        return;
    }
    long restaurantId = selectedRestaurant.getId();
    String name = nameField.getText();
    String description = descriptionField.getText();
    String imageBase64 = uploadedImageBase64;
    Integer price = Integer.parseInt(priceField.getText());
    Integer supply = Integer.parseInt(supplyField.getText());
    List<String> keywords = Arrays.asList(keywordsField.getText().split("\\s*,\\s*"));
    ObjectNode json = JsonUtil.getObjectMapper().createObjectNode();
    json.put("name", name);
    json.put("description", description);
    json.put("imageBase64", imageBase64);
    json.put("price", price);
    json.put("supply", supply);
    ArrayNode keywordsNode = json.putArray("keywords");
    for (String k : keywords) {
        keywordsNode.add(k);
    }
    executorService.submit(() -> {
        try {
            String token = AuthManager.getJwtToken();
            Optional<HttpResponse<String>> responseOpt = ApiClient.post("/restaurants/" + restaurantId + "/item", json.toString(), token);
            if (responseOpt.isPresent()) {
                int statusCode = responseOpt.get().statusCode();
                String responseBody = responseOpt.get().body();
                Platform.runLater(() -> {
                    if (statusCode == 200) {
                        JsonNode jsonNode = null;
                        try {
                            jsonNode = JsonUtil.getObjectMapper().readTree(responseBody);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                        long foodId = jsonNode.get("id").asLong();
                        errorMessageLabel.setText("Food item added successfully! Redirecting...");
                        loadChooseMenuPage(restaurantId, foodId);
                    } else {
                        errorMessageLabel.setText("Failed to add food item: " + responseBody);
                    }
                });
            } else {
                Platform.runLater(() -> errorMessageLabel.setText("No response from server."));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> errorMessageLabel.setText("Error adding food item."));
        }
    });
}
    private void loadChooseMenuPage(long restaurantId, long foodId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/snappfrontend/pages/ChooseMenuPage.fxml"));
            Parent root = loader.load();
            ChooseMenuController controller = loader.getController();
            controller.setData(restaurantId, foodId, executorService);
            Stage stage = (Stage) errorMessageLabel.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load choose menu page: " + e.getMessage(), AlertType.ERROR);
        }
    }
// create menu.........................................................................................

    private void initializeAddMenu() {
        loadRestaurantsForAddMenu();
        menuSubmitButton.setOnAction(event -> createMenu());
    }
    private void loadRestaurantsForAddMenu() {
        executorService.submit(() -> {
            try {
                final String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> showAlert("Error", "Invalid token", AlertType.ERROR));
                    return;
                }
                final String apiUrl = "/restaurants/mine";
                Optional<HttpResponse<String>> responseOpt = ApiClient.get(apiUrl, token);
                if (responseOpt.isPresent() && responseOpt.get().statusCode() == 200) {
                    List<RestaurantResponse> list = JsonUtil.getObjectMapper().readValue(
                            responseOpt.get().body(), new TypeReference<List<RestaurantResponse>>() {});
                    List<String> restaurantItems = list.stream()
                            .map(r -> r.getId() + ": " + (r.getName() != null ? r.getName() : "Unnamed"))
                            .toList();

                    Platform.runLater(() -> {
                        selectRestaurantForaddMenu.setItems(FXCollections.observableArrayList(restaurantItems));
                        if (!restaurantItems.isEmpty()) {
                            selectRestaurantForaddMenu.setValue(restaurantItems.get(0));
                        } else {
                            selectRestaurantForaddMenu.setValue(null);
                            showAlert("Warning", "No restaurants found", AlertType.WARNING);
                        }
                    });
                } else {
                    Platform.runLater(() -> showAlert("Error", "Failed to load restaurants: " +
                            (responseOpt.isPresent() ? responseOpt.get().statusCode() : "No response"), AlertType.ERROR));
                }
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error", "Failed to load restaurants: " + e.getMessage(), AlertType.ERROR));
            }
        });
    }

    private void createMenu() {
        String selectedRestaurant = selectRestaurantForaddMenu.getValue();
        String menuTitle = menuNameField.getText().trim();
        if (selectedRestaurant == null || selectedRestaurant.isEmpty()) {
            showAlert("Error", "Please select a restaurant", AlertType.ERROR);
            return;
        }
        if (menuTitle == null || menuTitle.isEmpty()) {
            showAlert("Error", "Please enter a menu title", AlertType.ERROR);
            return;
        }

        final String restaurantId = extractRestaurantId(selectedRestaurant);
        if (restaurantId == null || restaurantId.isEmpty() || !restaurantId.matches("\\d+")) {
            showAlert("Error", "Invalid restaurant ID: " + restaurantId, AlertType.ERROR);
            return;
        }

        executorService.submit(() -> {
            try {
                final String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> showAlert("Error", "Invalid token", AlertType.ERROR));
                    return;
                }

                String requestBody = String.format("{\"title\":\"%s\"}", menuTitle);
                Optional<HttpResponse<String>> responseOpt = ApiClient.post(
                        "/restaurants/" + restaurantId + "/menu",
                        requestBody,
                        token
                );
                if (responseOpt.isPresent()) {
                    int statusCode = responseOpt.get().statusCode();
                    String responseBody = responseOpt.get().body();
                    Platform.runLater(() -> {
                        switch (statusCode) {
                            case 200:
                                showAlert("Success", "Menu created successfully: " + responseBody, AlertType.INFORMATION);
                                menuNameField.clear();
                                break;
                            case 400:
                                showAlert("Error", "Invalid input: " + responseBody, AlertType.ERROR);
                                break;
                            case 401:
                                showAlert("Error", "Unauthorized: " + responseBody, AlertType.ERROR);
                                break;
                            case 403:
                                showAlert("Error", "Forbidden: " + responseBody, AlertType.ERROR);
                                break;
                            case 404:
                                showAlert("Error", "Restaurant not found: " + responseBody, AlertType.ERROR);
                                break;
                            case 409:
                                showAlert("Error", "Conflict: Menu title already exists: " + responseBody, AlertType.ERROR);
                                break;
                            case 500:
                                showAlert("Error", "Internal server error: " + responseBody, AlertType.ERROR);
                                break;
                            default:
                                showAlert("Error", "Unexpected error: " + statusCode, AlertType.ERROR);
                        }
                    });
                } else {
                    Platform.runLater(() -> showAlert("Error", "No response from server", AlertType.ERROR));
                }
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error", "Failed to create menu: " + e.getMessage(), AlertType.ERROR));
            }
        });
    }
    // back to home

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
        }
    }
}