package Controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class UserHttpHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("user/signup")) {
                handleSignup(exchange);
            }
            else if (path.equals("/user/login")) {
                handleLogin(exchange);
            } else {
                exchange.sendResponseHeaders(404, -1); // Not Found
            }
        }
        else
        {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
        }
    }
    private void handleSignup(HttpExchange exchange) throws IOException {
    }
    private void handleLogin(HttpExchange exchange) throws IOException {
    }

}


