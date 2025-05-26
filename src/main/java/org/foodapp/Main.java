package org.foodapp;

import org.foodapp.Controller.UserHttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.foodapp.Controller.AuthHandler;

import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws Exception {
        // ایجاد سرور روی پورت 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // تعریف مسیر /auth و تخصیص هندلر
        server.createContext("/auth", new AuthHandler());

        // شروع سرور
        server.start();
        System.out.println("✅ Server started on http://localhost:8080");
    }
}   
