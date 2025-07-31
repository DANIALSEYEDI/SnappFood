package org.example.snappfrontend.controllers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.animation.PauseTransition;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.util.Duration;
import org.example.snappfrontend.dto.AdminUserResponse;
import org.example.snappfrontend.models.Coupon;
import org.example.snappfrontend.models.Order;
import org.example.snappfrontend.models.Transaction;
import org.example.snappfrontend.utils.JsonUtil;
import org.example.snappfrontend.utils.AuthManager;
import org.example.snappfrontend.utils.ApiClient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminController {
    @FXML private Label errorMessageLabel;
    @FXML private ListView<String> actionList;

    // FXML elements for Users Tab
    @FXML private TableView<AdminUserResponse> usersTable;
    @FXML private TableColumn<AdminUserResponse, String> userIdColumn;
    @FXML private TableColumn<AdminUserResponse, String> userNameColumn;
    @FXML private TableColumn<AdminUserResponse, String> userPhoneColumn;
    @FXML private TableColumn<AdminUserResponse, String> userEmailColumn;
    @FXML private TableColumn<AdminUserResponse, String> userRoleColumn;
    @FXML private TableColumn<AdminUserResponse, String> userAddressColumn;
    @FXML private TableColumn<AdminUserResponse, String> userStatusColumn;
    @FXML private TableColumn<AdminUserResponse, BigDecimal> userBanknameColumn;
    @FXML private ComboBox<String> filterRoleComboBox;

    // The main TabPane to organize sections
    @FXML private TabPane mainTabPane;

    // FXML elements for Orders Tab
    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, Integer> orderIdColumn;
    @FXML private TableColumn<Order, Integer> orderCustomerIdColumn;
    @FXML private TableColumn<Order, Integer> orderVendorIdColumn;
    @FXML private TableColumn<Order, String> orderStatusColumn;
    @FXML private TableColumn<Order, Integer> orderPriceColumn;
    @FXML private TableColumn<Order, String> orderAddressColumn;
    @FXML private TableColumn<Order, String> orderCreatedAtColumn;

    // FXML elements for Transactions Tab
    @FXML private TableView<Transaction> transactionsTable;
    @FXML private TableColumn<Transaction, Integer> transactionIdColumn;
    @FXML private TableColumn<Transaction, Integer> transactionOrderIdColumn;
    @FXML private TableColumn<Transaction, Integer> transactionUserIdColumn;
    @FXML private TableColumn<Transaction, String> transactionMethodColumn;
    @FXML private TableColumn<Transaction, String> transactionStatusColumn;


    // FXML elements for Coupons Tab
    @FXML private TableView<Coupon> couponsTable;
    @FXML private TableColumn<Coupon, Integer> couponIdColumn;
    @FXML private TableColumn<Coupon, String> couponCodeColumn;
    @FXML private TableColumn<Coupon, String> couponTypeColumn;
    @FXML private TableColumn<Coupon, Double> couponValueColumn;
    @FXML private TableColumn<Coupon, Integer> couponMinPriceColumn;
    @FXML private TableColumn<Coupon, Integer> couponUserCountColumn;
    @FXML private TableColumn<Coupon, String> couponStartDateColumn;
    @FXML private TableColumn<Coupon, String> couponEndDateColumn;

    @FXML private TextField codeField;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private TextField valueField;
    @FXML private TextField minPriceField;
    @FXML private TextField userCountField;
    @FXML private DatePicker startDateField;
    @FXML private DatePicker endDateField;
    @FXML private Button addButton;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @FXML
    public void initialize() {
        ObservableList<String> actions = FXCollections.observableArrayList(
                "View Users",
                "View Orders",
                "View Transactions",
                "View Coupons",
                "Add Coupon"
        );
        actionList.setItems(actions);

        actionList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                handleActionSelection(newValue);
            }
        });

        // Initialize User Table Columns
        if (userIdColumn != null) userIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (userNameColumn != null) userNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        if (userPhoneColumn != null) userPhoneColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        if (userEmailColumn != null) userEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        if (userRoleColumn != null) userRoleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        if (userAddressColumn != null) userAddressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        if (userStatusColumn != null) userStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        if (userBanknameColumn != null) userBanknameColumn.setCellValueFactory(new PropertyValueFactory<>("bank_name"));

        usersTable.setEditable(true);
        userStatusColumn.setEditable(true);
        ObservableList<String> statusOptions = FXCollections.observableArrayList("approved", "rejected");
        userStatusColumn.setCellFactory(ComboBoxTableCell.forTableColumn(statusOptions));
        userStatusColumn.setOnEditCommit(event -> {
            AdminUserResponse user = event.getRowValue();
            String newStatus = event.getNewValue();
            user.setStatus(newStatus);
            updateUserStatus(user.getId().toString(), newStatus);
        });

        ObservableList<String> roles = FXCollections.observableArrayList("All Roles", "Buyer", "Seller", "Courier", "Admin");
        if (filterRoleComboBox != null) {
            filterRoleComboBox.setItems(roles);
            filterRoleComboBox.getSelectionModel().selectFirst();
            filterRoleComboBox.valueProperty().addListener((obs, oldVal, newVal) -> viewUsers());
        }

        // Initialize Order Table Columns
        if (orderIdColumn != null) orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (orderCustomerIdColumn != null) orderCustomerIdColumn.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        if (orderVendorIdColumn != null) orderVendorIdColumn.setCellValueFactory(new PropertyValueFactory<>("vendorId"));
        if (orderStatusColumn != null) orderStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        if (orderPriceColumn != null) orderPriceColumn.setCellValueFactory(new PropertyValueFactory<>("payPrice"));
        if (orderAddressColumn != null) orderAddressColumn.setCellValueFactory(new PropertyValueFactory<>("deliveryAddress"));
        if (orderCreatedAtColumn != null) orderCreatedAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));


        // Initialize Transaction Table Columns
        if (transactionIdColumn != null) transactionIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (transactionOrderIdColumn != null) transactionOrderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        if (transactionUserIdColumn != null) transactionUserIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        if (transactionMethodColumn != null) transactionMethodColumn.setCellValueFactory(new PropertyValueFactory<>("method"));
        if (transactionStatusColumn != null) transactionStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Initialize Coupon Table Columns
        if (couponIdColumn != null) couponIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (couponCodeColumn != null) couponCodeColumn.setCellValueFactory(new PropertyValueFactory<>("couponCode"));
        if (couponTypeColumn != null) couponTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        if (couponValueColumn != null) couponValueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        if (couponMinPriceColumn != null) couponMinPriceColumn.setCellValueFactory(new PropertyValueFactory<>("minPrice"));
        if (couponUserCountColumn != null) couponUserCountColumn.setCellValueFactory(new PropertyValueFactory<>("userCount"));
        if (couponStartDateColumn != null) couponStartDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        if (couponEndDateColumn != null) couponEndDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));


        typeComboBox.setItems(FXCollections.observableArrayList("fixed", "percent"));
        viewUsers();
        viewOrders();
        viewTransactions();
        viewCoupons();
    }

    private void handleActionSelection(String action) {
        switch (action) {
            case "View Users":
                if (mainTabPane != null) mainTabPane.getSelectionModel().select(0);
                viewUsers();
                break;
            case "View Orders":
                if (mainTabPane != null) mainTabPane.getSelectionModel().select(1);
                viewOrders();
                break;
            case "View Transactions":
                if (mainTabPane != null) mainTabPane.getSelectionModel().select(2);
                viewTransactions();
                break;
            case "View Coupons":
                if (mainTabPane != null) mainTabPane.getSelectionModel().select(3);
                viewCoupons();
                break;
            case "Add Coupon":
                if (mainTabPane != null) mainTabPane.getSelectionModel().select(4);
                addCoupon();
                break;
            default:
                break;
        }
    }


    @FXML
    private void viewUsers() {
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> errorMessageLabel.setText("Authentication token missing. Please log in again."));
                    return;
                }

                String selectedRole;
                if (filterRoleComboBox != null && filterRoleComboBox.getSelectionModel().getSelectedItem() != null &&
                        !filterRoleComboBox.getSelectionModel().getSelectedItem().equals("All Roles")) {
                    selectedRole = filterRoleComboBox.getSelectionModel().getSelectedItem();
                } else {
                    selectedRole = null;
                }
                String path = "/admin/users";
                Optional<HttpResponse<String>> responseOpt = ApiClient.get(path, token);

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            try {
                                List<AdminUserResponse> users = JsonUtil.getObjectMapper()
                                        .readerForListOf(AdminUserResponse.class)
                                        .readValue(rootNode);
                                ObservableList<AdminUserResponse> userObservableList = FXCollections.observableArrayList(users);
                                usersTable.setItems(userObservableList);

                                if (selectedRole != null) {
                                    userObservableList = userObservableList.filtered(user ->
                                            user.getRole() != null && user.getRole().equalsIgnoreCase(selectedRole)
                                    );
                                }

                                usersTable.setItems(userObservableList);
                            } catch (IOException e) {
                                errorMessageLabel.setText("Error parsing user data: " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                            errorMessageLabel.setText("Error viewing users: " + errorMessage);
                        }
                    });
                } else {
                    Platform.runLater(() -> errorMessageLabel.setText("Failed to connect to server for users. Please check server status."));
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    errorMessageLabel.setText("An unexpected error occurred while fetching users: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }


    private void updateUserStatus(String userId, String status) {

        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> errorMessageLabel.setText("Authentication token missing. Please log in again."));
                    return;
                }

                Map<String, String> statusData = new HashMap<>();
                statusData.put("status", status);
                String jsonBody = JsonUtil.getObjectMapper().writeValueAsString(statusData);

                Optional<HttpResponse<String>> responseOpt = ApiClient.patch("/admin/users/" + userId + "/status", jsonBody, token);

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            errorMessageLabel.setText("User status updated successfully.");
                            PauseTransition pause = new PauseTransition(Duration.seconds(3));
                            pause.setOnFinished(e -> errorMessageLabel.setText(""));
                            pause.play();
                            viewUsers();
                        } else {
                            String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                            errorMessageLabel.setText("Error updating user status: " + errorMessage);
                        }
                    });
                } else {
                    Platform.runLater(() -> errorMessageLabel.setText("Failed to connect to server for status update."));
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    errorMessageLabel.setText("An unexpected error occurred during status update: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }



    @FXML
    private void viewOrders() {
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> errorMessageLabel.setText("Authentication token missing. Please log in again."));
                    return;
                }
                Map<String, String> queryParams = new HashMap<>();
                String path = "/admin/orders";
                if (!queryParams.isEmpty()) {
                    StringBuilder queryString = new StringBuilder("?");
                    queryParams.forEach((key, value) -> queryString.append(key).append("=").append(value).append("&"));
                    queryString.setLength(queryString.length() - 1);
                    path += queryString.toString();
                }
                Optional<HttpResponse<String>> responseOpt = ApiClient.get(path, token);
                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            try {
                                List<Order> orders = JsonUtil.getObjectMapper().readerForListOf(Order.class).readValue(rootNode);
                                ObservableList<Order> orderObservableList = FXCollections.observableArrayList(orders);
                                ordersTable.setItems(orderObservableList);
                            } catch (IOException e) {
                                errorMessageLabel.setText("Error parsing orders data: " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                            errorMessageLabel.setText("Error viewing orders: " + errorMessage);
                        }
                    });
                } else {
                    Platform.runLater(() -> errorMessageLabel.setText("Failed to connect to server for orders. Please check server status."));
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    errorMessageLabel.setText("An unexpected error occurred while fetching orders: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }




    @FXML
    private void viewTransactions() {
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> errorMessageLabel.setText("Authentication token missing. Please log in again."));
                    return;
                }
                Map<String, String> queryParams = new HashMap<>();
                String path = "/admin/transactions";
                if (!queryParams.isEmpty()) {
                    StringBuilder queryString = new StringBuilder("?");
                    queryParams.forEach((key, value) -> queryString.append(key).append("=").append(value).append("&"));
                    queryString.setLength(queryString.length() - 1);
                    path += queryString.toString();
                }

                Optional<HttpResponse<String>> responseOpt = ApiClient.get(path, token);

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            try {
                                List<Transaction> transactions = JsonUtil.getObjectMapper().readerForListOf(Transaction.class).readValue(rootNode);
                                ObservableList<Transaction> transactionObservableList = FXCollections.observableArrayList(transactions);
                                transactionsTable.setItems(transactionObservableList);
                            } catch (IOException e) {
                                errorMessageLabel.setText("Error parsing transactions data: " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                            errorMessageLabel.setText("Error viewing transactions: " + errorMessage);
                        }
                    });
                } else {
                    Platform.runLater(() -> errorMessageLabel.setText("Failed to connect to server for transactions. Please check server status."));
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    errorMessageLabel.setText("An unexpected error occurred while fetching transactions: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }

    
    @FXML
    private void viewCoupons() {
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> errorMessageLabel.setText("Authentication token missing. Please log in again."));
                    return;
                }
                String path = "/admin/coupons";
                Optional<HttpResponse<String>> responseOpt = ApiClient.get(path, token);

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            try {
                                List<Coupon> coupons = JsonUtil.getObjectMapper().readerForListOf(Coupon.class).readValue(rootNode);
                                ObservableList<Coupon> couponObservableList = FXCollections.observableArrayList(coupons);
                                couponsTable.setItems(couponObservableList);
                            } catch (IOException e) {
                                errorMessageLabel.setText("Error parsing coupons data: " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                            errorMessageLabel.setText("Error viewing coupons: " + errorMessage);
                        }
                    });
                } else {
                    Platform.runLater(() -> errorMessageLabel.setText("Failed to connect to server for coupons. Please check server status."));
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    errorMessageLabel.setText("An unexpected error occurred while fetching coupons: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }


    @FXML
    private void addCoupon() {
        String code = codeField.getText();
        String type = typeComboBox.getValue();
        String valueStr = valueField.getText();
        String minPriceStr = minPriceField.getText();
        String userCountStr = userCountField.getText();
        LocalDate startDate = startDateField.getValue();
        LocalDate endDate = endDateField.getValue();

        if (code == null || type == null || valueStr.isEmpty() || minPriceStr.isEmpty() ||
                userCountStr.isEmpty() || startDate == null || endDate == null) {
            errorMessageLabel.setText("please fill all the required fields.");
            return;
        }

        try {
            double value = Double.parseDouble(valueStr);
            int minPrice = Integer.parseInt(minPriceStr);
            int userCount = Integer.parseInt(userCountStr);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("coupon_code", code);
            requestBody.put("type", type);
            requestBody.put("value", value);
            requestBody.put("min_price", minPrice);
            requestBody.put("user_count", userCount);
            requestBody.put("start_date", startDate.toString());
            requestBody.put("end_date", endDate.toString());

            String token = AuthManager.getJwtToken();
            String json = JsonUtil.getObjectMapper().writeValueAsString(requestBody);

            executorService.submit(() -> {
                try {
                    Optional<HttpResponse<String>> responseOpt = ApiClient.post("/admin/coupons", json, token);
                    Platform.runLater(() -> {
                        if (responseOpt.isPresent() && responseOpt.get().statusCode() == 201) {
                            errorMessageLabel.setText("coupon created successfully.");
                            PauseTransition pause = new PauseTransition(Duration.seconds(3));
                            pause.setOnFinished(e -> errorMessageLabel.setText(""));
                            pause.play();
                            clearForm();
                            viewCoupons();
                        } else {
                            errorMessageLabel.setText(responseOpt.get().body());
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> errorMessageLabel.setText("failed to create coupon"));
                }
            });

        } catch (NumberFormatException | JsonProcessingException e) {
           errorMessageLabel.setText("please enter a valid number.");
        }
    }

    private void clearForm() {
        codeField.clear();
        typeComboBox.getSelectionModel().clearSelection();
        valueField.clear();
        minPriceField.clear();
        userCountField.clear();
        startDateField.setValue(null);
        endDateField.setValue(null);
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