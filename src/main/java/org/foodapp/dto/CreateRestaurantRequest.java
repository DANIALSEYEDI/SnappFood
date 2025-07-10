package org.foodapp.dto;

public class CreateRestaurantRequest {
    public String name;
    public String address;
    public String phone;
    public String logoBase64;
    public Integer tax_fee;
    public Integer additional_fee;

    public CreateRestaurantRequest() {}
}

