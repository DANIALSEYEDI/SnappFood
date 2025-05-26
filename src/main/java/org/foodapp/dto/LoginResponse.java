package org.foodapp.dto;

public class LoginResponse {
    public String message;
    public Long user_id;
    public String token;

    public LoginResponse(String message, Long user_id, String token) {
        this.message = message;
        this.user_id = user_id;
        this.token = token;
    }
}
