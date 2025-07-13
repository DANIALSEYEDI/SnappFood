package org.foodapp.controller;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.foodapp.dao.*;
import org.foodapp.model.*;
import org.foodapp.dto.*;

import java.io.InputStreamReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class RatingHandler implements HttpHandler {

    private final RatingDao ratingDao = new RatingDao();
    private final OrderDao orderDao = new OrderDao();
    private final UserDao userDao = new UserDao();
    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        if (path.equals("/ratings") && method.equals("POST")) {
            handleSubmitRating(exchange);
        } else if (path.matches("/ratings/\\d+") && method.equals("GET")) {
            long id = extractId(path, "/ratings/");
            handleGetRating(exchange, id);
        } else if (path.matches("/ratings/\\d+") && method.equals("DELETE")) {
            long id = extractId(path, "/ratings/");
            handleDeleteRating(exchange, id);
        } else if (path.matches("/ratings/\\d+") && method.equals("PUT")) {
            long id = extractId(path, "/ratings/");
            handleUpdateRating(exchange, id);
        } else if (path.matches("/ratings/items/\\d+") && method.equals("GET")) {
            long itemId = extractId(path, "/ratings/items/");
            handleGetRatingsForItem(exchange, itemId);
        } else {
            exchange.sendResponseHeaders(404, -1);
        }
    }

    private void handleSubmitRating(HttpExchange exchange) throws IOException {
        RatingRequest request = gson.fromJson(new InputStreamReader(exchange.getRequestBody()), RatingRequest.class);
        if (request.order_id == null || request.rating == null || request.comment == null) {
            sendJson(exchange, 400, Map.of("error","Missing required fields"));
            return;
        }

        Order order = orderDao.findById(request.order_id);
        if (order == null) {
            sendJson(exchange, 404, Map.of("error","Order not found"));
            return;
        }

        Rating rating = new Rating();
        rating.setOrder(order);
        rating.setRating(request.rating);
        rating.setComment(request.comment);
        rating.setImageBase64(request.imageBase64);
        rating.setUser(order.getUser());

        ratingDao.save(rating);
        exchange.sendResponseHeaders(200, -1);
    }

    private void handleGetRating(HttpExchange exchange, long id) throws IOException {
        Rating rating = ratingDao.findById(id);
        if (rating == null) {
            sendJson(exchange, 404, Map.of("error", "Rating not found"));
            return;
        }
        RatingResponse dto = RatingResponse.fromEntity(rating);
        sendJson(exchange, 200, dto);
    }


    private void handleDeleteRating(HttpExchange exchange, long id) throws IOException {
        Rating rating = ratingDao.findById(id);
        if (rating == null) {
            sendJson(exchange, 404, Map.of("error","Rating not found"));
            return;
        }
        ratingDao.delete(rating);
        sendJson(exchange, 200, Map.of("message", "Rating deleted"));
    }

    private void handleUpdateRating(HttpExchange exchange, long id) throws IOException {
        Rating rating = ratingDao.findById(id);
        if (rating == null) {
            sendJson(exchange, 404, Map.of("error", "Rating not found"));
            return;
        }

        RatingRequest request = gson.fromJson(new InputStreamReader(exchange.getRequestBody()), RatingRequest.class);
        if (request.rating != null) rating.setRating(request.rating);
        if (request.comment != null) rating.setComment(request.comment);
        if (request.imageBase64 != null) rating.setImageBase64(request.imageBase64);

        ratingDao.save(rating);
        RatingResponse dto = RatingResponse.fromEntity(rating);
        sendJson(exchange, 200, dto);
    }


    private void handleGetRatingsForItem(HttpExchange exchange, long itemId) throws IOException {
        List<Rating> ratings = ratingDao.findByItemId(itemId);
        double avg = ratings.stream().mapToInt(Rating::getRating).average().orElse(0);

        List<RatingResponse> dtoList = ratings.stream()
                .map(RatingResponse::fromEntity)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("avg_rating", avg);
        response.put("comments", dtoList);
        sendJson(exchange, 200, response);
    }


    private long extractId(String path, String prefix) {
        try {
            return Long.parseLong(path.substring(prefix.length()));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    // ارسال JSON
    private void sendJson(HttpExchange exchange, int status, Object data) throws IOException {
        String json = gson.toJson(data);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, json.getBytes().length);
        exchange.getResponseBody().write(json.getBytes());
        exchange.close();
    }
}
