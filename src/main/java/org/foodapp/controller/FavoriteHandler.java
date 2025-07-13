package org.foodapp.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.foodapp.dao.RestaurantDao;
import org.foodapp.dao.UserDao;
import org.foodapp.dto.RestaurantResponse;
import org.foodapp.model.Restaurant;
import org.foodapp.model.Role;
import org.foodapp.model.User;
import org.foodapp.util.JwtUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class FavoriteHandler implements HttpHandler {

    private final UserDao userDao = new UserDao();
    private final RestaurantDao restaurantDao = new RestaurantDao();
    private static final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        User user = authenticate(exchange);
        if (user == null) {
            sendJson(exchange, 401,  "Unauthorized");
            return;
        }

        if (path.equals("/favorites") && method.equals("GET")) {
            handleGetFavorites(exchange, user);
        } else if (path.matches("/favorites/\\d+")) {
            long restaurantId = Long.parseLong(path.substring(path.lastIndexOf("/") + 1));

            if (method.equals("PUT")) {
                handleAddFavorite(exchange, user, restaurantId);
            } else if (method.equals("DELETE")) {
                handleRemoveFavorite(exchange, user, restaurantId);
            } else {
                sendJson(exchange, 405, "Method Not Allowed");
            }
        } else {
            sendJson(exchange, 404,"Path not found");
        }
    }





    private void handleGetFavorites(HttpExchange exchange, User user) throws IOException {
        try {

            List<Restaurant> favorites = user.getFavorites();
            if (favorites == null || favorites.isEmpty()) {
                sendJson(exchange, 404, "{\"error\": \"No favorite restaurants found\"}");
                return;
            }

            List<RestaurantResponse> dtoList = favorites.stream()
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

            sendJson(exchange, 200, gson.toJson(dtoList));
        }
        catch (Exception e) {
            sendJson(exchange, 500, "Internal Server Error");
        }
    }





    private void handleAddFavorite(HttpExchange exchange, User user, long restaurantId) throws IOException {
        try {
            Restaurant restaurant = restaurantDao.findById(restaurantId);
            if (user.getRole() != Role.BUYER) {
                sendJson(exchange, 403, "{\"error\": \"Only buyers can add favorites\"}");
                return;
            }

            if (restaurant == null) {
                sendJson(exchange, 404, "Restaurant not found");
                return;
            }
            List<Restaurant> favorites = user.getFavorites();
            if (favorites.contains(restaurant)) {
                sendJson(exchange, 409, "{\"error\": \"Already in favorites\"}");
                return;
            }
            favorites.add(restaurant);
            userDao.update(user);
            sendJson(exchange, 200, "Added to favorites");
        }
        catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, "Internal Server Error");
        }
    }





    private void handleRemoveFavorite(HttpExchange exchange, User user, long restaurantId) throws IOException {
        try {
            Restaurant restaurant = restaurantDao.findById(restaurantId);
            if (user.getRole() != Role.BUYER) {
                sendJson(exchange, 403, "{\"error\": \"Only buyers can remove favorites\"}");
                return;
            }
            if (restaurant == null) {
                sendJson(exchange, 404, "Restaurant not found");
                return;
            }
            List<Restaurant> favorites = user.getFavorites();
            if (!favorites.contains(restaurant)) {
                sendJson(exchange, 404, "{\"error\": \"Restaurant is not in favorites\"}");
                return;
            }
            favorites.remove(restaurant);
            userDao.update(user);
            sendJson(exchange, 200, "Removed from favorites");
        }
        catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, "Internal Server Error");
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






    public static void sendJson(HttpExchange exchange, int statusCode, Object data) throws IOException {
        String json = gson.toJson(data);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, json.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(json.getBytes());
        }
    }



}
