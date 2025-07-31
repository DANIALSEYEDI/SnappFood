package org.example.snappfrontend.dto;

import java.math.BigDecimal;

public class AuthProfileResponse {
    public Long id;
    public String full_name;
    public String phone;
    public String email;
    public String address;
    public String role;
    public String profileImageBase64;
    public String bank_name;
    public String account_number;
    public BigDecimal walletbalance;

    public AuthProfileResponse() {}

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}
    public String getFull_name() {return full_name;}
    public void setFull_name(String full_name) {this.full_name = full_name;}
    public String getPhone() {return phone;}
    public void setPhone(String phone) {this.phone = phone;}
    public String getEmail() {return email;}
    public void setEmail(String email) {this.email = email;}
    public String getRole() {return role;}
    public void setRole(String role) {this.role = role;}
    public String getAddress() {return address;}
    public void setAddress(String address) {this.address = address;}
    public String getProfileImageBase64() {return profileImageBase64;}
    public void setProfileImageBase64(String profileImageBase64) {this.profileImageBase64 = profileImageBase64;}
    public String getBank_name() {return bank_name;}
    public void setBank_name(String bank_name) {this.bank_name = bank_name;}
    public String getAccount_number() {return account_number;}
    public void setAccount_number(String account_number) {this.account_number = account_number;}
    public BigDecimal getWalletbalance() {return walletbalance;}
    public void setWalletbalance(BigDecimal walletbalance) {this.walletbalance = walletbalance;}
    }