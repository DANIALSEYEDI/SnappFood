package org.example.snappfrontend.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Order {
    private Long id;
    @JsonProperty("delivery_address")
    private String deliveryAddress;
    @JsonProperty("customer_id")
    private Integer customerId;
    @JsonProperty("vendor_id")
    private Integer vendorId;
    @JsonProperty("coupon_id")
    private Integer couponId;
    @JsonProperty("item_ids")
    private List<Integer> itemIds;
    @JsonProperty("raw_price")
    private Integer rawPrice;
    @JsonProperty("tax_fee")
    private Integer taxFee;
    @JsonProperty("additional_fee")
    private Integer additionalFee;
    @JsonProperty("courier_fee")
    private Integer courierFee;
    @JsonProperty("pay_price")
    private Integer payPrice;
    @JsonProperty("courier_id")
    private Integer courierId;
    @JsonProperty("status")
    private String status;
    @JsonProperty("created_at")
    private String createdAt;
    @JsonProperty("updated_at")
    private String updatedAt;
    @JsonProperty("deliveryStatus")
    private String deliveryStatus;
    @JsonProperty("restaurantStatus")
    private String restaurantStatus;

    // Getters and Setters
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }
    public Integer getVendorId() { return vendorId; }
    public void setVendorId(Integer vendorId) { this.vendorId = vendorId; }
    public Integer getCouponId() { return couponId; }
    public void setCouponId(Integer couponId) { this.couponId = couponId; }
    public List<Integer> getItemIds() { return itemIds; }
    public void setItemIds(List<Integer> itemIds) { this.itemIds = itemIds; }
    public Integer getRawPrice() { return rawPrice; }
    public void setRawPrice(Integer rawPrice) { this.rawPrice = rawPrice; }
    public Integer getTaxFee() { return taxFee; }
    public void setTaxFee(Integer taxFee) { this.taxFee = taxFee; }
    public Integer getAdditionalFee() { return additionalFee; }
    public void setAdditionalFee(Integer additionalFee) { this.additionalFee = additionalFee; }
    public Integer getCourierFee() { return courierFee; }
    public void setCourierFee(Integer courierFee) { this.courierFee = courierFee; }
    public Integer getPayPrice() { return payPrice; }
    public void setPayPrice(Integer payPrice) { this.payPrice = payPrice; }
    public Integer getCourierId() { return courierId; }
    public void setCourierId(Integer courierId) { this.courierId = courierId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public String getDeliveryStatus() { return deliveryStatus; }
    public void setDeliveryStatus(String deliveryStatus) { this.deliveryStatus = deliveryStatus; }
    public String getRestaurantStatus() { return restaurantStatus; }
    public void setRestaurantStatus(String restaurantStatus) { this.restaurantStatus = restaurantStatus; }
}