package org.foodapp.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.foodapp.dao.TransactionDao;
import org.foodapp.dao.UserDao;
import org.foodapp.dto.WalletTopUpRequest;
import org.foodapp.model.Transaction;
import org.foodapp.model.User;
import org.foodapp.util.JwtUtil;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class WalletHandler implements HttpHandler {
    private final Gson gson = new Gson();
    private final UserDao userDao = new UserDao();
    private final TransactionDao transactionDao = new TransactionDao();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        if (path.equals("/wallet/top-up") && method.equalsIgnoreCase("POST")) {
            handleTopUp(exchange);
        } else {
            sendJson(exchange, 404,"Path not found");
        }
    }




    private void handleTopUp(HttpExchange exchange) throws IOException {
        try {
            User user = authenticate(exchange);
            if (user == null) return;

            String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
            if (contentType == null || !contentType.contains("application/json")) {
                sendJson(exchange, 415, Map.of("error", "Unsupported Media Type"));
                return;
            }
            Map<String, Object> body = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), Map.class);
            if (body == null || !body.containsKey("amount")) {
                sendJson(exchange, 400, Map.of("error", "Amount is required"));
                return;
            }

            BigDecimal amount = new BigDecimal(body.get("amount").toString());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                sendJson(exchange, 400, Map.of("error", "Amount must be positive"));
                return;
            }

            user.increaseWallet(amount);
            userDao.update(user);
            Transaction transaction = new Transaction();
            transaction.setUser(user);
            transaction.setAmount(amount);
            transaction.setDescription("TOP_UP");
            transactionDao.save(transaction);
            sendJson(exchange, 200, Map.of("message", "Wallet topped up successfully"));
        }
        catch (NumberFormatException e) {
            e.printStackTrace();
            sendJson(exchange, 400, Map.of("error", "Invalid amount format"));
        }
        catch (Exception e) {
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
        return userDao.findById(Long.parseLong(decoded.getSubject()));
    }




    private void sendJson(HttpExchange exchange, int statusCode, Object body) throws IOException {
        String json = new Gson().toJson(body);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

}
