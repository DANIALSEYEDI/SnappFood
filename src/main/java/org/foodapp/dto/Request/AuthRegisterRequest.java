package org.foodapp.dto.Request;

public class AuthRegisterRequest {
    public String full_name;
    public String phone;
    public String password;
    public String role;
    public String address;
    public String email;
    public String profileImageBase64;
    public BankInfo bank_info;
    public static class BankInfo {
        public String bank_name;
        public String account_number;
    }
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
}

