package org.foodapp.dto;

public class RegisterRequest {
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

        public BankInfo() {}
        public BankInfo(String bank_name, String account_number) {
            this.bank_name = bank_name;
            this.account_number = account_number;
        }

        public String getBank_name() {
            return bank_name;
        }

        public void setBank_name(String bank_name) {
            this.bank_name = bank_name;
        }

        public String getAccount_number() {
            return account_number;
        }

        public void setAccount_number(String account_number) {
            this.account_number = account_number;
        }
    }

    public RegisterRequest() {}

    public RegisterRequest(String full_name, String phone, String password, String role, String address,
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

    // Getters and setters for other fields...

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileImageBase64() {
        return profileImageBase64;
    }

    public void setProfileImageBase64(String profileImageBase64) {
        this.profileImageBase64 = profileImageBase64;
    }

    public BankInfo getBank_info() {
        return bank_info;
    }

    public void setBank_info(BankInfo bank_info) {
        this.bank_info = bank_info;
    }
}

