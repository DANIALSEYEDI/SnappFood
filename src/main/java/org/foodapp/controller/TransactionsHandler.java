package org.foodapp.controller;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.foodapp.dao.TransactionDao;
import org.foodapp.dao.UserDao;
import org.foodapp.dto.TransactionsResponse;
import org.foodapp.model.Transaction;
import org.foodapp.model.User;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class TransactionsHandler implements HttpHandler {

    private final TransactionDao transactionDao = new TransactionDao();
    private final UserDao userDao = new UserDao();
    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        if (path.equals("/transactions") && method.equals("GET")) {
            handleGetTransactions(exchange);
        } else {
            exchange.sendResponseHeaders(404, -1);
        }
    }

    private void handleGetTransactions(HttpExchange exchange) throws IOException {
        try {
            User user = getAuthenticatedUser(exchange);
            if (user == null) {
                sendJson(exchange, 401,java.util.Map.of("error", "Unauthorized"));
                return;
            }

            List<Transaction> transactions = transactionDao.findByUser(user);
            List<TransactionsResponse> dtos = transactions.stream()
                    .map(TransactionsResponse::fromEntity)
                    .collect(Collectors.toList());

            sendJson(exchange, 200, dtos);
        } catch (Exception e) {
            sendJson(exchange, 500,  java.util.Map.of("error","Internal Server Error: " + e.getMessage()));
        }

    }

    private User getAuthenticatedUser(HttpExchange exchange) {
        return userDao.findById(1L); // فعلاً تستی، باید از توکن JWT یا Session استفاده شود
    }

    private void sendJson(HttpExchange exchange, int statusCode, Object body) throws IOException {
        String json = new Gson().toJson(body);
        byte[] bytes = json.getBytes();
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.getResponseBody().close();
    }
}

