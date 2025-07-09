package org.foodapp.controller;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.foodapp.dao.UserDao;
import org.foodapp.dto.*;
import org.foodapp.model.*;
import org.foodapp.util.JwtUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class AuthHandler implements HttpHandler {

    private final Gson gson = new Gson();
    private final UserDao userDao = new UserDao();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if (!"POST".equalsIgnoreCase(method)) {
                sendJson(exchange, 405, "{\"error\": \"Method not allowed\"}");
                return;
            }

            String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
            if (contentType == null || !contentType.contains("application/json")) {
                sendJson(exchange, 415, "{\"error\": \"Unsupported Media Type\"}");
                return;
            }

            switch (path) {
                case "/auth/register" -> handleRegister(exchange);
                case "/auth/login" -> handleLogin(exchange);
                default -> sendJson(exchange, 404, "{\"error\": \"Not found\"}");
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, "{\"error\": \"Internal server error\"}");
        }
    }

    private void handleRegister(HttpExchange exchange) throws IOException {
        RegisterRequest request = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), RegisterRequest.class);


        if (request.full_name == null || request.phone == null || request.password == null ||
                request.role == null || request.address == null) {
            sendJson(exchange, 400, "{\"error\": \"Invalid input - required fields missing\"}");
            return;
        }

        if (userDao.findByPhone(request.phone) != null) {
            sendJson(exchange, 409, "{\"error\": \"Phone number already exists\"}");
            return;
        }

        User user;
        switch (request.role.trim().toLowerCase()) {
            case "BUYER" -> user = new Buyer(
                    request.full_name, request.phone, request.email, request.password,
                    request.role, request.address, request.profileImageBase64, null, null
            );
            case "SELLER" -> {
                if (request.bank_info == null || request.bank_info.bank_name == null || request.bank_info.account_number == null) {
                    sendJson(exchange, 400, "{\"error\": \"Bank info required for seller\"}");
                    return;
                }
                user = new Seller(
                        request.full_name, request.phone, request.email, request.password,
                        request.role, request.address, request.profileImageBase64,
                        request.bank_info.bank_name, request.bank_info.account_number
                );
            }
            case "COURIER" -> {
                if (request.bank_info == null || request.bank_info.bank_name == null || request.bank_info.account_number == null) {
                    sendJson(exchange, 400, "{\"error\": \"Bank info required for courier\"}");
                    return;
                }
                user = new Courier(
                        request.full_name, request.phone, request.email, request.password,
                        request.role, request.address, request.profileImageBase64,
                        request.bank_info.bank_name, request.bank_info.account_number
                );
            }
            default -> {
                sendJson(exchange, 400, "{\"error\": \"Invalid role\"}");
                return;
            }
        }

        userDao.save(user);
        String token = JwtUtil.generateToken(user.getId().toString(), user.getRole());

        RegisterResponse response = new RegisterResponse(
                "User registered successfully",
                user.getId(),
                token
        );
        sendJson(exchange, 200, gson.toJson(response));
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        LoginRequest request = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), LoginRequest.class);

        if (request.phone == null || request.password == null) {
            sendJson(exchange, 400, "{\"error\": \"Invalid input\"}");
            return;
        }

        User user = userDao.findByPhoneAndPassword(request.phone, request.password);
        if (user == null) {
            sendJson(exchange, 401, "{\"error\": \"Invalid credentials\"}");
            return;
        }

        String token = JwtUtil.generateToken(user.getId().toString(), user.getRole());
        LoginResponse response = new LoginResponse(
                "Login successful",
                token,
                user
        );
        sendJson(exchange, 200, gson.toJson(response));
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
