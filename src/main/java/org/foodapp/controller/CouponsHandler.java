package org.foodapp.controller;


import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.foodapp.dao.CouponDao;
import org.foodapp.dao.UserDao;
import org.foodapp.dto.CouponResponse;
import org.foodapp.model.Coupon;
import org.foodapp.model.User;
import org.foodapp.util.JwtUtil;
import org.foodapp.util.QueryUtil;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class CouponsHandler implements HttpHandler {

    private final CouponDao couponDao = new CouponDao();
    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            User user = authenticate(exchange);
            if (user == null) {
                sendJson(exchange, 401, "{\"error\": \"Unauthorized\"}");
                return;
            }

            String query = exchange.getRequestURI().getQuery();
            Map<String, String> queryParams = QueryUtil.getQueryParams(query);
            String code = queryParams.get("coupon_code");

            if (code == null || code.trim().isEmpty()) {
                sendJson(exchange, 400, "{\"error\": \"Missing coupon_code\"}");
                return;
            }

            Coupon coupon = couponDao.findByCode(code.trim());
            if (coupon == null) {
                sendJson(exchange, 404, "{\"error\": \"Coupon not found\"}");
                return;
            }
            CouponResponse response = CouponResponse.fromEntity(coupon);
            sendJson(exchange, 200, response);
        }
        catch (Exception e){
            e.printStackTrace();
            sendJson(exchange, 500, "{\"error\": \"Internal Server Error\"}");
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

        return new UserDao().findById(Long.parseLong(decoded.getSubject()));
    }

}

