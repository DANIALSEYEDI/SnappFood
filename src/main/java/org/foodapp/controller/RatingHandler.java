package org.foodapp.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.foodapp.dao.*;
import org.foodapp.model.*;
import org.foodapp.dto.*;
import org.foodapp.util.GsonProvider;
import org.foodapp.util.JwtUtil;

import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class RatingHandler implements HttpHandler {

    private final RatingDao ratingDao = new RatingDao();
    private final OrderDao orderDao = new OrderDao();
    private final UserDao userDao = new UserDao();
    private final Gson gson = GsonProvider.INSTANCE;
    private final FoodItemDao foodItemDao = new FoodItemDao();

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
            sendJson(exchange, 404, "not_found path");
        }
    }


    private void handleSubmitRating(HttpExchange exchange) throws IOException {
        try {
            User user = authenticate(exchange);
            if (user == null || user.getRole() != Role.BUYER) {
                sendJson(exchange, 403, "{\"error\": \"Only buyers can rate\"}");
                return;
            }

            String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
            if (contentType == null || !contentType.contains("application/json")) {
                sendJson(exchange, 415, "{\"error\": \"Unsupported Media Type\"}");
                return;
            }

            RatingRequest request = gson.fromJson(new InputStreamReader(exchange.getRequestBody()), RatingRequest.class);
            if (request.order_id == null || request.rating == null || request.rating < 1 || request.rating > 5 || request.comment == null) {
                sendJson(exchange, 400, Map.of("error", "Missing required fields"));
                return;
            }

            Order order = orderDao.findById(request.order_id);
            if (order == null || !order.getUser().getId().equals(user.getId())) {
                sendJson(exchange, 404, Map.of("error", "Order not found"));
                return;
            }
            List<OrderItem> items = order.getItemsOfOrder();
            if (items == null || items.isEmpty()) {
                sendJson(exchange, 400, Map.of("error", "Order has no items"));
                return;
            }

            for (OrderItem item : items) {
                Rating rating = new Rating();
                rating.setOrder(order);
                rating.setUser(user);
                rating.setRating(request.rating);
                rating.setComment(request.comment);
                rating.setImageBase64(request.imageBase64 != null ? request.imageBase64 : List.of());
                rating.setItem(item.getItem());

                ratingDao.save(rating);
            }
            sendJson(exchange, 200, "{\"message\": \"Rating submitted successfully\"}");
        } catch (JsonSyntaxException e) {
            sendJson(exchange, 400, "{\"error\": \"Invalid input format\"}");
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, "{\"error\": \"Internal server error\"}");
        }
    }







    private void handleGetRatingsForItem(HttpExchange exchange, long itemId) throws IOException {
        try {
            User user = authenticate(exchange);
            if (user == null) {
                return;
            }

            FoodItem item = foodItemDao.findById(itemId);
            if (item == null) {
                sendJson(exchange, 404, "{\"error\": \"Item not found\"}");
                return;
            }

            List<Rating> ratings = ratingDao.findByItemId(itemId);
            if (ratings.isEmpty()) {
                sendJson(exchange, 200, gson.toJson(new RatingResponse(0.0, List.of())));
                return;
            }

            double avg = ratings.stream().mapToInt(Rating::getRating).average().orElse(0.0);
            RatingResponse response = new RatingResponse(avg, ratings);
            sendJson(exchange, 200, gson.toJson(response));


        } catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, "{\"error\": \"Internal server error\"}");
        }
    }





    private void handleGetRating(HttpExchange exchange, long id) throws IOException {
        try {
            User user = authenticate(exchange);
            if (user == null) {
                return;
            }
            Rating rating = ratingDao.findById(id);
            if (rating == null) {
                sendJson(exchange, 404, Map.of("error", "Rating not found"));
                return;
            }
            RatingDto dto = RatingDto.fromEntity(rating);
            sendJson(exchange, 200, gson.toJson(dto));
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, "{\"error\": \"Internal server error\"}");
        }
    }







    private void handleDeleteRating(HttpExchange exchange, long id) throws IOException {
        try {
            User user = authenticate(exchange);
            if (user == null) return;
            Rating rating = ratingDao.findById(id);
            if (rating == null) {
                sendJson(exchange, 404, Map.of("error", "Rating not found"));
                return;
            }
            if (!rating.getUser().getId().equals(user.getId())) {
                sendJson(exchange, 403, Map.of("error", "You can only delete your own rating"));
                return;
            }

            ratingDao.delete(rating);
            sendJson(exchange, 200, Map.of("message", "Rating deleted"));
        }
        catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, "{\"error\": \"Internal server error\"}");
        }
    }








    private void handleUpdateRating(HttpExchange exchange, long id) throws IOException {
        try {
            User user = authenticate(exchange);
            if (user == null) return;
            String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
            if (contentType == null || !contentType.contains("application/json")) {
                sendJson(exchange, 415, Map.of("error", "Unsupported Media Type"));
                return;
            }


            Rating rating = ratingDao.findById(id);
            if (rating == null) {
                sendJson(exchange, 404, Map.of("error", "Rating not found"));
                return;
            }
            if (!rating.getUser().getId().equals(user.getId())) {
                sendJson(exchange, 403, Map.of("error", "You can only edit your own rating"));
                return;
            }

            JsonObject body = JsonParser.parseReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)).getAsJsonObject();
            if (body.has("rating")) {
                int newRating = body.get("rating").getAsInt();
                if (newRating < 1 || newRating > 5) {
                    sendJson(exchange, 400, Map.of("error", "Rating must be between 1 and 5"));
                    return;
                }
                rating.setRating(newRating);
            }

            if (body.has("comment")) {
                rating.setComment(body.get("comment").getAsString());
            }

            if (body.has("imageBase64")) {
                List<String> images = new ArrayList<>();
                for (JsonElement el : body.getAsJsonArray("imageBase64")) {
                    images.add(el.getAsString());
                }
                rating.setImageBase64(images);
            }

            ratingDao.update(rating);
            RatingDto dto = RatingDto.fromEntity(rating);
            sendJson(exchange, 200, gson.toJson(dto));
        }
        catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, "{\"error\": \"Internal server error\"}");
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
        return userDao.findById(Long.parseLong(decoded.getSubject()));
    }



    private long extractId(String path, String prefix) {
        try {
            return Long.parseLong(path.substring(prefix.length()));
        } catch (NumberFormatException e) {
            return -1;
        }
    }



    private void sendJson(HttpExchange exchange, int status, Object data) throws IOException {
        String json = gson.toJson(data);
        byte[] responseBytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, responseBytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}

