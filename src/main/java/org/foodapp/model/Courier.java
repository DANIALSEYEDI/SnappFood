package org.foodapp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "courier")
public class Courier extends User {

    @Column
    private String bankAccount;

    public Courier() {}

    public String getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }
}
