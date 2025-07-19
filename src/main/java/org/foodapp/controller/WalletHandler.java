package org.foodapp.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.foodapp.dao.TransactionDao;
import org.foodapp.dao.UserDao;
import org.foodapp.dto.WalletTopUpRequest;
import org.foodapp.model.PaymentMethod;
import org.foodapp.model.PaymentStatus;
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
            sendJson(exchange, 404,"not_found path");
        }
    }



    private void handleTopUp(HttpExchange exchange) throws IOException {
        Transaction tx = new Transaction();
        try {
            User user = authenticate(exchange);
            if (user == null) {
                return;
            }
            tx.setUser(user);
            tx.setMethod(PaymentMethod.WALLET);
            tx.setCreatedAt(LocalDateTime.now());

            Map<String, Object> body = gson.fromJson(
                    new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8),
                    Map.class
            );

            if (body == null || !body.containsKey("amount") ||  body.get("amount") == null) {
                tx.setStatus(PaymentStatus.FAILED);
                transactionDao.save(tx);
                sendJson(exchange, 400, Map.of("error", "invalid_input"));
                return;
            }

            BigDecimal amount;
            try {
                amount = new BigDecimal(body.get("amount").toString());
            } catch (NumberFormatException e) {
                tx.setStatus(PaymentStatus.FAILED);
                transactionDao.save(tx);
                sendJson(exchange, 400, Map.of("error", "invalid_input"));
                return;
            }


            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                tx.setAmount(amount);
                tx.setStatus(PaymentStatus.FAILED);
                transactionDao.save(tx);
                sendJson(exchange, 400, Map.of("error", "invalid_input"));
                return;
            }

            BigDecimal current = user.getWalletBalance();
            user.setWalletBalance(current.add(amount));
            userDao.update(user);

            tx.setAmount(amount);
            tx.setStatus(PaymentStatus.SUCCESS);
            transactionDao.save(tx);
            sendJson(exchange, 200, Map.of("message", "Wallet topped up successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            tx.setStatus(PaymentStatus.FAILED);
            transactionDao.save(tx);
            sendJson(exchange, 500, Map.of("error", "Internal_server_error"));
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
