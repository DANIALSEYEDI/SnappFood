package org.foodapp.dto;

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
    public AuthRegisterRequest(String full_name, String phone, String password, String role, String address,
                               String email, String profileImageBase64, BankInfo bank_info) {
        this.full_name = full_name;
        this.phone = phone;
        this.password = password;
        this.role = role;
        this.address = address;
        this.email = email;
        this.profileImageBase64 = profileImageBase64;
        this.bank_info = bank_info;
    }
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
}

