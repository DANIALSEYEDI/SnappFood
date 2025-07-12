package org.foodapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.foodapp.dao.FoodItemDao;
import org.foodapp.dto.ItemFilterRequest;
import org.foodapp.model.FoodItem;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;

public class BuyerHandler implements HttpHandler{
    private final FoodItemDao foodItemDao = new FoodItemDao();
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        URI uri = exchange.getRequestURI();
        String path = uri.getPath();
        try {
            if (path.equals("/items") && method.equals("POST")) {
                handleFilterItems(exchange);
            } else if (path.matches("/items/\\d+") && method.equals("GET")) {
                long id = extractId(path, "/items/", "");
                handleGetItem(exchange, id);
            } else {
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
            ItemFilterRequest request = readJson(exchange.getRequestBody(), ItemFilterRequest.class);
            List<FoodItem> items = foodItemDao.findByFilters(
                    request.getSearch(),
                    request.getPrice(),
                    request.getKeywords()
            );
            sendJson(exchange, 200, items);
        } catch (Exception e) {
            e.printStackTrace(); // اختیاری برای لاگ
            sendJson(exchange, 400, Map.of("error", "Invalid input"));
        }
    }


    private void handleGetItem(HttpExchange exchange, long id) throws IOException {
        try {
            FoodItem item = foodItemDao.findById(id);
            if (item == null) {
                sendJson(exchange, 404, Map.of("error", "Item not found"));
                return;
            }
            sendJson(exchange, 200, item);
        } catch (Exception e) {
            e.printStackTrace(); // اختیاری برای لاگ
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


    private long extractId(String path, String prefix, String suffix) {
        try {
            String temp = path.substring(prefix.length());
            int endIndex = temp.indexOf(suffix);
            if (endIndex == -1) {
                return Long.parseLong(temp);
            } else {
                return Long.parseLong(temp.substring(0, endIndex));
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid path format for extracting ID");
        }
    }
}
