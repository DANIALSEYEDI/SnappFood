package org.foodapp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "sellers")
public class Seller extends User {

    @Column
    private String bankAccount;
     @Column
     private String address;

     public Seller() {}


    public String getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }
    public String getAddress() {return address;
    }
    public void setAddress(String address) {this.address = address;
    }
}
