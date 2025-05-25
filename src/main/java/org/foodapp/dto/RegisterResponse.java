package org.foodapp.dto;

public class RegisterResponse {
    public String message;
    public Long user_id;
    public String token;

    public RegisterResponse(String message, Long user_id, String token) {
        this.message = message;
        this.user_id = user_id;
        this.token = token;
    }
}
