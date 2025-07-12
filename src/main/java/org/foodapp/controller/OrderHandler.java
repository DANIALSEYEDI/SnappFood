package org.foodapp.controller;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.foodapp.dao.*;
import org.foodapp.model.*;
import org.foodapp.dto.*;
import org.foodapp.util.QueryParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

public class OrderHandler implements HttpHandler {

    private final OrderDao orderDao = new OrderDao();
    private final FoodItemDao foodItemDao = new FoodItemDao();
    private final RestaurantDao restaurantDao = new RestaurantDao();
    private final UserDao userDao = new UserDao();
    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        if (path.equals("/orders") && method.equals("POST")) {
            handleSubmitOrder(exchange);
        } else if (path.matches("/orders/\\d+") && method.equals("GET")) {
            long id = extractId(path, "/orders/");
            handleGetOrder(exchange, id);
        } else if (path.equals("/orders/history") && method.equals("GET")) {
            handleGetOrderHistory(exchange);
        } else {
            sendNotFound(exchange, "Endpoint not found");
        }
    }

    private void handleSubmitOrder(HttpExchange exchange) throws IOException {
        try {
            SubmitOrderRequest req = gson.fromJson(new InputStreamReader(exchange.getRequestBody()), SubmitOrderRequest.class);
            if (req.vendor_id == null || req.delivery_address == null || req.items == null || req.items.isEmpty()) {
                sendBadRequest(exchange, "Missing required fields");
                return;
            }

            Restaurant vendor = restaurantDao.findById(req.vendor_id);
            if (vendor == null) {
                sendNotFound(exchange, "Vendor not found");
                return;
            }

            Order order = new Order();
            order.setRestaurant(vendor);
            order.setDeliveryAddress(req.delivery_address);
            order.setStatus(OrderStatus.PENDING);
            order.setUser(getAuthenticatedUser(exchange));

            for (OrderItemRequest itemReq : req.items) {
                FoodItem item = foodItemDao.findById(itemReq.item_id);
                if (item == null) continue;
                order.addItem(item, itemReq.quantity);
            }

            orderDao.save(order);
            sendJson(exchange, 200, AdminOrderResponse.fromEntity(order));
        } catch (Exception e) {
            sendServerError(exchange, "Failed to submit order: " + e.getMessage());
        }
    }

    private void handleGetOrder(HttpExchange exchange, long id) throws IOException {
        Order order = orderDao.findById(id);
        if (order == null) {
            sendNotFound(exchange, "Order not found");
            return;
        }
        sendJson(exchange, 200, AdminOrderResponse.fromEntity(order));
    }

    private void handleGetOrderHistory(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> params = QueryParser.parse(query);
        String search = params.get("search");
        String vendor = params.get("vendor");

        User currentUser = getAuthenticatedUser(exchange);
        List<Order> orders = orderDao.findHistoryByUser(currentUser, search, vendor);
        List<AdminOrderResponse> dtoList = orders.stream()
                .map(AdminOrderResponse::fromEntity)
                .collect(Collectors.toList());

        sendJson(exchange, 200, dtoList);
    }

    // متدهای کمکی

    private User getAuthenticatedUser(HttpExchange exchange) {
        return userDao.findById(1L); // فرضی برای تست
    }

    private long extractId(String path, String prefix) {
        try {
            return Long.parseLong(path.substring(prefix.length()));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void sendJson(HttpExchange exchange, int statusCode, Object data) throws IOException {
        String response = gson.toJson(data);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        exchange.getResponseBody().write(response.getBytes());
        exchange.getResponseBody().close();
    }

    private void sendBadRequest(HttpExchange exchange, String message) throws IOException {
        sendJson(exchange, 400, Map.of("error", message));
    }

    private void sendNotFound(HttpExchange exchange, String message) throws IOException {
        sendJson(exchange, 404, Map.of("error", message));
    }

    private void sendServerError(HttpExchange exchange, String message) throws IOException {
        sendJson(exchange, 500, Map.of("error", message));
    }
}

