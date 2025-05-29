package org.foodapp.model;
import jakarta.persistence.*;


@Entity
@Table(name = "sellers")
public class Seller extends User {
     public Seller() {}
     public Seller(String name, String phoneNumber, String email, String password, String role, String address, String profileImageBase64, String bankName, String accountNumber) {
         super(name, phoneNumber, email,  password,  role, address, profileImageBase64, bankName,  accountNumber);
     }
}
