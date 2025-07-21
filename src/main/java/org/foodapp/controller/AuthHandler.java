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
                }
                case "/auth/login" -> {
                    if (method.equalsIgnoreCase("POST")) handleLogin(exchange);
                }
                case "/auth/profile" -> {
                    if (method.equalsIgnoreCase("GET")) handleProfileGet(exchange);
                    else if (method.equalsIgnoreCase("PUT")) handleProfileUpdate(exchange);
                }
                case "/auth/logout" -> {
                    if (method.equalsIgnoreCase("POST")) handleLogout(exchange);
                }
                default -> sendJson(exchange, 404, "{\"error\": \"not_found\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, "{\"error\": \"internal_server_error\"}");
        }
    }




    private void handleRegister(HttpExchange exchange) throws IOException {
        AuthRegisterRequest request = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), AuthRegisterRequest.class);

        if (request.full_name == null || request.phone == null || request.password == null ||
                request.role == null ) {
            sendJson(exchange, 400, "{\"error\": \"invalid_input\"}");
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
            sendJson(exchange, 400, "{\"error\": \"invalid_input\"}");
            return;
        }

        String bankName=null ;
        String accountNumber=null ;
        UserStatus userStatus = UserStatus.REJECTED;

        if ((role == Role.SELLER || role == Role.COURIER)) {
            if (request.bank_info == null || request.bank_info.bank_name == null || request.bank_info.account_number == null) {
                sendJson(exchange, 400, "{\"error\": \"invalid_input bank_info\"}");
                return;
            }
        }
        if (request.bank_info != null && (request.bank_info.bank_name != null || request.bank_info.account_number != null)) {
            bankName = request.bank_info.bank_name;
            accountNumber = request.bank_info.account_number;
        }

        if (role == Role.SELLER || role == Role.BUYER) {
            if (request.address==null ||  request.address.trim().isEmpty()){
                sendJson(exchange, 400, "{\"error\": \"invalid_input\"}");
                return;
            }
        }

        try {
            User user = new User(
                    request.full_name,
                    request.phone,
                    request.email,
                    request.password,
                    request.address,
                    request.profileImageBase64,
                    role,
                    bankName,
                    accountNumber,
                    userStatus
            );
            userDao.save(user);
            String token = JwtUtil.generateToken(user.getId().toString(), user.getRole().toString());
            AuthRegisterResponse response = new AuthRegisterResponse(
                    "User registered successfully",
                    user.getId(),
                    token
            );
            sendJson(exchange, 200, gson.toJson(response));
        }
        catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, "{\"error\": \"internal_server_error\"}");
        }
    }






    private void handleLogin(HttpExchange exchange) throws IOException {
        try {
            AuthLoginRequest request = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), AuthLoginRequest.class);

            if (request.phone == null || request.password == null) {
                sendJson(exchange, 400, "{\"error\": \"invalid_input\"}");
                return;
            }

            User user = userDao.findByPhoneAndPassword(request.phone, request.password);
            if (user == null) {
                sendJson(exchange, 401, "{\"error\": \"User not found\"}");
                return;
            }
            String token = JwtUtil.generateToken(user.getId().toString(), user.getRole().toString());
            AuthProfileResponse response = new AuthProfileResponse(
                    user.getId(),
                    user.getFullName(),
                    user.getPhoneNumber(),
                    user.getEmail(),
                    user.getRole(),
                    user.getAddress(),
                    user.getProfileImageBase64(),
                    user.getBankName(),
                    user.getAccountNumber()
            );

            AuthLoginResponse res = new AuthLoginResponse(
                    "User logged in successfully",
                    token,
                    response
            );
            sendJson(exchange, 200, gson.toJson(res));
        }
        catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, "{\"error\": \"internal_server_error\"}");
        }
    }






    private void handleProfileGet(HttpExchange exchange) throws IOException {
        try {
            String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
            if (contentType != null && !contentType.contains("application/json")) {
                sendJson(exchange, 415, "{\"error\": \"unsupported_media_type\"}");
                return;
            }
            User user = authenticate(exchange);
            if (user == null) {
                sendJson(exchange, 401, "{\"error\": \"Unauthorized\"}");
                return;
            }
            AuthProfileResponse response = new AuthProfileResponse(
                    user.getId(),
                    user.getFullName(),
                    user.getPhoneNumber(),
                    user.getEmail(),
                    user.getRole(),
                    user.getAddress(),
                    user.getProfileImageBase64(),
                    user.getBankName(),
                    user.getAccountNumber()
            );
            sendJson(exchange, 200, gson.toJson(response));
        }
        catch (NumberFormatException e) {
            sendJson(exchange, 400, "{\"error\": \"invalid_input\"}");
        } catch (SecurityException e) {
            sendJson(exchange, 403, "{\"error\": \"Forbidden\"}");
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, "{\"error\": \"internal_server_error\"}");
        }
    }





    private void handleProfileUpdate(HttpExchange exchange) throws IOException {
        try {
            User user = authenticate(exchange);
            if (user == null) {
                sendJson(exchange, 401, "{\"error\": \"Unauthorized\"}");
                return;
            }
            String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
            if (contentType == null || !contentType.contains("application/json")) {
                sendJson(exchange, 415, "{\"error\": \"unsupported_media_type\"}");
                return;
            }

            AuthProfileUpdateRequest request = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), AuthProfileUpdateRequest.class);

            if (request == null) {
                sendJson(exchange, 400, "{\"error\": \"invalid_input\"}");
                return;
            }

            if (request.full_name != null) user.setFullName(request.full_name);
            if (request.phone != null) user.setPhoneNumber(request.phone);
            if (request.email != null) user.setEmail(request.email);
            if ((user.getRole()==Role.BUYER && user.getRole()==Role.SELLER) && request.address != null) user.setAddress(request.address);
            if (request.profileImageBase64 != null) user.setProfileImageBase64(request.profileImageBase64);
            if ((user.getRole() == Role.SELLER || user.getRole() == Role.COURIER) && request.bank_info != null) {
                user.setBankName(request.bank_info.bank_name);
                user.setAccountNumber(request.bank_info.account_number);
            }
            userDao.update(user);
            sendJson(exchange, 200, "{\"message\": \"Profile updated successfully\"}");
        }
        catch (Exception e) {
        e.printStackTrace();
        sendJson(exchange, 500, "{\"error\": \"internal_server_error\"}");
    }
    }






    private void handleLogout(HttpExchange exchange) throws IOException {
        try {
            User user = authenticate(exchange);
            if (user == null) {
                sendJson(exchange, 401, "{\"error\": \"Unauthorized\"}");
                return;
            }
            sendJson(exchange, 200, "{\"message\": \"User logged out successfully\"}");
        }
        catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, "{\"error\": \"internal_server_error\"}");
        }
    }





    private User authenticate(HttpExchange exchange) throws IOException {
        List<String> authHeaders = exchange.getRequestHeaders().get("Authorization");
        if (authHeaders == null || authHeaders.isEmpty()) {
            sendJson(exchange, 401, "{\"error\": \"unauthorized\"}");
            return null;
        }
        String token = authHeaders.get(0).replace("Bearer ", "");
        DecodedJWT decoded;
        try {
            decoded = JwtUtil.verifyToken(token);
        } catch (Exception e) {
            sendJson(exchange, 401, "{\"error\": \"unauthorized\"}");
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



