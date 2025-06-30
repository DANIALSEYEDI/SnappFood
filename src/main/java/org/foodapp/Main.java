package org.foodapp;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.foodapp.controller.AdminHandler;
import org.foodapp.controller.AdminRestaurantHandler;
import org.foodapp.controller.AuthHandler;
import org.foodapp.controller.RestaurantHandler;

import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);


        server.createContext("/auth", new AuthHandler());

        server.createContext("/restaurants",  new RestaurantHandler());

        server.createContext("/admin/restaurants", new AdminRestaurantHandler());

        server.createContext("/admin/login", new AdminHandler());

        server.start();
        System.out.println("✅ Server started on http://localhost:8080");
    }
}
