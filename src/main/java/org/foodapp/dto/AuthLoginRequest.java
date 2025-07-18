package org.foodapp.dto;

public class AuthLoginRequest {
    public String phone;
    public String password;
    public AuthLoginRequest(String phone, String password) {
        this.phone = phone;
        this.password = password;
    }
}
