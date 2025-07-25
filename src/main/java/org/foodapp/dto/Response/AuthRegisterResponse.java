package org.foodapp.dto.Response;
public class AuthRegisterResponse {
    public String message;
    public Long user_id;
    public String token;

    public AuthRegisterResponse(String message, Long user_id, String token) {
        this.message = message;
        this.user_id = user_id;
        this.token = token;
    }
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
}
