package org.foodapp.controller;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.foodapp.dao.*;
import org.foodapp.dto.*;
import org.foodapp.model.*;
import org.foodapp.util.JwtUtil;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.net.URLDecoder;
import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.foodapp.util.QueryUtil;


public class RestaurantHandler implements HttpHandler {

    private final Gson gson = new Gson();
    private final RestaurantDao restaurantDao = new RestaurantDao();
    private final FoodItemDao foodItemDao = new FoodItemDao();
    private final MenuDao menuDao = new MenuDao();
    private final OrderDao orderDao = new OrderDao();

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
            }else if (path.matches("/restaurants/\\d+/menu") && method.equals("POST")) {
                long id = extractId(path, "/restaurants/", "/menu");
                handleCreateMenu(exchange, id);
            } else if (path.matches("/restaurants/\\d+/menu/[^/]+") && method.equals("DELETE")) {
                long id = extractId(path, "/restaurants/", "/menu/");
                String title = path.substring(path.lastIndexOf("/") + 1);
                handleDeleteMenu(exchange, id, title);
            } else if (path.matches("/restaurants/\\d+/menu/[^/]+") && method.equals("PUT")) {
                long id = extractId(path, "/restaurants/", "/menu/");
                String title = path.substring(path.lastIndexOf("/") + 1);
                handleAddItemToMenu(exchange, id, title);
            } else if (path.matches("/restaurants/\\d+/menu/[^/]+/\\d+") && method.equals("DELETE")) {
                String[] parts = path.split("/");
                long id = Long.parseLong(parts[2]);
                String title = parts[4];
                long itemId = Long.parseLong(parts[5]);
                handleRemoveItemFromMenu(exchange, id, title, itemId);
            }else if (path.matches("/restaurants/\\d+/orders") && method.equalsIgnoreCase("GET")) {
                long id = extractId(path, "/restaurants/", "/orders");
                handleGetOrdersForRestaurant(exchange, id);
            } else if (path.matches("/restaurants/orders/\\d+") && method.equalsIgnoreCase("PATCH")) {
                long oid = extractId(path, "/restaurants/orders/", "");
                handleUpdateOrderStatus(exchange, oid);
            }
            else {
                sendJson(exchange, 404, "{\"error\": \"Not found\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, "{\"error\": \"Internal server error\"}");
        }
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;

        for (String pair : query.split("&")) {
            String[] parts = pair.split("=");
            if (parts.length == 2) {
                String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
                String value = URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
                map.put(key, value);
            }
        }
        return map;
    }

    private long extractId(String path, String prefix, String suffix) {
        try {
            if (!path.startsWith(prefix)) {
                throw new IllegalArgumentException("Path does not start with expected prefix");
            }

            String temp = path.substring(prefix.length());

            if (suffix != null && !suffix.isEmpty()) {
                int endIndex = temp.indexOf(suffix);
                if (endIndex != -1) {
                    temp = temp.substring(0, endIndex);
                }
            }

            return Long.parseLong(temp);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid path format for extracting ID: " + path);
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
            RestaurantCreateRequest request = gson.fromJson(
                    new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8),
                    RestaurantCreateRequest.class
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
            RestaurantCreateRequest request = gson.fromJson(
                    new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8),
                    RestaurantCreateRequest.class
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
        try {
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

            RestaurantFoodItemRequest request = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), RestaurantFoodItemRequest.class);
            if (request.name == null || request.description == null || request.price == null || request.supply == null || request.keywords == null || request.keywords.isEmpty()) {
                sendJson(exchange, 400, "{\"error\": \"Missing required fields\"}");
                return;
            }

            FoodItem item = new FoodItem(
                    request.name, request.imageBase64, request.description,
                    request.price, request.supply, request.keywords, restaurant
            );
            new FoodItemDao().save(item);
            FoodItemResponse response = new FoodItemResponse(item);
            sendJson(exchange, 200, gson.toJson(response));
        }
        catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, "{\"error\": \"Internal server error\"}");
        }
    }


    private void handleEditItem(HttpExchange exchange) throws IOException {
        try {
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

            RestaurantFoodItemRequest request = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), RestaurantFoodItemRequest.class);
            if (request.name != null) item.setName(request.name);
            if (request.imageBase64 != null) item.setImageBase64(request.imageBase64);
            if (request.description != null) item.setDescription(request.description);
            if (request.price != null) item.setPrice(request.price);
            if (request.supply != null) item.setSupply(request.supply);
            if (request.keywords != null && !request.keywords.isEmpty()) item.setKeywords(request.keywords);

            new FoodItemDao().update(item);
            FoodItemResponse response = new FoodItemResponse(item);
            sendJson(exchange, 200, gson.toJson(response));
        }
        catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, "{\"error\": \"Internal server error\"}");
        }
    }



    private void handleDeleteItem(HttpExchange exchange) throws IOException {
        try {
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
        catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, "{\"error\": \"Internal server error\"}");
        }
    }




    private long extractIdFromPath(String path, String prefix, String suffix) {
        return Long.parseLong(path.replace(prefix, "").replace(suffix, "").split("/")[0]);
    }




    private void handleCreateMenu(HttpExchange exchange, long restaurantId) throws IOException {
        try {
            User user = authenticate(exchange);
            if (user == null) {
                sendJson(exchange, 403, "{\"error\": \"Unauthorized\"}");
                return;
            }

            Restaurant restaurant = restaurantDao.findById(restaurantId);
            if (restaurant == null) {
                sendJson(exchange, 404, "{\"error\": \"Restaurant not found\"}");
                return;
            }

            if (!restaurant.getSeller().getId().equals(user.getId())) {
                sendJson(exchange, 403, "{\"error\": \"Access denied\"}");
                return;
            }

            RestaurantMenuRequest request = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), RestaurantMenuRequest.class);
            if (request.title == null) {
                sendJson(exchange, 400, "{\"error\": \"Menu title is required\"}");
                return;
            }

            Menu menu = new Menu();
            menu.setTitle(request.title);
            menu.setRestaurant(restaurant);

            restaurant.getMenus().add(menu);
            restaurantDao.update(restaurant);
            menuDao.save(menu);
            sendJson(exchange, 200, gson.toJson(Map.of("title", menu.getTitle())));
        }
        catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, "{\"error\": \"Internal server error\"}");
        }
    }



    private void handleDeleteMenu(HttpExchange exchange, long restaurantId, String title) throws IOException {
        try {
            User user = authenticate(exchange);
            if (user == null) {
                sendJson(exchange, 403, "{\"error\": \"Unauthorized\"}");
                return;}

            Restaurant restaurant = restaurantDao.findById(restaurantId);
            if (restaurant == null) {
                sendJson(exchange, 404, "{\"error\": \"Restaurant not found\"}");
                return;
            }

            Menu toRemove = restaurant.getMenus().stream()
                    .filter(menu -> menu.getTitle().equalsIgnoreCase(title))
                    .findFirst()
                    .orElse(null);

            if (toRemove == null) {
                sendJson(exchange, 404, "{\"error\": \"Menu not found\"}");
                return;
            }

            restaurant.getMenus().remove(toRemove);
            restaurantDao.update(restaurant);
            sendJson(exchange, 200, "{\"message\": \"Food menu removed from restaurant successfully\"}");
        }
        catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, "{\"error\": \"Internal server error\"}");
        }
    }




    private void handleAddItemToMenu(HttpExchange exchange, long restaurantId, String title) throws IOException {
        try {
            User user = authenticate(exchange);
            if (user == null) {
                sendJson(exchange, 403, "{\"error\": \"Unauthorized\"}");
                return;
            }

            Restaurant restaurant = restaurantDao.findById(restaurantId);
            if (restaurant == null) {
                sendJson(exchange, 404, "{\"error\": \"Restaurant not found\"}");
                return;
            }

            if (!restaurant.getSeller().getId().equals(user.getId())) {
                sendJson(exchange, 403, "{\"error\": \"Access denied\"}");
                return;
            }

            Menu menu = menuDao.findByRestaurantAndTitleWithItems(restaurantId, title);
            if (menu == null) {
                sendJson(exchange, 404, "{\"error\": \"Menu not found\"}");
                return;
            }

            RestaurantsAddItemToMenuRequest request = gson.fromJson(
                    new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8),
                    RestaurantsAddItemToMenuRequest.class
            );

            FoodItem item = foodItemDao.findById(request.item_id);
            if (item == null) {
                sendJson(exchange, 404, "{\"error\": \"Food item not found\"}");
                return;
            }

            menu.addItem(item);
            menuDao.update(menu);

            sendJson(exchange, 200, "{\"message\": \"Food item added to menu successfully\"}");
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, "{\"error\": \"Internal server error\"}");
        }
    }



    private void handleRemoveItemFromMenu(HttpExchange exchange, long restaurantId, String title, long itemId) throws IOException {
        try {
            User user = authenticate(exchange);
            if (user == null) {
                sendJson(exchange, 403, "{\"error\": \"Unauthorized\"}");
                return;
            }

            Restaurant restaurant = restaurantDao.findById(restaurantId);
            if (restaurant == null) {
                sendJson(exchange, 404, "{\"error\": \"Restaurant not found\"}");
                return;
            }

            Menu menu = menuDao.findByRestaurantAndTitleWithItems(restaurantId, title);
            if (menu == null) {
                sendJson(exchange, 404, "{\"error\": \"Menu not found\"}");
                return;
            }

            FoodItem item = foodItemDao.findById(itemId);
            boolean found = menu.getItems().stream()
                    .anyMatch(i -> i.getId().equals(item.getId()));

            if (item == null || !found) {
                sendJson(exchange, 404, "{\"error\": \"Item not found in this menu\"}");
                return;
            }

            menu.getItems().remove(item);
            menuDao.update(menu);

            sendJson(exchange, 200, "{\"message\": \"Item removed from menu successfully\"}");
        }
        catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, "{\"error\": \"Internal server error\"}");
        }
    }




    private void handleGetOrdersForRestaurant(HttpExchange exchange, long restaurantId) throws IOException {
        try {
            User user = authenticate(exchange);
            if (user == null) {
                sendJson(exchange, 401, "{\"error\": \"Unauthorized\"}");
                return;
            }

            if (user.getRole() != Role.SELLER) {
                sendJson(exchange, 403, "{\"error\": \"Only sellers can view their orders\"}");
                return;
            }

            Restaurant restaurant = restaurantDao.findById(restaurantId);
            if (restaurant == null || !restaurant.getSeller().getId().equals(user.getId())) {
                sendJson(exchange, 403, "{\"error\": \"Access denied\"}");
                return;
            }

            Map<String, String> queryParams = QueryUtil.getQueryParams(exchange.getRequestURI().getRawQuery());
            String status = queryParams.get("status");
            String search = queryParams.get("search");
            String userParam = queryParams.get("user");
            String courier = queryParams.get("courier");

            List<Order> orders = orderDao.findByFilters(restaurantId, status, search, userParam, courier);

            List<OrderResponse> response = orders.stream()
                    .map(OrderResponse::fromEntity)
                    .toList();

            sendJson(exchange, 200, new Gson().toJson(response));

        } catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, "{\"error\": \"Internal server error\"}");
        }
    }




    private void handleUpdateOrderStatus(HttpExchange exchange, long orderId) throws IOException {
        try {
            User user = authenticate(exchange);
            if (user == null) {
                sendJson(exchange, 401, "{\"error\": \"Unauthorized\"}");
                return;
            }

            if (user.getRole() != Role.SELLER) {
                sendJson(exchange, 403, "{\"error\": \"Only sellers can change order status\"}");
                return;
            }

            RestaurantUpdateOrderStatusRequest request = new ObjectMapper()
                    .readValue(exchange.getRequestBody(), RestaurantUpdateOrderStatusRequest.class);

            if (request.getStatus() == null) {
                sendJson(exchange, 400, "{\"error\": \"Missing status in request body\"}");
                return;
            }

            Order order = orderDao.findById(orderId);
            if (order == null) {
                sendJson(exchange, 404, "{\"error\": \"Order not found\"}");
                return;
            }


            if (!order.getRestaurant().getSeller().getId().equals(user.getId())) {
                sendJson(exchange, 403, "{\"error\": \"Access denied: You cannot change this order\"}");
                return;
            }

            try {
                OrderStatus newStatus = OrderStatus.valueOf(request.getStatus().toUpperCase());
                order.setStatus(newStatus);
                orderDao.update(order);
            } catch (IllegalArgumentException e) {
                sendJson(exchange, 400, "{\"error\": \"Invalid status value\"}");
                return;
            }

            sendJson(exchange, 200, "{\"message\": \"Order status changed successfully\"}");

        } catch (Exception e) {
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
