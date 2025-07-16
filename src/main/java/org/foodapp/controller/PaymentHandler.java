package org.foodapp.controller;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.foodapp.dao.*;
import org.foodapp.dto.PaymentResponse;
import org.foodapp.model.*;
import org.foodapp.util.QueryParser;

import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Map;

public class PaymentHandler implements HttpHandler {

    private final OrderDao orderDao = new OrderDao();
    private final UserDao userDao = new UserDao();
    private final PaymentDao paymentDao = new PaymentDao();
    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        if (path.equals("/payment/online") && method.equalsIgnoreCase("POST")) {
            handlePayment(exchange);
        } else {
            exchange.sendResponseHeaders(404, -1);
        }
    }

    private void handlePayment(HttpExchange exchange) throws IOException {
        try {
            Map<String, Object> body = gson.fromJson(new InputStreamReader(exchange.getRequestBody()), Map.class);
            Double orderIdD = (Double) body.get("order_id");
            PaymentMethod method = (PaymentMethod) body.get("method");

            if (orderIdD == null || method == null) {
                sendJson(exchange, 400, Map.of("error", "Missing fields"));
                return;
            }

            Long orderId = orderIdD.longValue();
            Order order = orderDao.findById(orderId);
            if (order == null) {
                sendJson(exchange, 404, Map.of("error", "Order not found"));
                return;
            }

            User user = order.getUser();

            Payment payment = new Payment();
            payment.setOrder(order);
            payment.setMethod(method);
            payment.setAmount(order.getTotalPrice());
            payment.setUser(user);

            paymentDao.save(payment);

            PaymentResponse response = PaymentResponse.fromEntity(payment);
            sendJson(exchange, 200, response);

        } catch (Exception e) {
            sendJson(exchange, 500, Map.of("error", "Payment failed"));
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
}

