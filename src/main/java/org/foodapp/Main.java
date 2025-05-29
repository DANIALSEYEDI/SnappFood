package org.foodapp;
import com.sun.net.httpserver.HttpServer;
import org.foodapp.controller.AuthHandler;
import java.net.InetSocketAddress;


public class Main {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/auth", new AuthHandler());
        server.start();
        System.out.println("✅ Server started on http://localhost:8080");
    }
}   
