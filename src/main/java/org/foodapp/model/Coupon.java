package org.foodapp.model;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "coupons")
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    private Integer discountAmount;
    private Date expirationDate;
    private Boolean isUsed = false;

    public Coupon() {}
    public Coupon(String code, Integer discountAmount, Date expirationDate, Boolean isUsed) {
        this.code = code;
        this.discountAmount = discountAmount;
        this.expirationDate = expirationDate;
        this.isUsed = isUsed;
    }
    // Getters and Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public Integer getDiscountAmount() {
        return discountAmount;
    }
    public void setDiscountAmount(Integer discountAmount) {
        this.discountAmount = discountAmount;
    }
    public Date getExpirationDate() {
        return expirationDate;
    }
    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }
    public Boolean getIsUsed() {
        return isUsed;
    }
    public void setIsUsed(Boolean isUsed) {
        this.isUsed = isUsed;
    }
}


