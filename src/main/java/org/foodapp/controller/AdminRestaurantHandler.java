package org.foodapp.controller;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.foodapp.dao.RestaurantDao;
import org.foodapp.dto.UpdateRestaurantStatusRequest;
import org.foodapp.model.Restaurant;
import org.foodapp.model.RestaurantStatus;
import org.foodapp.util.JwtUtil;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class AdminRestaurantHandler implements HttpHandler {

    private final Gson gson = new Gson();
    private final RestaurantDao restaurantDao = new RestaurantDao();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if ("PUT".equalsIgnoreCase(method) && path.startsWith("/admin/restaurants/")) {
            handleUpdateStatus(exchange, path);
        } else if ("GET".equalsIgnoreCase(method) && path.equals("/admin/restaurants/pending")) {
            handlePendingList(exchange);
        } else {
            sendJson(exchange, 404, "{\"error\": \"Not found\"}");
        }
    }

    private void handleUpdateStatus(HttpExchange exchange, String path) throws IOException {
        String token = extractToken(exchange);
        if (!isAdmin(token)) {
            sendJson(exchange, 403, "{\"error\": \"Forbidden\"}");
            return;
        }

        Long id = Long.parseLong(path.replace("/admin/restaurants/", ""));

        InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        UpdateRestaurantStatusRequest request = gson.fromJson(reader, UpdateRestaurantStatusRequest.class);

        Restaurant restaurant = restaurantDao.findById(id);
        if (restaurant == null) {
            sendJson(exchange, 404, "{\"error\": \"Restaurant not found\"}");
            return;
        }

        try {
            RestaurantStatus status = RestaurantStatus.valueOf(request.status.toUpperCase());
            restaurant.setStatus(status);
            restaurantDao.update(restaurant);
            sendJson(exchange, 200, "{\"message\": \"Restaurant status updated\"}");
        } catch (IllegalArgumentException e) {
            sendJson(exchange, 400, "{\"error\": \"Invalid status value\"}");
        }
    }

    private void handlePendingList(HttpExchange exchange) throws IOException {
        String token = extractToken(exchange);
        if (!isAdmin(token)) {
            sendJson(exchange, 403, "{\"error\": \"Forbidden\"}");
            return;
        }

        var pending = restaurantDao.findByStatus(RestaurantStatus.PENDING);
        sendJson(exchange, 200, gson.toJson(pending));
    }

    private String extractToken(HttpExchange exchange) {
        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7);
        }
        return null;
    }

    private boolean isAdmin(String token) {
        if (token == null) return false;
        DecodedJWT decoded = JwtUtil.verifyToken(token);
        return decoded.getClaim("role").asString().equalsIgnoreCase("admin");
    }

    private void sendJson(HttpExchange exchange, int statusCode, String json) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
