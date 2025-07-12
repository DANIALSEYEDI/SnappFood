package org.foodapp.dto;

public class RestaurantUpdateOrderStatusRequest {
    private String status; // accepted, rejected, served

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

