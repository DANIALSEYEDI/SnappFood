package org.foodapp.model;
import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "user_type")
@Table(name = "users")
public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Column(nullable = false)
    protected String name;

    @Column(nullable = false, unique = true)
    protected String phoneNumber;

    @Column
    protected String email;

    @Column(nullable = false)
    protected String password;

    @Column(nullable = false)
    protected String role;

    @Column
    protected String address;

    @Column(name = "profile_image", columnDefinition = "TEXT")
    protected String profileImageBase64;

    @Column(name = "bank_name")
    protected String bankName;

    @Column(name = "account_number")
    protected String accountNumber;

    public User() {}

    public User(String name, String phoneNumber, String email, String password, String role, String address, String profileImageBase64, String bankName, String accountNumber) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.password = password;
        this.role = role;
        this.address = address;
        this.profileImageBase64 = profileImageBase64;
        this.bankName = bankName;
        this.accountNumber = accountNumber;
    }


    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getProfileImageBase64() { return profileImageBase64; }
    public void setProfileImageBase64(String profileImageBase64) { this.profileImageBase64 = profileImageBase64; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
}



