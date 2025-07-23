package org.foodapp.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.foodapp.dao.CreateAdminDao;
import org.foodapp.dto.AdminLoginRequest;
import org.foodapp.model.Admin;
import org.foodapp.util.JwtUtil;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class AdminLoginHandler implements HttpHandler {
    private final Gson gson = new Gson();
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            sendJson(exchange, 405, "{\"error\": \"Method not allowed\"}");
            return;
        }

        try {
            AdminLoginRequest request = gson.fromJson(
                    new InputStreamReader(exchange.getRequestBody()),
                    AdminLoginRequest.class
            );
            if (request.phonenumber == null || request.password == null) {
                sendJson(exchange, 400, "{\"error\": \"fields are missing\"}");
                return;
            }
            CreateAdminDao adminDao = new CreateAdminDao();
            Admin admin = adminDao.findByPhonenumber(request.phonenumber);

            if (admin == null || !admin.getPassword().equals(request.password)) {
                sendJson(exchange, 404, "{\"error\": \"not_found admin\"}");
                return;
            }
            String token = JwtUtil.generateToken(admin.getId().toString(), "ADMIN");
            sendJson(exchange, 200, String.format("{\"token\": \"%s\"}", token));
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, "{\"error\": \"internal server error\"}");
        }

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
