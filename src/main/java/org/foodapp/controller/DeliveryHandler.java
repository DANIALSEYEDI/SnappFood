package org.foodapp.controller;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.foodapp.dao.OrderDao;
import org.foodapp.dao.UserDao;
import org.foodapp.dto.*;
import org.foodapp.model.Order;
import org.foodapp.model.OrderStatus;
import org.foodapp.model.User;
import org.foodapp.util.QueryParser;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DeliveryHandler implements HttpHandler {

    private final OrderDao orderDao = new OrderDao();
    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        if (path.equals("/deliveries/available") && method.equals("GET")) {
            handleAvailableDeliveries(exchange);
        } else if (path.equals("/deliveries/history") && method.equals("GET")) {
            handleDeliveryHistory(exchange);
        } else if (path.matches("/deliveries/\\d+") && method.equalsIgnoreCase("PATCH")) {
            long id = extractId(path, "/deliveries/");
            handleUpdateDeliveryStatus(exchange, id);
        } else {
            sendJson(exchange, 404,"Path not found");
        }
    }

    private void handleAvailableDeliveries(HttpExchange exchange) throws IOException {
        List<Order> orders = orderDao.findByStatus(OrderStatus.PENDING);
        List<DeliveryResponse> result = orders.stream()
                .map(DeliveryResponse::fromEntity)
                .collect(Collectors.toList());
        sendJson(exchange, 200, result);
    }

    private void handleDeliveryHistory(HttpExchange exchange) throws IOException {
        Map<String, String> params = QueryParser.parse(exchange.getRequestURI().getQuery());
        User courier = getAuthenticatedUser(exchange);
        List<Order> orders = orderDao.findDeliveriesByCourier(courier, params);
        List<DeliveryResponse> result = orders.stream()
                .map(DeliveryResponse::fromEntity)
                .collect(Collectors.toList());
        sendJson(exchange, 200, result);
    }

    private void handleUpdateDeliveryStatus(HttpExchange exchange, long id) throws IOException {
        Order order = orderDao.findById(id);
        if (order == null) {
            sendJson(exchange, 404, Map.of("error", "Order not found"));
            return;
        }

        DeliveryStatusUpdateRequest request = gson.fromJson(
                new InputStreamReader(exchange.getRequestBody()),
                DeliveryStatusUpdateRequest.class
        );

        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(request.status.toUpperCase());
        } catch (IllegalArgumentException e) {
            sendJson(exchange, 400, Map.of("error", "Invalid status value"));
            return;
        }

        if (order.getCourier() == null)
            order.setCourier(getAuthenticatedUser(exchange)); // اولین کسی که قبول می‌کند

        order.setStatus(newStatus);
        orderDao.save(order);
        sendJson(exchange, 200, Map.of("message", "Status updated", "order", DeliveryResponse.fromEntity(order)));
    }

    private long extractId(String path, String prefix) {
        try {
            return Long.parseLong(path.substring(prefix.length()));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private User getAuthenticatedUser(HttpExchange exchange) {
        return new UserDao().findById(1L); // فرضی برای تست
    }

    private void sendJson(HttpExchange exchange, int statusCode, Object body) throws IOException {
        String json = new Gson().toJson(body);
        byte[] bytes = json.getBytes();
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.getResponseBody().close();
    }
}
