package org.foodapp.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.foodapp.dao.*;
import org.foodapp.model.*;
import org.foodapp.dto.*;
import org.foodapp.util.JwtUtil;
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
            sendJson(exchange, 404, "{\"error\": \"Not found\"}");
        }
    }





    private void handleSubmitOrder(HttpExchange exchange) throws IOException {
        try {
            User user = authenticate(exchange);
            if (user == null || user.getRole() != Role.BUYER) {
                sendJson(exchange, 403, "{\"error\": \"Only buyers can submit orders\"}");
                return;
            }
            String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
            if (contentType == null || !contentType.contains("application/json")) {
                sendJson(exchange, 415, "{\"error\": \"Unsupported Media Type\"}");
                return;
            }

            OrderSubmitRequest req = gson.fromJson(new InputStreamReader(exchange.getRequestBody()), OrderSubmitRequest.class);
            if (req.vendor_id == null || req.delivery_address == null || req.items == null || req.items.isEmpty()) {
                sendJson(exchange, 400, "{\"error\": \"Missing required fields\"}");
                return;
            }

            Restaurant vendor = restaurantDao.findById(req.vendor_id);
            if (vendor == null) {
                sendJson(exchange, 404, "{\"error\": \"Vendor not found\"}");
                return;
            }

            Order order = new Order();
            order.setRestaurant(vendor);
            order.setDeliveryAddress(req.delivery_address);
            order.setStatus(OrderStatus.PENDING);
            order.setUser(user);

            for (OrderItemRequest itemReq : req.items) {
                FoodItem item = foodItemDao.findById(itemReq.item_id);
                if (item == null) continue;
                order.addItem(item, itemReq.quantity);
            }

            orderDao.save(order);
            sendJson(exchange, 200, OrderAdminResponse.fromEntity(order));
        }
        catch (JsonSyntaxException e){
                sendJson(exchange, 400, "{\"error\": \"Invalid input format\"}");
        }
        catch (Exception e) {
            sendJson(exchange, 500, "{\"error\": \"Internal server error\"}");
        }
    }









    private void handleGetOrder(HttpExchange exchange, long id) throws IOException {
        Order order = orderDao.findById(id);
        if (order == null) {
            sendJson(exchange, 404, "{\"error\": \"Order not found\"}");
            return;
        }
        sendJson(exchange, 200, OrderAdminResponse.fromEntity(order));
    }







    private void handleGetOrderHistory(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> params = QueryParser.parse(query);
        String search = params.get("search");
        String vendor = params.get("vendor");

        User currentUser = getAuthenticatedUser(exchange);
        List<Order> orders = orderDao.findHistoryByUser(currentUser, search, vendor);
        List<OrderAdminResponse> dtoList = orders.stream()
                .map(OrderAdminResponse::fromEntity)
                .collect(Collectors.toList());

        sendJson(exchange, 200, dtoList);
    }









    private User getAuthenticatedUser(HttpExchange exchange) {
        return userDao.findById(1L);
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
            sendJson(exchange, 401, "{\"error\": \"Invalid token\"}");
            return null;
        }
        return userDao.findById(Long.parseLong(decoded.getSubject()));
    }

}

