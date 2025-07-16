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
        server.createContext("/items", new ItemsHandler());
        server.createContext("/coupons", new CouponsHandler());
        server.createContext("/orders", new OrderHandler());
        server.createContext("/favorites", new FavoriteHandler());
        server.createContext("/ratings", new RatingHandler());
        server.createContext("/deliveries", new DeliveryHandler());
        server.createContext("/transactions", new TransactionsHandler());
        server.createContext("/wallet", new WalletHandler());

        server.start();
        System.out.println("✅ Server started on http://localhost:8080");
    }
}
