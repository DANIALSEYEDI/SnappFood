package org.foodapp.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.foodapp.dao.*;
import org.foodapp.dto.Request.OrderItemRequest;
import org.foodapp.dto.Request.OrderSubmitRequest;
import org.foodapp.dto.Response.OrderResponse;
import org.foodapp.model.*;
import org.foodapp.util.JwtUtil;
import org.foodapp.util.QueryUtil;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class OrderHandler implements HttpHandler {

    private final OrderDao orderDao = new OrderDao();
    private final FoodItemDao foodItemDao = new FoodItemDao();
    private final RestaurantDao restaurantDao = new RestaurantDao();
    private final UserDao userDao = new UserDao();
    private final Gson gson = new Gson();
    private final CouponDao couponDao = new CouponDao();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        if (path.equals("/orders") && method.equals("POST")) {
            handleSubmitOrder(exchange);
        } else if (path.matches("/orders/\\d+") && method.equals("GET")) {
            long id = extractId(path, "/orders/");
            handleGetOrder(exchange, id);
        } else if (path.equals("/orders/history") && method.equals("GET")) {
            handleGetOrderHistory(exchange);
        } else {
            sendJson(exchange, 404, "{\"error\": \"not_found\"}");
        }
    }




    private void handleSubmitOrder(HttpExchange exchange) throws IOException {
        try {
            User user = authenticate(exchange);
            if (user == null) return;
            if (user.getRole() != Role.BUYER) {
                sendJson(exchange, 403, "{\"error\": \"forbidden\"}");
                return;
            }

            String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
            if (contentType == null || !contentType.contains("application/json")) {
                sendJson(exchange, 415, "{\"error\": \"Unsupported Media Type\"}");
                return;
            }

            OrderSubmitRequest req = gson.fromJson(new InputStreamReader(exchange.getRequestBody()), OrderSubmitRequest.class);
            if (req.vendor_id == null || req.delivery_address == null || req.items == null || req.items.isEmpty()) {
                sendJson(exchange, 400, "{\"error\": \"invalid_input\"}");
                return;
            }

            Restaurant vendor = restaurantDao.findById(req.vendor_id);
            if (vendor == null) {
                sendJson(exchange, 404, "{\"error\": \"not_found vendor\"}");
                return;
            }


            Coupon coupon = null;
            if (req.coupon_id != null) {
                coupon = couponDao.findById(req.coupon_id);
                if (coupon == null) {
                    sendJson(exchange, 404, Map.of("error", "not_found coupon"));
                    return;
                }
                LocalDate now = LocalDate.now();
                if (now.isBefore(coupon.getStartDate()) || now.isAfter(coupon.getEndDate())) {
                    sendJson(exchange, 400, Map.of("error", "Coupon expired or not active"));
                    return;
                }
                int used = orderDao.countOrdersByCoupon(coupon);
                if (used >= coupon.getUserCount()) {
                    sendJson(exchange, 400, Map.of("error", "Coupon usage limit reached"));
                    return;
                }
            }

            Order order = new Order();
            order.setRestaurant(vendor);
            order.setDeliveryAddress(req.delivery_address);
            order.setStatus(OrderStatus.UNPAID_AND_CANCELLED);
            order.setUser(user);
            order.setAdditionalFee(vendor.getAdditionalFee());
            order.setTaxFee(vendor.getTaxFee());
            order.setRestaurantStatus(OrderRestaurantStatus.PENDING);
            order.setDeliveryStatus(OrderDeliveryStatus.PENDING);
            order.setCreatedAt(LocalDateTime.now());
            order.setUpdatedAt(LocalDateTime.now());

            order.setItemsOfOrder(new ArrayList<>());

            int rawPrice = 0;

            for (OrderItemRequest itemReq : req.items) {
                FoodItem foodItem = foodItemDao.findById(itemReq.item_id);
                if (foodItem == null || !foodItem.getRestaurant().getId().equals(vendor.getId())) {
                    continue;
                }

                if (itemReq.quantity <= 0 || itemReq.quantity > foodItem.getSupply()) {
                    continue;
                }
                foodItem.setSupply(foodItem.getSupply() - itemReq.quantity);
                foodItemDao.update(foodItem);
                OrderItem orderItem = new OrderItem();
                orderItem.setItem(foodItem);
                orderItem.setQuantity(itemReq.quantity);
                orderItem.setOrder(order);
                order.getItemsOfOrder().add(orderItem);
                rawPrice += foodItem.getPrice() * itemReq.quantity;
            }
            int courierPrice = (int) (rawPrice*0.1);
            int payprice=rawPrice+ vendor.getAdditionalFee()+vendor.getTaxFee()+courierPrice;
            if (order.getItemsOfOrder().isEmpty()) {
                sendJson(exchange, 400, Map.of("error", "No valid items to submit in order"));
                return;
            }
            if (coupon != null && payprice < coupon.getMinPrice()) {
                sendJson(exchange, 400, Map.of("error", "Order price below coupon minimum"));
                return;
            }
            order.setRawPrice(rawPrice);
            order.setCoupon(coupon);
            order.setCourierFee((courierPrice));
            int discount=0;
            if (coupon != null) {
                if (coupon.getType() == CouponType.FIXED) {
                    discount = coupon.getValue().intValue();
                } else if (coupon.getType() == CouponType.PERCENT) {
                    discount = rawPrice * coupon.getValue().intValue() / 100;
                }
            }

            order.setPayPrice(Math.max(0, payprice - discount));



            orderDao.save(order);
            sendJson(exchange, 200, OrderResponse.fromEntity(order));
        }
        catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, "{\"error\": \"internal_server_error\"}");
        }
    }


    private void handleGetOrder(HttpExchange exchange, long id) throws IOException {
        try {
            User user = authenticate(exchange);
            if (user == null) return;
            if (user.getRole() != Role.BUYER) {
                sendJson(exchange, 403, "{\"error\": \"forbidden\"}");
                return;
            }
            Order order = orderDao.findById(id);
            if (order == null) {
                sendJson(exchange, 404, "{\"error\": \"not_found order\"}");
                return;
            }
            if (!order.getUser().getId().equals(user.getId())) {
                sendJson(exchange, 403, "{\"error\": \"forbidden\"}");
                return;
            }
            sendJson(exchange, 200, OrderResponse.fromEntity(order));
        }
        catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, "{\"error\": \"internal_server_error\"}");
        }
    }




    private void handleGetOrderHistory(HttpExchange exchange) throws IOException {
        try {
            User user = authenticate(exchange);
            if (user == null) return;
            if (user.getRole() != Role.BUYER) {
                sendJson(exchange, 403, "{\"error\": \"forbidden\"}");
                return;
            }
            Map<String, String> queryParams = QueryUtil.getQueryParams(exchange.getRequestURI().getRawQuery());
            String search = queryParams.get("search");
            String vendor = queryParams.get("vendor");
            List<Order> orders = orderDao.findHistoryForUser(user.getId(), search, vendor);
            List<OrderResponse> response = orders.stream()
                    .map(OrderResponse::fromEntity)
                    .toList();

            sendJson(exchange, 200, response);
        }
        catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, "{\"error\": \"internal_server_error\"}");
        }
    }


    private long extractId(String path, String prefix) {
        try {
            return Long.parseLong(path.substring(prefix.length()));
        } catch (NumberFormatException e) {
            return -1;
        }
    }


    private void sendJson(HttpExchange exchange, int statusCode, Object data) throws IOException {
        String response = gson.toJson(data);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        exchange.getResponseBody().write(response.getBytes());
        exchange.getResponseBody().close();
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
}
