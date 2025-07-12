package org.foodapp;

import com.sun.net.httpserver.HttpServer;
import org.foodapp.controller.*;

import java.net.InetSocketAddress;


public class Main {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/auth", new AuthHandler());
        server.createContext("/restaurants",  new RestaurantHandler());
        server.createContext("/vendors", new VendorsHandler());
        server.createContext("/items", new BuyerHandler());
        server.createContext("/orders", new OrderHandler());

        server.start();
        System.out.println("✅ Server started on http://localhost:8080");
    }
}
