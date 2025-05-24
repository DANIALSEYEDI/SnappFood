package org.foodapp;


import org.foodapp.Controller.UserHttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws Exception{
        HttpServer server = HttpServer.create(new InetSocketAddress(8080),0);
        server.createContext("/user", new UserHttpHandler());

        server.start();
        System.out.println("server started");
    }
}
