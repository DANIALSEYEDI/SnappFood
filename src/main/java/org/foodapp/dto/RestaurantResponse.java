package org.foodapp.dto;

public class RestaurantResponse {
    public Long id;
    public String name;
    public String address;
    public String phone;
    public String logoBase64;
    public Integer tax_fee;
    public Integer additional_fee;
    public boolean approved;

    public RestaurantResponse() {}

    public RestaurantResponse(Long id, String name, String address, String phone, String logoBase64, Integer tax_fee, Integer additional_fee, boolean approved) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.logoBase64 = logoBase64;
        this.tax_fee = tax_fee;
        this.additional_fee = additional_fee;
        this.approved = approved;
    }
}
