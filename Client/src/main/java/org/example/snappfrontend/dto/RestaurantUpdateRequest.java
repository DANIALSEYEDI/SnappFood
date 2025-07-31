package org.example.snappfrontend.dto;
public class RestaurantUpdateRequest {
    private String name;
    private String address;
    private String phone;
    private Double tax_fee;
    private Double additional_fee;
    private String logoBase64;

    public RestaurantUpdateRequest() {}
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Double getTax_fee() { return tax_fee; }
    public void setTax_fee(Double tax_fee) { this.tax_fee = tax_fee; }
    public Double getAdditional_fee() { return additional_fee; }
    public void setAdditional_fee(Double additional_fee) { this.additional_fee = additional_fee; }
    public String getLogoBase64() { return logoBase64; }
    public void setLogoBase64(String logoBase64) { this.logoBase64 = logoBase64; }
}