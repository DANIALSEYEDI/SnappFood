package org.foodapp.dto;

public class AuthLoginRequest {
    public String phone;
    public String password;
    public AuthLoginRequest(String phone, String password) {
        this.phone = phone;
        this.password = password;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
}
