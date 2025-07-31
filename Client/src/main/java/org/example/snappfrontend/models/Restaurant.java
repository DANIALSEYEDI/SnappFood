package org.example.snappfrontend.models;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Restaurant {
    private Long id;
    private String name;
    private String address;
    private String phone;
    private String logoBase64;
    @JsonProperty("tax_fee")
    private Integer taxFee;
    @JsonProperty("additional_fee")
    private Integer additionalFee;


    public Restaurant() {}

    // getter / setter
    public String getName(){
        return name;
    }
    public Long getId(){
        return id;
    }
    public String getAddress(){
        return address;
    }
    public void setAddress(String address){
        this.address = address;
    }
    public String getPhone(){
        return phone;
    }
    public void setPhone(String phone){
        this.phone = phone;
    }
    public String getLogoBase64(){return logoBase64;}
    public void setLogoBase64(String logoBase64){this.logoBase64 = logoBase64;}
    public Integer getTaxFee(){return taxFee;}
    public void setTaxFee(Integer taxFee){this.taxFee = taxFee;}
    public Integer getAdditionalFee(){return additionalFee;}
    public void setAdditionalFee(Integer additionalFee){this.additionalFee = additionalFee;}
}