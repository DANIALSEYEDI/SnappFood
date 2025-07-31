package org.example.snappfrontend.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
public class FoodItemResponse {
    private Long id;
    private String name;
    private String description;
    private Integer price;
    private Integer supply;
    private List<String> keywords;
    @JsonProperty("vendor_id")
    private Long vendorId;

    public Long getId() {return id;}
    public String getName() {return name;}
    public String getDescription() {return description;}
    public Integer getPrice() {return price;}
    public Integer getSupply() {return supply;}
    public List<String> getKeywords() {return keywords;}
    public Long getVendor_id() {return vendorId;}
    public void setId(Long id) {this.id = id;}
    public void setName(String name) {this.name = name;}
    public void setDescription(String description) {this.description = description;}
    public void setPrice(Integer price) {this.price = price;}
    public void setSupply(Integer supply) {this.supply = supply;}
    public void setKeywords(List<String> keywords) {this.keywords = keywords;}
    public void setVendor_id(Long vendorId) {this.vendorId = vendorId;}
}