package org.example.snappfrontend.dto;

public class AuthProfileUpdateRequestDTO {
    public String full_name;
    public String phone;
    public String email;
    public String address;
    public String profileImageBase64;
    public BankInfo bank_info;


    public static class BankInfo {
        public String bank_name;
        public String account_number;

        public String getBank_name() {return bank_name;}
        public String getAccount_number() {return account_number;}
        public void setBank_name(String bank_name) {this.bank_name = bank_name;}
        public void setAccount_number(String account_number) {this.account_number = account_number;}
    }

    public AuthProfileUpdateRequestDTO() {}
    public String getFull_name() {return full_name;}
    public String getPhone() {return phone;}
    public String getEmail() {return email;}
    public String getAddress() {return address;}
    public String getProfileImageBase64() {return profileImageBase64;}
    public BankInfo getBank_info() {return bank_info;}
    public void setFull_name(String full_name) {this.full_name = full_name;}
    public void setPhone(String phone) {this.phone = phone;}
    public void setEmail(String email) {this.email = email;}
    public void setAddress(String address) {this.address = address;}
    public void setProfileImageBase64(String profileImageBase64) {this.profileImageBase64 = profileImageBase64;}
    public void setBank_info(BankInfo bank_info) {this.bank_info = bank_info;}
}