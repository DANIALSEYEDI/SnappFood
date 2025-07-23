package org.foodapp.dto;

import org.foodapp.model.User;

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


    public static AdminUserResponse fromEntity(User user) {
        AdminUserResponse dto = new AdminUserResponse();
        dto.id = user.getId();
        dto.fullName = user.getFullName();
        dto.phoneNumber = user.getPhoneNumber();
        dto.email = user.getEmail();
        dto.address = user.getAddress();
        dto.role = user.getRole().name();
        dto.profileImageBase64 = user.getProfileImageBase64();
        dto.bank_name = user.getBankName();
        dto.account_number = user.getAccountNumber();
        dto.status= String.valueOf(user.getStatus());
        return dto;
    }
}
