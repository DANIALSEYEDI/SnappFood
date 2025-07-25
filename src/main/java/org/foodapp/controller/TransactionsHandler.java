package org.foodapp.controller;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.foodapp.dao.TransactionDao;
import org.foodapp.dao.UserDao;
import org.foodapp.dto.Response.TransactionsResponse;
import org.foodapp.model.Transaction;
import org.foodapp.model.User;
import org.foodapp.util.JwtUtil;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class TransactionsHandler implements HttpHandler {

    private final TransactionDao transactionDao = new TransactionDao();
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        if (path.equals("/transactions") && method.equals("GET")) {
            handleGetTransactions(exchange);
        } else {
            sendJson(exchange, 404,"not_found path");
        }
    }

    private void handleGetTransactions(HttpExchange exchange) throws IOException {
        try {
            User user = authenticate(exchange);
            if (user == null) {
                return;
            }
            List<Transaction> transactions = transactionDao.findByUser(user);
            if (transactions.isEmpty()) {
                sendJson(exchange, 404, "not found");
                return;
            }
            List<TransactionsResponse> response = transactions.stream()
                    .map(TransactionsResponse::from)
                    .toList();

            sendJson(exchange, 200, response);
        } catch (Exception e) {
            sendJson(exchange, 500, Map.of("error", "internal_server_error"));
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
        return new UserDao().findById(Long.parseLong(decoded.getSubject()));
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

