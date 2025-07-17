package org.foodapp.dto;

import org.foodapp.model.User;

public class AdminUserResponse {
    public Long id;
    public String fullName;
    public String password;
    public String phoneNumber;
    public String email;
    public String address;
    public String role;
    public String status;


    public static AdminUserResponse fromEntity(User user) {
        AdminUserResponse dto = new AdminUserResponse();
        dto.id = user.getId();
        dto.password = user.getPassword();
        dto.fullName = user.getFullName();
        dto.phoneNumber = user.getPhoneNumber();
        dto.email = user.getEmail();
        dto.address = user.getAddress();
        dto.role = user.getRole().name();
        dto.status = user.getStatus().name();
        return dto;
    }
}
