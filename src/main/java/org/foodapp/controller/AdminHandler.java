package org.foodapp.controller;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.foodapp.dao.*;
import org.foodapp.dto.AdminTransactionResponse;
import org.foodapp.dto.AdminUserResponse;
import org.foodapp.dto.CouponResponse;
import org.foodapp.dto.OrderResponse;
import org.foodapp.model.*;
import org.foodapp.util.JwtUtil;
import org.foodapp.util.QueryUtil;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminHandler implements HttpHandler {

    private final UserDao userDao = new UserDao();
    private final Gson gson = new Gson();
    private final TransactionDao transactionDao = new TransactionDao();
    private final CouponDao couponDao = new CouponDao();
    private final OrderDao orderDao = new OrderDao();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if (method.equalsIgnoreCase("GET") && path.equals("/admin/users")) {
            handleListUsers(exchange);
        } else if (method.equalsIgnoreCase("PATCH") && path.startsWith("/admin/users/") && path.endsWith("/status")) {
            long userId = extractId(path, "/admin/users/", "/status");
            handleUpdateUserStatus(exchange, userId);
        }else if (method.equals("GET") && path.equals("/admin/transactions")) {
            handleGetTransactions(exchange);
        }
        else if (path.equals("/admin/coupons") && method.equalsIgnoreCase("POST")) {
            handleCreateCoupon(exchange);
        }
        else if (path.equals("/admin/coupons") && method.equalsIgnoreCase("GET")) {
            handleGetListCoupons(exchange);
        }
        else if (method.equalsIgnoreCase("GET") && path.matches("^/admin/coupons/\\d+$")) {
            long id = extractId2(path, "/admin/coupons/");
            handleGetCoupon(exchange, id);
        }
        else if (method.equalsIgnoreCase("DELETE") && path.matches("^/admin/coupons/\\d+$")) {
            long id = extractId2(path, "/admin/coupons/");
            handleDeleteCoupon(exchange, id);
        }
        else if (method.equalsIgnoreCase("PUT") && path.matches("^/admin/coupons/\\d+$")) {
            long id = extractId2(path, "/admin/coupons/");
            handleUpdateCoupon(exchange, id);
        }
        else if (method.equals("GET") && path.equals("/admin/orders")) {
            handleGetAdminOrders(exchange);
        }

        else {
            sendJson(exchange, 404, "{\"error\": \"Path Not found\"}");
        }
    }







    private void handleListUsers(HttpExchange exchange) throws IOException {
        try {
            Admin admin = authenticateAdmin(exchange);
            if (admin == null) return;
            List<User> users = userDao.findAll();
            List<AdminUserResponse> response = users.stream()
                    .map(AdminUserResponse::fromEntity)
                    .collect(Collectors.toList());
            if (response.isEmpty()) {
                sendJson(exchange, 404, "{\"error\": \"No users found\"}");
                return;
            }
            sendJson(exchange, 200, response);
        }
        catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, "{\"error\": \"Internal server error\"}");
        }

    }







    private void handleUpdateUserStatus(HttpExchange exchange, long userId) throws IOException {
        try {
            Admin admin = authenticateAdmin(exchange);
            if (admin == null) return;

            String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
            if (contentType == null || !contentType.contains("application/json")) {
                sendJson(exchange, 415, Map.of("error", "Unsupported Media Type"));
                return;
            }

            Map<String, String> body = gson.fromJson(
                    new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8),
                    new TypeToken<Map<String, String>>() {
                    }.getType()
            );

            String statusStr = body.get("status");
            if (statusStr == null) {
                sendJson(exchange, 400, Map.of("error", "Missing status"));
                return;
            }

            UserStatus status;
            try {
                status = UserStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                sendJson(exchange, 400, Map.of("error", "Invalid status"));
                return;
            }
            User user = userDao.findById(userId);
            if (user == null) {
                sendJson(exchange, 404, Map.of("error", "User not found"));
                return;
            }
            user.setStatus(status);
            userDao.update(user);
            sendJson(exchange, 200, Map.of("message", "User status updated"));
        }
        catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, "{\"error\": \"Internal server error\"}");
        }
    }







    private void handleGetTransactions(HttpExchange exchange) throws IOException {
        try {

            Admin admin = authenticateAdmin(exchange);
            if (admin == null) return;

            Map<String, String> queryParams = QueryUtil.getQueryParams(exchange.getRequestURI().getQuery());
            List<Transaction> transactions = transactionDao.findByFilters(queryParams);
            List<AdminTransactionResponse> response = transactions.stream()
                    .map(AdminTransactionResponse::fromEntity)
                    .collect(Collectors.toList());
            if (response.isEmpty()) {
                sendJson(exchange, 404, Map.of("error", "No transactions"));
                return;
            }

            sendJson(exchange, 200, response);
        }
        catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, "{\"error\": \"Internal server error\"}");
        }
    }







    private void handleCreateCoupon(HttpExchange exchange) throws IOException {
        Admin admin = authenticateAdmin(exchange);
        if (admin == null) return;

        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null || !contentType.contains("application/json")) {
            sendJson(exchange, 415, Map.of("error", "Unsupported Media Type"));
            return;
        }

        try {
            Map<String, Object> body = gson.fromJson(
                    new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8),
                    Map.class
            );

            String code = (String) body.get("coupon_code");
            String typeStr = (String) body.get("type");
            Number valueNum = (Number) body.get("value");
            Number minPriceNum = (Number) body.get("min_price");
            Number userCountNum = (Number) body.get("user_count");
            String startDateStr = (String) body.get("start_date");
            String endDateStr = (String) body.get("end_date");

            if (code == null || typeStr == null || valueNum == null || minPriceNum == null ||
                    userCountNum == null || startDateStr == null || endDateStr == null) {
                sendJson(exchange, 400, Map.of("error", "Missing required fields"));
                return;
            }


            CouponType type;
            try {
                type = CouponType.valueOf(typeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                sendJson(exchange, 400, Map.of("error", "Invalid coupon type"));
                return;
            }


            if (couponDao.findByCode(code) != null) {
                sendJson(exchange, 409, Map.of("error", "Coupon code already exists"));
                return;
            }
            Coupon coupon = new Coupon();
            coupon.setCouponCode(code);
            coupon.setType(type);
            coupon.setValue(BigDecimal.valueOf(valueNum.doubleValue()));
            coupon.setMinPrice(minPriceNum.intValue());
            coupon.setUserCount(userCountNum.intValue());
            coupon.setStartDate(LocalDate.parse(startDateStr));
            coupon.setEndDate(LocalDate.parse(endDateStr));

            couponDao.save(coupon);

            sendJson(exchange, 201, Map.of("message", "Coupon created"));
        } catch (Exception e) {
            e.printStackTrace();
           sendJson(exchange, 500, "{\"error\": \"Internal server error\"}");
        }
    }







    private void handleGetListCoupons(HttpExchange exchange) throws IOException {
        try {
            Admin admin = authenticateAdmin(exchange);
            if (admin == null) return;

            List<Coupon> coupons = couponDao.findAll();
            List<CouponResponse> result = coupons.stream()
                    .map(CouponResponse::fromEntity)
                    .toList();
            if (result.isEmpty()) {
                sendJson(exchange, 404, Map.of("error", "No coupons"));
                return;
            }
            sendJson(exchange, 200, result);
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, Map.of("error", "Internal server error"));
        }
    }







    private void handleGetCoupon(HttpExchange exchange, long id) throws IOException {
        try {
            Admin admin = authenticateAdmin(exchange);
            if (admin == null) return;

            Coupon coupon = couponDao.findById(id);
            if (coupon == null) {
                sendJson(exchange, 404, Map.of("error", "Coupon not found"));
                return;
            }

            CouponResponse response = CouponResponse.fromEntity(coupon);
            sendJson(exchange, 200, response);
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, Map.of("error", "Internal server error"));
        }
    }



    private void handleDeleteCoupon(HttpExchange exchange, long id) throws IOException {
        try {
            Admin admin = authenticateAdmin(exchange);
            if (admin == null) return;

            Coupon coupon = couponDao.findById(id);
            if (coupon == null) {
                sendJson(exchange, 404, Map.of("error", "Coupon not found"));
                return;
            }

            couponDao.delete(coupon);
            sendJson(exchange, 200, Map.of("message", "Coupon deleted"));
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, Map.of("error", "Internal Server Error"));
        }
    }


    private void handleUpdateCoupon(HttpExchange exchange, long id) throws IOException {
        try {
            Admin admin = authenticateAdmin(exchange);
            if (admin == null) return;

            if (!"application/json".equalsIgnoreCase(exchange.getRequestHeaders().getFirst("Content-Type"))) {
                sendJson(exchange, 415, Map.of("error", "Unsupported Media Type"));
                return;
            }

            Coupon coupon = couponDao.findById(id);
            if (coupon == null) {
                sendJson(exchange, 404, Map.of("error", "Coupon not found"));
                return;
            }

            Map<String, Object> body = gson.fromJson(new InputStreamReader(exchange.getRequestBody()), Map.class);

            if (body.containsKey("coupon_code")) {
                coupon.setCouponCode(body.get("coupon_code").toString());
            }
            if (body.containsKey("type")) {
                try {
                    coupon.setType(CouponType.valueOf(body.get("type").toString().toUpperCase()));
                } catch (IllegalArgumentException e) {
                    sendJson(exchange, 400, Map.of("error", "Invalid coupon type"));
                    return;
                }
            }
            if (body.containsKey("value")) {
                try {
                    coupon.setValue(new BigDecimal(body.get("value").toString()));
                } catch (NumberFormatException e) {
                    sendJson(exchange, 400, Map.of("error", "Invalid value format"));
                    return;
                }
            }

            if (body.containsKey("min_price")) {
                try {
                    BigDecimal decimal = new BigDecimal(body.get("min_price").toString());
                    coupon.setMinPrice(decimal.intValueExact());
                } catch (ArithmeticException | NumberFormatException e) {
                    sendJson(exchange, 400, Map.of("error", "Invalid min_price format: must be a whole number"));
                    return;
                }
            }
            Object countObj = body.get("user_count");
            if (countObj instanceof Number) {
                coupon.setUserCount(((Number) countObj).intValue());
            } else {
                sendJson(exchange, 400, Map.of("error", "Invalid user_count"));
                return;
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            if (body.containsKey("start_date")) {
                try {
                    String start = body.get("start_date").toString();
                    coupon.setStartDate(LocalDate.parse(start, formatter));
                } catch (DateTimeParseException e) {
                    sendJson(exchange, 400, Map.of("error", "Invalid start_date format. Expected yyyy-MM-dd"));
                    return;
                }
            }

            if (body.containsKey("end_date")) {
                try {
                    String end = body.get("end_date").toString();
                    coupon.setEndDate(LocalDate.parse(end, formatter));
                } catch (DateTimeParseException e) {
                    sendJson(exchange, 400, Map.of("error", "Invalid end_date format. Expected yyyy-MM-dd"));
                    return;
                }
            }


            couponDao.update(coupon);
            sendJson(exchange, 200, Map.of("message", "Coupon updated"));


        } catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, Map.of("error", "Internal Server Error"));
        }
    }



    private void handleGetAdminOrders(HttpExchange exchange) throws IOException {
        try {
            Admin admin = authenticateAdmin(exchange);
            if (admin == null) return;

            Map<String, String> params = QueryUtil.getQueryParams(exchange.getRequestURI().getQuery());

            List<Order> orders = orderDao.findByAdminFilters(params);
            List<OrderResponse> response = orders.stream()
                    .map(OrderResponse::fromEntity)
                    .collect(Collectors.toList());
            if (response.isEmpty()) {
                sendJson(exchange, 404, Map.of("error", "Order not found"));
                return;
            }
            sendJson(exchange, 200, response);
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, Map.of("error", "Internal server error"));
        }
    }











    private long extractId2(String path, String prefix) {
        try {
            return Long.parseLong(path.substring(prefix.length()));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid path format for extracting ID");
        }
    }







    private long extractId(String path, String prefix, String suffix) {
        try {
            String temp = path.substring(prefix.length());
            int endIndex = temp.indexOf(suffix);
            if (endIndex == -1) return -1;
            return Long.parseLong(temp.substring(0, endIndex));
        } catch (Exception e) {
            return -1;
        }
    }







    private Admin authenticateAdmin(HttpExchange exchange) throws IOException {
        List<String> authHeaders = exchange.getRequestHeaders().get("Authorization");
        if (authHeaders == null || authHeaders.isEmpty()) {
            sendJson(exchange, 401, Map.of("error", "Missing Authorization header"));
            return null;
        }

        String token = authHeaders.get(0).replace("Bearer ", "");
        try {
            var decoded = JwtUtil.verifyToken(token);
            String role = decoded.getClaim("role").asString();
            if (!"ADMIN".equals(role)) {
                sendJson(exchange, 403, Map.of("error", "Forbidden"));
                return null;
            }
            long adminId = Long.parseLong(decoded.getSubject());
            return new CreateAdminDao().findById(adminId);
        } catch (Exception e) {
            sendJson(exchange, 401, Map.of("error", "Invalid token"));
            return null;
        }
    }



    private void sendJson(HttpExchange exchange, int status, Object data) throws IOException {
        String json = gson.toJson(data);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, json.getBytes().length);
        exchange.getResponseBody().write(json.getBytes());
        exchange.getResponseBody().close();
    }
}
