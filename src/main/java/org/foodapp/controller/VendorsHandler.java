package org.foodapp.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.foodapp.dao.RestaurantDao;
import org.foodapp.dao.UserDao;
import org.foodapp.dto.FoodItemResponse;
import org.foodapp.dto.VendorSimpleRestaurantDTO;
import org.foodapp.dto.VendorFilterRequest;
import org.foodapp.dto.VendorMenuResponse;
import org.foodapp.model.*;
import org.foodapp.util.JwtUtil;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

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
                sendJson(exchange, 404, "{\"error\": \"Not found\"}");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, "{\"error\": \"Internal server error\"}");
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
                sendJson(exchange, 403, "{\"error\": \"Only buyers can access this endpoint\"}");
                return;
            }

            String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
            if (contentType == null || !contentType.contains("application/json")) {
                sendJson(exchange, 415, "{\"error\": \"Unsupported Media Type\"}");
                return;
            }

            VendorFilterRequest request = gson.fromJson(
                    new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8),
                    VendorFilterRequest.class
            );

            List<Restaurant> vendors = restaurantDao.findByFilters(request.search, request.keywords);

            if (vendors == null || vendors.isEmpty()) {
                sendJson(exchange, 404, "{\"error\": \"No vendors found\"}");
                return;
            }

            List<VendorMenuResponse> responseList = new ArrayList<>();

            for (Restaurant vendor : vendors) {
                VendorMenuResponse dto = new VendorMenuResponse();
                dto.vendorId = vendor.getId();
                dto.vendorName = vendor.getName();
                dto.vendorPhone = vendor.getPhone();
                dto.vendorAddress = vendor.getAddress();

                List<String> titles = new ArrayList<>();
                Map<String, List<FoodItemResponse>> map = new LinkedHashMap<>();

                for (Menu menu : vendor.getMenus()) {
                    titles.add(menu.getTitle());
                    List<FoodItemResponse> items = menu.getItems().stream()
                            .map(FoodItemResponse::new)
                            .collect(Collectors.toList());
                    map.put(menu.getTitle(), items);
                }

                dto.menu_titles = titles;
                dto.menu_items_by_title = map;
                responseList.add(dto);
            }

            sendJson(exchange, 200, gson.toJson(responseList));

        } catch (JsonSyntaxException e) {
            sendJson(exchange, 400, "{\"error\": \"Invalid input format\"}");
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, "{\"error\": \"Internal server error\"}");
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
                sendJson(exchange, 403, "{\"error\": \"Only buyers can view vendors\"}");
                return;
            }

            Restaurant restaurant = restaurantDao.findWithMenusAndItems(vendorId);
            if (restaurant == null) {
                sendJson(exchange, 404, "{\"error\": \"Vendor not found\"}");
                return;
            }

            VendorMenuResponse dto = new VendorMenuResponse();
            dto.vendor = VendorSimpleRestaurantDTO.from(restaurant);

            List<String> menuTitles = new ArrayList<>();
            Map<String, List<FoodItemResponse>> menuItemsMap = new LinkedHashMap<>();

            for (Menu menu : restaurant.getMenus()) {
                menuTitles.add(menu.getTitle());

                List<FoodItemResponse> itemResponses = menu.getItems().stream()
                        .map(FoodItemResponse::new)
                        .collect(Collectors.toList());

                menuItemsMap.put(menu.getTitle(), itemResponses);
            }

            dto.menu_titles = menuTitles;
            dto.menu_items_by_title = menuItemsMap;

            sendJson(exchange, 200, gson.toJson(dto));

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
