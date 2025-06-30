package org.foodapp.controller;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.foodapp.dao.RestaurantDao;
import org.foodapp.dao.UserDao;
import org.foodapp.dto.RestaurantRequest;
import org.foodapp.model.Restaurant;
import org.foodapp.model.Seller;
import org.foodapp.model.User;
import org.foodapp.util.JwtUtil;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.hibernate.Session;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class RestaurantHandler implements HttpHandler {

    private final Gson gson = new Gson();
    private final RestaurantDao restaurantDao = new RestaurantDao();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        URI uri = exchange.getRequestURI();
        String path = uri.getPath();

        try {
            if (path.equals("/restaurants") && method.equals("POST")) {
                handleCreate(exchange);
            } else if (path.equals("/restaurants/mine") && method.equals("GET")) {
                handleGetMyRestaurants(exchange);
            } else if (path.matches("/restaurants/\\d+") && method.equals("PUT")) {
                handleUpdate(exchange);
            } else {
                sendJson(exchange, 404, "{\"error\": \"Not found\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, "{\"error\": \"Internal server error\"}");
        }
    }

    private void handleCreate(HttpExchange exchange) throws IOException {
        User user = authenticate(exchange);
        if (user == null) return;
        if (!(user instanceof Seller)) {
            sendJson(exchange, 403, "{\"error\": \"Only sellers can create restaurants\"}");
            return;
        }

        RestaurantRequest request = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), RestaurantRequest.class);
        if (request.name == null || request.address == null || request.phone == null) {
            sendJson(exchange, 400, "{\"error\": \"Invalid input\"}");
            return;
        }

        Restaurant restaurant = new Restaurant(
                request.name,
                request.address,
                request.phone,
                request.logoBase64,
                request.tax_fee,
                request.additional_fee,
                (Seller) user
        );

        restaurantDao.save(restaurant);
        sendJson(exchange, 201, gson.toJson(restaurant));
    }

    private void handleGetMyRestaurants(HttpExchange exchange) throws IOException {
        User user = authenticate(exchange);
        if (user == null) return;
        if (!(user instanceof Seller)) {
            sendJson(exchange, 403, "{\"error\": \"Only sellers can view their restaurants\"}");
            return;
        }

        List<Restaurant> restaurants = restaurantDao.findBySeller(user.getId());
        sendJson(exchange, 200, gson.toJson(restaurants));
    }

    private void handleUpdate(HttpExchange exchange) throws IOException {
        User user = authenticate(exchange);
        if (user == null) return;
        if (!(user instanceof Seller)) {
            sendJson(exchange, 403, "{\"error\": \"Only sellers can update restaurants\"}");
            return;
        }

        String path = exchange.getRequestURI().getPath();
        int id = Integer.parseInt(path.substring(path.lastIndexOf("/") + 1));
        Restaurant restaurant = restaurantDao.findById((long) id);
        if (restaurant == null || restaurant.getSeller().getId() != user.getId()) {
            sendJson(exchange, 404, "{\"error\": \"Restaurant not found or not authorized\"}");
            return;
        }

        RestaurantRequest request = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), RestaurantRequest.class);
        if (request.name != null) restaurant.setName(request.name);
        if (request.address != null) restaurant.setAddress(request.address);
        if (request.phone != null) restaurant.setPhone(request.phone);
        if (request.logoBase64 != null) restaurant.setLogoBase64(request.logoBase64);
        if (request.tax_fee != null) restaurant.setTaxFee(request.tax_fee);
        if (request.additional_fee != null) restaurant.setAdditionalFee(request.additional_fee);

        restaurantDao.update(restaurant);
        sendJson(exchange, 200, gson.toJson(restaurant));
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
        return new UserDao().findById(Long.parseLong(decoded.getSubject()));
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
