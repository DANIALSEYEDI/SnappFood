package org.example.snappfrontend.dto;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RestaurantCreateRequestDTO {
    public String name;
    public String address;
    public String phone;

    @JsonProperty("logoBase64")
    public String logoBase64;

    @JsonProperty("tax_fee")
    public Double tax_fee;

    @JsonProperty("additional_fee")
    public Double additionalFee;

    public RestaurantCreateRequestDTO() {}
}