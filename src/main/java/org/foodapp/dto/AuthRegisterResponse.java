package org.foodapp.dto;

public class AuthRegisterResponse {
    public String message;
    public Long user_id;
    public String token;

    public AuthRegisterResponse(String message, Long user_id, String token) {
        this.message = message;
        this.user_id = user_id;
        this.token = token;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public Long getUser_id() {
        return user_id;
    }
    public void setUser_id(Long user_id) {
        this.user_id = user_id;
    }
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }

}
