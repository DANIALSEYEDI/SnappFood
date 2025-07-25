package org.foodapp.dto.Request;

public class AuthProfileUpdateRequest {
    public String full_name;
    public String phone;
    public String email;
    public String address;
    public String profileImageBase64;
    public BankInfo bank_info;

    public static class BankInfo {
        public String bank_name;
        public String account_number;
    }
}

