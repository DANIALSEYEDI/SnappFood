package org.foodapp.Controller;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.foodapp.dto.*;
import org.foodapp.model.*;
import org.foodapp.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class AuthHandler implements HttpHandler {

    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if ("POST".equalsIgnoreCase(method)) {
            switch (path) {
                case "/auth/register" -> handleRegister(exchange);
                case "/auth/login" -> handleLogin(exchange);
                default -> exchange.sendResponseHeaders(404, -1);
            }
        } else {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
        }
    }

    private void handleRegister(HttpExchange exchange) throws IOException {
        InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        RegisterRequest request = gson.fromJson(reader, RegisterRequest.class);

        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        User user;
        switch (request.role.toUpperCase()) {
            case "BUYER" -> {
                Buyer buyer = new Buyer();
                buyer.setName(request.full_name);
                buyer.setPhoneNumber(request.phone);
                buyer.setPassword(request.password);
                buyer.setAddress(request.address);
                user = buyer;
            }
            case "SELLER" -> {
                Seller seller = new Seller();
                seller.setName(request.full_name);
                seller.setPhoneNumber(request.phone);
                seller.setPassword(request.password);
                seller.setAddress(request.address);
                user = seller;
            }
            case "COURIER" -> {
                Courier courier = new Courier();
                courier.setName(request.full_name);
                courier.setPhoneNumber(request.phone);
                courier.setPassword(request.password);
                user = courier;
            }
            default -> {
                sendJson(exchange, 400, "{\"message\": \"Invalid role\"}");
                return;
            }
        }
        session.persist(user);
        session.getTransaction().commit();
        session.close();
        RegisterResponse response = new RegisterResponse(
                "User registered successfully",
                user.getId(),
                "fake-jwt-token"
        );
        sendJson(exchange, 200, gson.toJson(response));
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        LoginRequest request = gson.fromJson(reader, LoginRequest.class);

        Session session = HibernateUtil.getSessionFactory().openSession();
        Query<User> query = session.createQuery("FROM User WHERE phoneNumber = :phone AND password = :password", User.class);
        query.setParameter("phone", request.phone);
        query.setParameter("password", request.password);
        User user = query.uniqueResult();
        session.close();

        if (user == null) {
            sendJson(exchange, 401, "{\"message\": \"Invalid credentials\"}");
            return;
        }

        LoginResponse response = new LoginResponse(
                "Login successful",
                user.getId(),
                "fake-jwt-token"
        );

        sendJson(exchange, 200, gson.toJson(response));
    }

    private void sendJson(HttpExchange exchange, int statusCode, String json) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, json.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(json.getBytes());
        os.close();
    }
}
