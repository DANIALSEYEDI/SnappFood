package org.foodapp.dto;
public class AuthLoginResponse {
    public String message;
    public String token;
    public AuthProfileResponse user;

    public AuthLoginResponse(String message, String token,AuthProfileResponse user) {
        this.message = message;
        this.token = token;
        this.user = user;
    }
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
    public AuthProfileResponse getUser() {
        return user;
    }
    public void setUser(AuthProfileResponse user) {
        this.user = user;
    }
}

