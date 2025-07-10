package org.foodapp.controller;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.foodapp.dao.RestaurantDao;
import org.foodapp.dao.UserDao;
import org.foodapp.dao.FoodItemDao;
import org.foodapp.dto.CreateRestaurantRequest;
import org.foodapp.dto.RestaurantResponse;
import org.foodapp.dto.FoodItemRequest;
import org.foodapp.model.Restaurant;
import org.foodapp.model.Role;
import org.foodapp.model.User;
import org.foodapp.model.FoodItem;
import org.foodapp.util.JwtUtil;
import com.auth0.jwt.interfaces.DecodedJWT;

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
            if (path.equals("/restaurants") && method.equalsIgnoreCase("POST")) {
                handleCreate(exchange);
            } else if (path.equals("/restaurants/mine") && method.equalsIgnoreCase("GET")) {
                handleGetMyRestaurants(exchange);
            } else if (path.matches("/restaurants/\\d+") && method.equalsIgnoreCase("PUT")) {
                handleUpdate(exchange);
            }else if (path.matches("/restaurants/\\d+/item") && method.equals("POST")) {
                handleAddItem(exchange);
            } else if (path.matches("/restaurants/\\d+/item/\\d+") && method.equals("PUT")) {
                handleEditItem(exchange);
            } else if (path.matches("/restaurants/\\d+/item/\\d+") && method.equals("DELETE")) {
                handleDeleteItem(exchange);
            } else {
                sendJson(exchange, 404, "{\"error\": \"Not found\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, "{\"error\": \"Internal server error\"}");
        }
    }

    private void handleCreate(HttpExchange exchange) throws IOException {
        try {
            User user = authenticate(exchange);
            if (user == null) {
                sendJson(exchange, 401, "{\"error\": \"Unauthorized\"}");
                return;
            }
            if (user.getRole() != Role.SELLER) {
                sendJson(exchange, 403, "{\"error\": \"Only sellers can create restaurants\"}");
                return;
            }
            CreateRestaurantRequest request = gson.fromJson(
                    new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8),
                    CreateRestaurantRequest.class
            );
            if (request.name == null || request.address == null || request.phone == null) {
                sendJson(exchange, 400, "{\"error\": \"Missing required fields: name, address, or phone\"}");
                return;
            }

            Restaurant restaurant = new Restaurant(
                    request.name,
                    request.address,
                    request.phone,
                    request.logoBase64,
                    request.tax_fee,
                    request.additional_fee,
                    user
            );

            restaurantDao.save(restaurant);
            RestaurantResponse response = new RestaurantResponse(
                    restaurant.getId(),
                    restaurant.getName(),
                    restaurant.getAddress(),
                    restaurant.getPhone(),
                    restaurant.getLogoBase64(),
                    restaurant.getTaxFee(),
                    restaurant.getAdditionalFee()
            );
            sendJson(exchange, 201, gson.toJson(response));
        }
        catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, "{\"error\": \"Internal server error\"}");
        }
    }

    private void handleGetMyRestaurants(HttpExchange exchange) throws IOException {
        try {
            String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
            if (contentType == null || !contentType.toLowerCase().contains("application/json")) {
                sendJson(exchange, 415, "{\"error\": \"Unsupported Media Type\"}");
                return;
            }
                User user = authenticate(exchange);
            if (user == null) {
                sendJson(exchange, 401, "{\"error\": \"Unauthorized\"}");
                return;
            }
            if (user.getRole() != Role.SELLER) {
                sendJson(exchange, 403, "{\"error\": \"Only sellers can view their restaurants\"}");
                return;
            }
            List<Restaurant> restaurants = restaurantDao.findBySeller(user.getId());
            if (restaurants == null || restaurants.isEmpty()) {
                sendJson(exchange, 404, "{\"error\": \"No restaurants found for this seller\"}");
                return;
            }
            List<RestaurantResponse> responseList = restaurants.stream()
                    .map(r -> new RestaurantResponse(
                            r.getId(),
                            r.getName(),
                            r.getAddress(),
                            r.getPhone(),
                            r.getLogoBase64(),
                            r.getTaxFee(),
                            r.getAdditionalFee()
                    ))
                    .toList();

            sendJson(exchange, 200, gson.toJson(responseList));
        }
        catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, "{\"error\": \"Internal server error\"}");
        }
    }

    private void handleUpdate(HttpExchange exchange) throws IOException {
        try {
            String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
            if (contentType == null || !contentType.toLowerCase().contains("application/json")) {
                sendJson(exchange, 415, "{\"error\": \"Unsupported Media Type\"}");
                return;
            }
                User user = authenticate(exchange);
            if (user == null) {
                sendJson(exchange, 401, "{\"error\": \"Unauthorized\"}");
                return;
            }
            if (user.getRole() != Role.SELLER) {
                sendJson(exchange, 403, "{\"error\": \"Only sellers can update restaurants\"}");
                return;
            }
            String path = exchange.getRequestURI().getPath();
            long id;
            try {
                id = Long.parseLong(path.substring(path.lastIndexOf("/") + 1));
            } catch (NumberFormatException e) {
                sendJson(exchange, 400, "{\"error\": \"Invalid restaurant ID\"}");
                return;
            }
            Restaurant restaurant = restaurantDao.findById(id);
            if (restaurant == null) {
                sendJson(exchange, 404, "{\"error\": \"Restaurant not found\"}");
                return;
            }
            if (!restaurant.getSeller().getId().equals(user.getId())) {
                sendJson(exchange, 403, "{\"error\": \"You are not authorized to update this restaurant\"}");
                return;
            }
            CreateRestaurantRequest request = gson.fromJson(
                    new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8),
                    CreateRestaurantRequest.class
            );

            if (request.name != null) restaurant.setName(request.name);
            if (request.address != null) restaurant.setAddress(request.address);
            if (request.phone != null) restaurant.setPhone(request.phone);
            if (request.logoBase64 != null) restaurant.setLogoBase64(request.logoBase64);
            if (request.tax_fee != null) restaurant.setTaxFee(request.tax_fee);
            if (request.additional_fee != null) restaurant.setAdditionalFee(request.additional_fee);

            restaurantDao.update(restaurant);
            RestaurantResponse response = new RestaurantResponse(
                    restaurant.getId(),
                    restaurant.getName(),
                    restaurant.getAddress(),
                    restaurant.getPhone(),
                    restaurant.getLogoBase64(),
                    restaurant.getTaxFee(),
                    restaurant.getAdditionalFee()
            );
            sendJson(exchange, 200, gson.toJson(response));
        }
        catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, "{\"error\": \"Internal server error\"}");
        }
    }
    private void handleAddItem(HttpExchange exchange) throws IOException {
        User user = authenticate(exchange);
        if (user == null || user.getRole() != Role.SELLER) {
            sendJson(exchange, 403, "{\"error\": \"Only sellers can add food items\"}");
            return;
        }

        long restaurantId = extractIdFromPath(exchange.getRequestURI().getPath(), "/restaurants/", "/item");
        Restaurant restaurant = new RestaurantDao().findById(restaurantId);

        if (restaurant == null || !restaurant.getSeller().getId().equals(user.getId())) {
            sendJson(exchange, 403, "{\"error\": \"Unauthorized to modify this restaurant\"}");
            return;
        }

        FoodItemRequest request = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), FoodItemRequest.class);
        if (request.name == null || request.description == null || request.price == null || request.supply == null || request.keywords == null || request.keywords.isEmpty()) {
            sendJson(exchange, 400, "{\"error\": \"Missing required fields\"}");
            return;
        }

        FoodItem item = new FoodItem(
                request.name, request.imageBase64, request.description,
                request.price, request.supply, request.keywords, restaurant
        );
        new FoodItemDao().save(item);
        sendJson(exchange, 200, gson.toJson(item));
    }

    private void handleEditItem(HttpExchange exchange) throws IOException {
        User user = authenticate(exchange);
        if (user == null || user.getRole() != Role.SELLER) {
            sendJson(exchange, 403, "{\"error\": \"Only sellers can edit food items\"}");
            return;
        }

        String path = exchange.getRequestURI().getPath();
        long restaurantId = Long.parseLong(path.split("/")[2]);
        long itemId = Long.parseLong(path.split("/")[4]);

        Restaurant restaurant = new RestaurantDao().findById(restaurantId);
        if (restaurant == null || !restaurant.getSeller().getId().equals(user.getId())) {
            sendJson(exchange, 403, "{\"error\": \"Unauthorized\"}");
            return;
        }

        FoodItem item = new FoodItemDao().findById(itemId);
        if (item == null || !item.getRestaurant().getId().equals(restaurantId)) {
            sendJson(exchange, 404, "{\"error\": \"Item not found\"}");
            return;
        }

        FoodItemRequest request = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), FoodItemRequest.class);
        if (request.name != null) item.setName(request.name);
        if (request.imageBase64 != null) item.setImageBase64(request.imageBase64);
        if (request.description != null) item.setDescription(request.description);
        if (request.price != null) item.setPrice(request.price);
        if (request.supply != null) item.setSupply(request.supply);
        if (request.keywords != null && !request.keywords.isEmpty()) item.setKeywords(request.keywords);

        new FoodItemDao().update(item);
        sendJson(exchange, 200, gson.toJson(item));
    }

    private void handleDeleteItem(HttpExchange exchange) throws IOException {
        User user = authenticate(exchange);
        if (user == null || user.getRole() != Role.SELLER) {
            sendJson(exchange, 403, "{\"error\": \"Only sellers can delete food items\"}");
            return;
        }

        String path = exchange.getRequestURI().getPath();
        long restaurantId = Long.parseLong(path.split("/")[2]);
        long itemId = Long.parseLong(path.split("/")[4]);

        Restaurant restaurant = new RestaurantDao().findById(restaurantId);
        FoodItem item = new FoodItemDao().findById(itemId);

        if (restaurant == null || item == null || !restaurant.getSeller().getId().equals(user.getId()) || !item.getRestaurant().getId().equals(restaurantId)) {
            sendJson(exchange, 403, "{\"error\": \"Unauthorized or item not found\"}");
            return;
        }

        new FoodItemDao().delete(item);
        sendJson(exchange, 200, "{\"message\": \"Food item removed successfully\"}");
    }

    private long extractIdFromPath(String path, String prefix, String suffix) {
        return Long.parseLong(path.replace(prefix, "").replace(suffix, "").split("/")[0]);
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
