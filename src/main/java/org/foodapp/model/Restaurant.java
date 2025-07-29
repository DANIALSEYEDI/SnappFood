package org.foodapp.model;
import jakarta.persistence.*;
import java.util.*;
@Entity
@Table(name = "restaurants")
public class Restaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String logoBase64;

    private Integer taxFee;
    private Integer additionalFee;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private User seller;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<Menu> menus = new HashSet<>();



    public Restaurant() {}

    public Restaurant(String name, String address, String phone, String logoBase64,
                      Integer taxFee, Integer additionalFee, User seller) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.logoBase64 = logoBase64;
        this.taxFee = taxFee;
        this.additionalFee = additionalFee;
        this.seller = seller;
    }


    // Getters and Setters
    public Long getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public String getLogoBase64() {
        return logoBase64;
    }
    public void setLogoBase64(String logoBase64) {
        this.logoBase64 = logoBase64;
    }
    public Integer getTaxFee() {
        return taxFee;
    }
    public void setTaxFee(Integer taxFee) {
        this.taxFee = taxFee;
    }
    public Integer getAdditionalFee() {
        return additionalFee;
    }
    public void setAdditionalFee(Integer additionalFee) {
        this.additionalFee = additionalFee;
    }
    public User getSeller() {
        return seller;
    }
    public Set<Menu> getMenus() {
        return menus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Restaurant)) return false;
        Restaurant that = (Restaurant) o;
        return this.id != null && this.id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}