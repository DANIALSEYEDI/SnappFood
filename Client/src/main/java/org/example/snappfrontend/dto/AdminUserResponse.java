package org.example.snappfrontend.dto;

public class AdminUserResponse {
    public Long id;
    public String fullName;
    public String phoneNumber;
    public String email;
    public String address;
    public String role;
    public String profileImageBase64;
    public String bank_name;
    public String account_number;
    public String status;

    public AdminUserResponse() {}

    public Long getId() {return id;}
    public String getFullName() {return fullName;}
    public String getPhoneNumber() {return phoneNumber;}
    public String getEmail() {return email;}
    public String getAddress() {return address;}
    public String getRole() {return role;}
    public String getProfileImageBase64() {return profileImageBase64;}
    public String getBank_name() {return bank_name;}
    public String getAccount_number() {return account_number;}
    public String getStatus() {return status;}
    public void setId(Long id) {this.id = id;}
    public void setFullName(String fullName) {this.fullName = fullName;}
    public void setPhoneNumber(String phoneNumber) {this.phoneNumber = phoneNumber;}
    public void setEmail(String email) {this.email = email;}
    public void setAddress(String address) {this.address = address;}
    public void setRole(String role) {this.role = role;}
    public void setProfileImageBase64(String profileImageBase64) {this.profileImageBase64 = profileImageBase64;}
    public void setBank_name(String bank_name) {this.bank_name = bank_name;}
    public void setAccount_number(String account_number) {this.account_number = account_number;}
    public void setStatus(String status) {this.status = status;}
}