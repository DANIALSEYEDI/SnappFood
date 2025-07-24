package org.foodapp.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.foodapp.dao.OrderDao;
import org.foodapp.dao.UserDao;
import org.foodapp.dto.*;
import org.foodapp.model.*;
import org.foodapp.util.JwtUtil;
import org.foodapp.util.QueryParser;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
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
            sendJson(exchange, 404,"not_found path");
        }
    }



    private void handleAvailableDeliveries(HttpExchange exchange) throws IOException {
        try {
            User user = authenticate(exchange);
            if (user == null) {
                return;
            }
            if (user.getRole() != Role.COURIER || user.getStatus()==UserStatus.REJECTED) {
                sendJson(exchange, 403, Map.of("error", "forbidden User"));
                return;
            }
            List<Order> orders = orderDao.findAvailableForDelivery();
            List<OrderResponse> result = orders.stream()
                    .map(OrderResponse::fromEntity)
                    .collect(Collectors.toList());
            sendJson(exchange, 200, result);
        }
        catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, Map.of("error", "internal_server_error"));
        }
    }



   private void handleDeliveryHistory(HttpExchange exchange) throws IOException {
        try {
            User courier = authenticate(exchange);
            if (courier==null) {
                return;
            }
            if (courier.getStatus()==UserStatus.REJECTED|| courier.getRole() != Role.COURIER) {
                sendJson(exchange, 403, Map.of("error", "forbidden User"));
                return;
            }
            Map<String, String> params = QueryParser.parse(exchange.getRequestURI().getQuery());
            String search = params.getOrDefault("search", null);
            String vendor = params.getOrDefault("vendor", null);
            String userPhone = params.getOrDefault("user", null);

            List<Order> orders = orderDao.findDeliveryHistoryByCourier(courier, search, vendor, userPhone);
            List<OrderResponse> result = orders.stream()
                    .map(OrderResponse::fromEntity)
                    .collect(Collectors.toList());

            sendJson(exchange, 200, result);
        }
        catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, Map.of("error", "Internal_server_error"));
        }
    }



    private void handleUpdateDeliveryStatus(HttpExchange exchange, long id) throws IOException {
        try {
            User courier = authenticate(exchange);
            if (courier == null) return;
            if (courier.getRole() != Role.COURIER || courier.getStatus()==UserStatus.REJECTED) {
                sendJson(exchange, 403, Map.of("error", "forbidden User"));
                return;
            }
            DeliveryStatusUpdateRequest request = gson.fromJson(
                    new InputStreamReader(exchange.getRequestBody()),
                    DeliveryStatusUpdateRequest.class
            );
            if (request.status == null) {
                sendJson(exchange, 400, Map.of("error", "invalid_input"));
                return;
            }
            OrderDeliveryStatus newStatus;
            try {
                newStatus = OrderDeliveryStatus.valueOf(request.status.toUpperCase());
            } catch (IllegalArgumentException e) {
                sendJson(exchange, 400, Map.of("error", "Invalid_input delivery status"));
                return;
            }

            Order order = orderDao.findById(id);
            if (order == null) {
                sendJson(exchange, 404, Map.of("error", "not_found Order"));
                return;
            }

            if (order.getStatus()==OrderStatus.CANCELLED || order.getStatus()==OrderStatus.UNPAID_AND_CANCELLED ||
                    order.getStatus()==OrderStatus.SUBMITTED || order.getStatus()==OrderStatus.WAITING_VENDOR
            ){
                sendJson(exchange, 403, Map.of("error", "Order not ready for delivery"));
                return;
            }

            OrderDeliveryStatus current = order.getDeliveryStatus();
            boolean isValidTransition = switch (current) {
                case PENDING -> newStatus == OrderDeliveryStatus.ACCEPTED;
                case ACCEPTED -> newStatus == OrderDeliveryStatus.RECEIVED;
                case RECEIVED -> newStatus == OrderDeliveryStatus.DELIVERED;
                default -> false;
            };

            if (!isValidTransition) {
                sendJson(exchange, 400, Map.of("error", "Invalid delivery status transition"));
                return;
            }

            if (order.getCourier() == null) {
                order.setCourier(courier);
            } else if (!order.getCourier().getId().equals(courier.getId())) {
                sendJson(exchange, 403, Map.of("error", "This delivery is assigned to another courier"));
                return;
            }

            if (newStatus==OrderDeliveryStatus.ACCEPTED){
                order.setStatus(OrderStatus.ON_THE_WAY);
            }
            else if (newStatus==OrderDeliveryStatus.RECEIVED){
                order.setStatus(OrderStatus.ON_THE_WAY);
            }
            else if(newStatus==OrderDeliveryStatus.DELIVERED){
                order.setStatus(OrderStatus.COMPLETED);
            }
            order.setDeliveryStatus(newStatus);
            order.setUpdatedAt(LocalDateTime.now());
            orderDao.update(order);
            sendJson(exchange, 200, Map.of(
                    "message", "Changed status successfully",
                    "order", OrderResponse.fromEntity(order)
            ));
        }
        catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, Map.of("error", "internal_server_error"));
        }
    }



    private long extractId(String path, String prefix) {
        try {
            String trimmed = path.substring(prefix.length());
            return Long.parseLong(trimmed.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid path format for extracting ID");
        }
    }


    private User authenticate(HttpExchange exchange) throws IOException {
        List<String> authHeaders = exchange.getRequestHeaders().get("Authorization");
        if (authHeaders == null || authHeaders.isEmpty()) {
            sendJson(exchange, 401, "{\"error\": \"Missing Authorization header\"}");
            return null;
        }
        String token = authHeaders.get(0).replace("Bearer ", "");
        DecodedJWT decoded;
        try {
            decoded = JwtUtil.verifyToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 401, "{\"error\": \"Invalid token\"}");
            return null;
        }
        return new UserDao().findById(Long.parseLong(decoded.getSubject()));
    }


    private void sendJson(HttpExchange exchange, int statusCode, Object body) throws IOException {
        String json = new Gson().toJson(body);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}