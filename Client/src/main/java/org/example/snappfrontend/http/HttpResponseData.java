package org.example.snappfrontend.http;

public class HttpResponseData {
    private final int statusCode;
    private final String body;

    public HttpResponseData(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body;
    }
    public int getStatusCode() {return statusCode;}
    public String getBody() {return body;}
}