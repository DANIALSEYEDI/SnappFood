package org.foodapp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "buyers")

public class Buyer extends User {

    @Column(nullable = false)
    private String address;

public Buyer() {}

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

}

