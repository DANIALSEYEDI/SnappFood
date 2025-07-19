package org.foodapp.dto;

import org.foodapp.model.Role;

public class AuthProfileResponse {
    public Long id;
    public String full_name;
    public String phone;
    public String email;
    public Role role;
    public String address;
    public String profileImageBase64;
    public String bank_name;
    public String account_number;

    public AuthProfileResponse(Long id, String name, String phone, String email, Role role,String address, String profileImageBase64, String bank_name, String account_number){
        this.id = id;
        full_name = name;
        this.phone = phone;
        this.email = email;
        this.role = role;
        this.address = address;
        this.profileImageBase64 = profileImageBase64;
        this.bank_name = bank_name;
        this.account_number = account_number;
    }
}
