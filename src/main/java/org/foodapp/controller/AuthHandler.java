package org.foodapp.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.foodapp.dao.UserDao;
import org.foodapp.dto.*;
import org.foodapp.model.*;
import org.foodapp.util.JwtUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class AuthHandler implements HttpHandler {

    private final Gson gson = new Gson();
    private final UserDao userDao = new UserDao();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            switch (path) {
                case "/auth/register" -> {
                    if (method.equalsIgnoreCase("POST")) handleRegister(exchange);
                    else sendJson(exchange, 405, "{\"error\": \"Method not allowed\"}");
                }
                case "/auth/login" -> {
                    if (method.equalsIgnoreCase("POST")) handleLogin(exchange);
                    else sendJson(exchange, 405, "{\"error\": \"Method not allowed\"}");
                }
                case "/auth/profile" -> {
                    if (method.equalsIgnoreCase("GET")) handleProfileGet(exchange);
                    else if (method.equalsIgnoreCase("PUT")) handleProfileUpdate(exchange);
                    else sendJson(exchange, 405, "{\"error\": \"Method not allowed\"}");
                }
                case "/auth/logout" -> {
                    if (method.equalsIgnoreCase("POST")) handleLogout(exchange);
                    else sendJson(exchange, 405, "{\"error\": \"Method not allowed\"}");
                }
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

        Role role;
        try {
            role = Role.valueOf(request.role.toUpperCase());
        } catch (IllegalArgumentException e) {
            sendJson(exchange, 400, "{\"error\": \"Invalid role\"}");
            return;
        }

        String bankName = null;
        String accountNumber = null;

        if ((role == Role.SELLER || role == Role.COURIER)) {
            if (request.bank_info == null || request.bank_info.bank_name == null || request.bank_info.account_number == null) {
                sendJson(exchange, 400, "{\"error\": \"Bank info required\"}");
                return;
            }
            bankName = request.bank_info.bank_name;
            accountNumber = request.bank_info.account_number;
        }

        User user = new User(
                request.full_name,
                request.phone,
                request.email,
                request.password,
                request.address,
                request.profileImageBase64,
                role,
                bankName,
                accountNumber
        );

        userDao.save(user);

        String token = JwtUtil.generateToken(user.getId().toString(), user.getRole().toString());

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

        String token = JwtUtil.generateToken(user.getId().toString(), user.getRole().toString());
        LoginResponse response = new LoginResponse(
                "Login successful",
                token,
                user
        );
        sendJson(exchange, 200, gson.toJson(response));
    }

    private void handleProfileGet(HttpExchange exchange) throws IOException {
        User user = authenticate(exchange);
        if (user == null) return;
        sendJson(exchange, 200, gson.toJson(user));
    }

    private void handleProfileUpdate(HttpExchange exchange) throws IOException {
        User user = authenticate(exchange);
        if (user == null) return;

        ProfileUpdateRequest request = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), ProfileUpdateRequest.class);

        if (request.full_name != null) user.setFullName(request.full_name);
        if (request.phone != null) user.setPhoneNumber(request.phone);
        if (request.email != null) user.setEmail(request.email);
        if (request.address != null) user.setAddress(request.address);
        if (request.profileImageBase64 != null) user.setProfileImageBase64(request.profileImageBase64);

        if ((user.getRole() == Role.SELLER || user.getRole() == Role.COURIER) && request.bank_info != null) {
            user.setBankName(request.bank_info.bank_name);
            user.setAccountNumber(request.bank_info.account_number);
        }

        userDao.update(user);
        sendJson(exchange, 200, "{\"message\": \"Profile updated successfully\"}");
    }

    private void handleLogout(HttpExchange exchange) throws IOException {
        User user = authenticate(exchange);
        if (user == null) return;
        sendJson(exchange, 200, "{\"message\": \"User logged out successfully\"}");
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

    private void sendJson(HttpExchange exchange, int statusCode, String json) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}



