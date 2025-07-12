package org.foodapp.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.foodapp.dao.FoodItemDao;
import org.foodapp.dao.UserDao;
import org.foodapp.dto.FoodItemResponse;
import org.foodapp.dto.ItemFilterRequest;
import org.foodapp.model.FoodItem;
import org.foodapp.model.Role;
import org.foodapp.model.User;
import org.foodapp.util.JwtUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class ItemsHandler implements HttpHandler{
    private final FoodItemDao foodItemDao = new FoodItemDao();
    private final Gson gson = new Gson();
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        URI uri = exchange.getRequestURI();
        String path = uri.getPath();
        try {
            if (path.equals("/items") && method.equals("POST")) {
                handleFilterItems(exchange);
            }
            else if (path.matches("/items/\\d+") && method.equalsIgnoreCase("GET")) {
                long id = extractId(path, "/items/");
                handleGetItem(exchange, id);
            }
         else {
                sendJson(exchange, 404, "{\"error\": \"Not found\"}");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, "{\"error\": \"Internal server error\"}");
        }
    }





    private void handleFilterItems(HttpExchange exchange) throws IOException {
        try {
            User user = authenticate(exchange);
            if (user == null || user.getRole() != Role.BUYER) {
                sendJson(exchange, 403, "{\"error\": \"Only buyers can access this endpoint\"}");
                return;
            }
            String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
            if (contentType == null || !contentType.contains("application/json")) {
                sendJson(exchange, 415, "{\"error\": \"Unsupported Media Type\"}");
                return;
            }

            ItemFilterRequest request = gson.fromJson(
                    new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8),
                    ItemFilterRequest.class
            );

            List<FoodItem> items = foodItemDao.findByFilters(request.search, request.price, request.keywords);
            if (items.isEmpty()) {
                sendJson(exchange, 404, "{\"error\": \"No items found\"}");
                return;
            }

            List<FoodItemResponse> response = items.stream()
                    .map(FoodItemResponse::new)
                    .collect(Collectors.toList());


            sendJson(exchange, 200, gson.toJson(response));
        }
        catch (JsonSyntaxException e) {
            sendJson(exchange, 400, "{\"error\": \"Invalid input format\"}");
        }
        catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 400, Map.of("error", "Invalid input"));
        }
    }





    private void handleGetItem(HttpExchange exchange, long id) throws IOException {
        try {
            User user = authenticate(exchange);
            if (user == null) {
                sendJson(exchange, 401, "{\"error\": \"Unauthorized\"}");
                return;
            }

            if (user.getRole() != Role.BUYER) {
                sendJson(exchange, 403, "{\"error\": \"Only buyers can access this endpoint\"}");
                return;
            }


            FoodItem item = foodItemDao.findById(id);
            if (item == null) {
                sendJson(exchange, 404, Map.of("error", "Item not found"));
                return;
            }
            FoodItemResponse response = new FoodItemResponse(item);
            sendJson(exchange, 200, gson.toJson(response));
        }
        catch (NumberFormatException e) {
            sendJson(exchange, 400, "{\"error\": \"Invalid item ID\"}");
        }
        catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, Map.of("error", "Internal server error"));
        }
    }





    private final ObjectMapper objectMapper = new ObjectMapper();

    private <T> T readJson(InputStream inputStream, Class<T> clazz) throws IOException {
        return objectMapper.readValue(inputStream, clazz);
    }

    private void sendJson(HttpExchange exchange, int statusCode, Object data) throws IOException {
        byte[] jsonBytes = objectMapper.writeValueAsBytes(data);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, jsonBytes.length);
        exchange.getResponseBody().write(jsonBytes);
        exchange.getResponseBody().close();
    }

    private long extractId(String path, String prefix) {
        if (!path.startsWith(prefix)) {
            throw new IllegalArgumentException("Invalid path prefix");
        }
        try {
            String idStr = path.substring(prefix.length());
            return Long.parseLong(idStr);
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
            sendJson(exchange, 401, "{\"error\": \"Invalid token\"}");
            return null;
        }

        return new UserDao().findById(Long.parseLong(decoded.getSubject()));
    }
}
