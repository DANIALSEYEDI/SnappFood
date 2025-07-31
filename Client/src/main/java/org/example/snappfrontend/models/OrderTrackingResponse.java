package org.example.snappfrontend.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderTrackingResponse {
    public Long id;
    public String status;
    public String deliveryStatus;
    public String restaurantStatus;

    public OrderTrackingResponse(){}

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}
    public String getStatus() {return status;}
    public void setStatus(String status) {this.status = status;}
    public void setDeliveryStatus(String deliveryStatus) {this.deliveryStatus = deliveryStatus;}
    public void setRestaurantStatus(String restaurantStatus) {this.restaurantStatus = restaurantStatus;}
    public String getDeliveryStatus() {
        return deliveryStatus;
    }
    public String getRestaurantStatus() {
        return restaurantStatus;
    }
}