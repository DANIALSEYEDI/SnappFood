package org.foodapp.model;
import jakarta.persistence.*;

@Entity
@Table(name = "admins")
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String phonenumber;

    @Column(nullable = false)
    private String password;

    public Admin() {}

    //Getter and Setter
    public void setPhonenumber(String phonenumber) {this.phonenumber = phonenumber;}
    public String getPhonenumber() {return this.phonenumber;}
    public void setPassword(String password) {
        this.password = password;
    }
    public String getPassword() {
        return password;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
}
