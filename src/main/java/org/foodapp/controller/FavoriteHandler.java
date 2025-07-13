package org.foodapp.controller;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.foodapp.dao.RestaurantDao;
import org.foodapp.dao.UserDao;
import org.foodapp.model.Restaurant;
import org.foodapp.model.User;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class FavoriteHandler implements HttpHandler {

    private final UserDao userDao = new UserDao();
    private final RestaurantDao restaurantDao = new RestaurantDao();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        User user = getAuthenticatedUser(exchange);
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
        List<Restaurant> favorites = user.getFavorites();
        sendJson(exchange, 200, favorites);
    }

    private void handleAddFavorite(HttpExchange exchange, User user, long restaurantId) throws IOException {
        Restaurant restaurant = restaurantDao.findById(restaurantId);
        if (restaurant == null) {
            sendJson(exchange, 404,"Restaurant not found");
            return;
        }

        if (!user.getFavorites().contains(restaurant)) {
            user.getFavorites().add(restaurant);
            userDao.update(user);
        }
        sendJson(exchange, 200, "Added to favorites");
    }

    private void handleRemoveFavorite(HttpExchange exchange, User user, long restaurantId) throws IOException {
        Restaurant restaurant = restaurantDao.findById(restaurantId);
        if (restaurant == null) {
            sendJson(exchange, 404,"Restaurant not found");
            return;
        }

        if (user.getFavorites().contains(restaurant)) {
            user.getFavorites().remove(restaurant);
            userDao.update(user);
        }

        sendJson(exchange, 200, "Removed from favorites");
    }

    private User getAuthenticatedUser(HttpExchange exchange) {
        // به صورت تستی یا با توکن از header
        return userDao.findById(1L); // فرضی
    }

    private static final Gson gson = new Gson();
    public static void sendJson(HttpExchange exchange, int statusCode, Object data) throws IOException {
        String json = gson.toJson(data);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, json.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(json.getBytes());
        }
    }
}
