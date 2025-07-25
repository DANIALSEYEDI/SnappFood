package org.foodapp.controller;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.foodapp.dao.RestaurantDao;
import org.foodapp.dao.UserDao;
import org.foodapp.dto.Request.VendorFilterRequest;
import org.foodapp.dto.Response.FoodItemResponse;
import org.foodapp.dto.Response.RestaurantResponse;
import org.foodapp.dto.Response.VendorSimpleRestaurantDTO;
import org.foodapp.model.*;
import org.foodapp.util.JwtUtil;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class VendorsHandler implements HttpHandler {
    private final RestaurantDao restaurantDao = new RestaurantDao();
    private final Gson gson = new Gson();
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        URI uri = exchange.getRequestURI();
        String path = uri.getPath();
        try {
            if (path.equals("/vendors") && method.equals("POST")) {
                handleSearchVendors(exchange);
            } else if (path.matches("/vendors/\\d+") && method.equals("GET")) {
                long id = extractId(path, "/vendors/", "");
                handleGetVendorDetails(exchange, id);
            } else {
                sendJson(exchange, 404, "{\"error\": \"not_found\"}");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, "{\"error\": \"Internal_server_error\"}");
        }
    }


    private void handleSearchVendors(HttpExchange exchange) throws IOException {
        try {
            User user = authenticate(exchange);
            if (user == null) {
                sendJson(exchange, 401, "{\"error\": \"Unauthorized\"}");
                return;
            }
            if (user.getRole() != Role.BUYER) {
                sendJson(exchange, 403, "{\"error\": \"forbidden\"}");
                return;
            }
            VendorFilterRequest request = gson.fromJson(
                    new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8),
                    VendorFilterRequest.class
            );

            List<Restaurant> vendors = restaurantDao.findByFilters(request.search, request.keywords);

            if (vendors == null || vendors.isEmpty()) {
                sendJson(exchange, 404, "{\"error\": \"not_found vendors\"}");
                return;
            }

            List<RestaurantResponse> responseList = vendors.stream()
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
        }  catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, "{\"error\": \"Internal_server_error\"}");
        }
    }


    private void handleGetVendorDetails(HttpExchange exchange, long vendorId) throws IOException {
        try {
            User user = authenticate(exchange);
            if (user == null) {
                sendJson(exchange, 401, "{\"error\": \"Unauthorized\"}");
                return;
            }
            if (user.getRole() != Role.BUYER) {
                sendJson(exchange, 403, "{\"error\": \"forbidden\"}");
                return;
            }

            Restaurant restaurant = restaurantDao.findWithMenusAndItems(vendorId);
            if (restaurant == null) {
                sendJson(exchange, 404, "{\"error\": \"not_found vendor\"}");
                return;
            }
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("vendor", VendorSimpleRestaurantDTO.from(restaurant));

            List<String> menuTitles = restaurant.getMenus().stream()
                    .map(Menu::getTitle)
                    .toList();
            response.put("menu_titles", menuTitles);
            for (Menu menu : restaurant.getMenus()) {
                String title = menu.getTitle();
                List<FoodItemResponse> items = menu.getItems().stream()
                        .map(FoodItemResponse::new)
                        .toList();
                response.put(title, items);
            }
            sendJson(exchange, 200, gson.toJson(response));
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, "{\"error\": \"Internal_server_error\"}");
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


    private long extractId(String path, String prefix, String suffix) {
        try {
            String temp = path.substring(prefix.length());
            if (!suffix.isEmpty() && temp.contains(suffix)) {
                temp = temp.substring(0, temp.indexOf(suffix));
            }
            return Long.parseLong(temp);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid path format for extracting ID");
        }
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
