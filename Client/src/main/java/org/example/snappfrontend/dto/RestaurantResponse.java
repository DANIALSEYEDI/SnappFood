package org.example.snappfrontend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RestaurantResponse {

    private int id;
    private String name;
    private String address;
    private String phone;
    @JsonProperty("logoBase64")
    private String logoBase64;
    @JsonProperty("tax_fee")
    private Double taxFee;
    @JsonProperty("additional_fee")
    private Double additionalFee;
    public RestaurantResponse() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getLogoBase64() { return logoBase64; }
    public void setLogoBase64(String logoBase64) { this.logoBase64 = logoBase64; }
    public Double getTaxFee() { return taxFee; }
    public void setTaxFee(Double taxFee) { this.taxFee = taxFee; }
    public Double getAdditionalFee() { return additionalFee; }
    public void setAdditionalFee(Double additionalFee) { this.additionalFee = additionalFee; }
}