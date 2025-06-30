package org.foodapp.controller;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.foodapp.dto.AdminLoginRequest;
import org.foodapp.model.Admin;
import org.foodapp.util.HibernateUtil;
import org.foodapp.util.JwtUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class AdminHandler implements HttpHandler {
    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST") ||
                !exchange.getRequestURI().getPath().equals("/admin/login")) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }

        InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        AdminLoginRequest request = gson.fromJson(reader, AdminLoginRequest.class);

        if (request.username == null || request.password == null) {
            sendJson(exchange, 400, "{\"error\": \"Invalid input\"}");
            return;
        }

        Session session = HibernateUtil.getSessionFactory().openSession();
        Query<Admin> query = session.createQuery("FROM Admin WHERE username = :username AND password = :password", Admin.class);
        query.setParameter("username", request.username);
        query.setParameter("password", request.password);
        Admin admin = query.uniqueResult();
        session.close();

        if (admin == null) {
            sendJson(exchange, 401, "{\"error\": \"Invalid credentials\"}");
            return;
        }

        String token = JwtUtil.generateToken(admin.getId().toString(), "admin");

        sendJson(exchange, 200, String.format("""
                {
                  "message": "Admin login successful",
                  "token": "%s"
                }
                """, token));
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
