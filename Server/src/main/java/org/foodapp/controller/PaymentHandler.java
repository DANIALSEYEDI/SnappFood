package org.foodapp.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.foodapp.dao.*;
import org.foodapp.dto.Request.PaymentRequest;
import org.foodapp.dto.Response.TransactionsResponse;
import org.foodapp.model.*;
import org.foodapp.util.JwtUtil;
import java.io.InputStreamReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class PaymentHandler implements HttpHandler {
    private final Gson gson = new Gson();
    private final OrderDao orderDao = new OrderDao();
    private final TransactionDao transactionDao = new TransactionDao();
    private final UserDao userDao = new UserDao();
    private final TransactionDao TransactionDao = new TransactionDao();
    private final TransactionsResponse transactionsResponse = new TransactionsResponse();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        if (path.equals("/payment/online") && method.equalsIgnoreCase("POST")) {
            handlePayment(exchange);
        } else {
            sendJson(exchange, 404, "{\"error\": \"not_found\"}");
        }
    }



    private void handlePayment(HttpExchange exchange) throws IOException {
        try {
            Transaction transaction = new Transaction();
            User user = authenticate(exchange);
            if (user == null) return;
            transaction.setUser(user);
            transaction.setCreatedAt(LocalDateTime.now());

            PaymentRequest request = gson.fromJson(
                    new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8),
                    PaymentRequest.class
            );

            if (request == null || request.order_id == null || request.method == null) {
                sendJson(exchange, 400, Map.of("error", "invalid_input"));
                return;
            }
            PaymentMethod method;
            try {
                method = PaymentMethod.valueOf(request.method.toUpperCase());
            } catch (IllegalArgumentException e) {
                sendJson(exchange, 400, Map.of("error", "Invalid_input method"));
                return;
            }
            transaction.setMethod(method);
            Order order = orderDao.findById(request.order_id);
            if (order == null || !order.getUser().getId().equals(user.getId())) {
                sendJson(exchange, 404, Map.of("error", "not_found order"));
                return;
            }
            if (!(order.getStatus()==OrderStatus.UNPAID_AND_CANCELLED) ) {
                sendJson(exchange, 409, Map.of("error", "Order cannot be paid"));
                return;
            }
            transaction.setOrder(order);

            BigDecimal payAmount = BigDecimal.valueOf(order.getPayPrice());
            if (method == PaymentMethod.WALLET) {
                if (user.getWalletBalance().compareTo(payAmount) < 0) {
                    transaction.setStatus(PaymentStatus.FAILED);
                    transaction.setAmount(payAmount);
                    transactionDao.save(transaction);
                    sendJson(exchange, 409, Map.of("error", "Insufficient wallet balance"));
                    return;
                }
                user.setWalletBalance(user.getWalletBalance().subtract(payAmount));
                userDao.update(user);
                transaction.setStatus(PaymentStatus.SUCCESS);
            } else {
                transaction.setStatus(PaymentStatus.SUCCESS);
            }

            transaction.setAmount(payAmount);
            order.setStatus(OrderStatus.SUBMITTED);
            TransactionDao.save(transaction);
            order.setUpdatedAt(LocalDateTime.now());
            orderDao.update(order);
            sendJson(exchange, 200, transactionsResponse.from(transaction));
            } catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, "{\"error\": \"internal_server_error\"}");
        }
    }


    private void sendJson(HttpExchange exchange, int status, Object response) throws IOException {
        String json = gson.toJson(response);
        byte[] bytes = json.getBytes();
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
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
        return userDao.findById(Long.parseLong(decoded.getSubject()));
    }
}