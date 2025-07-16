package org.foodapp.controller;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.foodapp.dao.TransactionDao;
import org.foodapp.dao.UserDao;
import org.foodapp.dto.WalletTopUpRequest;
import org.foodapp.model.Transaction;
import org.foodapp.model.User;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
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
            exchange.sendResponseHeaders(404, -1);
        }
    }

    private void handleTopUp(HttpExchange exchange) throws IOException {
        try {
            WalletTopUpRequest request = gson.fromJson(new InputStreamReader(exchange.getRequestBody()), WalletTopUpRequest.class);

            if (request.amount == null || request.amount.compareTo(BigDecimal.ZERO) <= 0) {
                sendJson(exchange, 400, Map.of("error", "Amount must be greater than zero"));
                return;
            }

            User user = getAuthenticatedUser(exchange);
            user.setWalletBalance(user.getWalletBalance().add(request.amount));
            userDao.update(user);

            Transaction tx = new Transaction();
            tx.setAmount(request.amount);
            tx.setUser(user);
            tx.setCreatedAt(LocalDateTime.now());
            transactionDao.save(tx);

            sendJson(exchange, 200, Map.of("message", "Wallet topped up successfully", "balance", user.getWalletBalance()));
        } catch (Exception e) {
            sendJson(exchange, 500, Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    private User getAuthenticatedUser(HttpExchange exchange) {
        return userDao.findById(1L); // جایگزین با توکن یا session واقعی
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
