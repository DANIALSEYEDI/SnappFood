package org.example.snappfrontend.dto;

public class AuthRegisterResponse {
    public String message;
    public Long user_id;
    public String token;
    public AuthRegisterResponse() {}

    public String getMessage() {return message;}
    public Long getUser_id() {return user_id;}
    public String getToken() {return token;}
    public void setToken(String token) {this.token = token;}
    public void setMessage(String message) {this.message = message;}
    public void setUser_id(Long user_id) {this.user_id = user_id;}
}