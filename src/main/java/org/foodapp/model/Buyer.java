package org.foodapp.model;
import jakarta.persistence.*;

@Entity
@Table(name = "buyers")
public class Buyer extends User {
public Buyer() {}
    public Buyer(String name, String phoneNumber, String email, String password, String role, String address, String profileImageBase64, String bankName, String accountNumber) {
    super( name,  phoneNumber,  email, password, role, address, profileImageBase64, bankName, accountNumber);
    }
}

