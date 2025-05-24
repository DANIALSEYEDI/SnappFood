package org.foodapp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "deliverers")
public class Deliverer extends User {

    @Column
    private String bankAccount;

    public String getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }
}
