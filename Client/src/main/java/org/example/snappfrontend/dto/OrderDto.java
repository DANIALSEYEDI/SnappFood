package org.example.snappfrontend.dto;

public class OrderDto {
    private Long id;
    private Integer customerId;
    private String deliveryAddress;
    private String status;
    private Integer payPrice;
    private String createdAt;
    private String restaurantStatus;

    public OrderDto(Long id, Integer customerId, String deliveryAddress, String status,
                    Integer payPrice, String createdAt, String restaurantStatus) {
        this.id = id;
        this.customerId = customerId;
        this.deliveryAddress = deliveryAddress;
        this.status = status;
        this.payPrice = payPrice;
        this.createdAt = createdAt;
        this.restaurantStatus = restaurantStatus;
    }
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getPayPrice() { return payPrice; }
    public void setPayPrice(Integer payPrice) { this.payPrice = payPrice; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getRestaurantStatus() { return restaurantStatus; }
    public void setRestaurantStatus(String restaurantStatus) { this.restaurantStatus = restaurantStatus; }
}