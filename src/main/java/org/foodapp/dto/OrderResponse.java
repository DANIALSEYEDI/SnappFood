package org.foodapp.dto;

import org.foodapp.model.Order;
public class OrderResponse {
    private Long id;
    private String status;
    private String user;
    private String courier;
    private Long restaurantId;


    public OrderResponse() {}

    public static OrderResponse fromEntity(Order order) {
        OrderResponse dto = new OrderResponse();
        dto.setId(order.getId());
        dto.setStatus(order.getStatus().name());
        dto.setUser(order.getUser().getPhoneNumber());
        dto.setCourier(order.getCourier() != null ? order.getCourier().getPhoneNumber() : null);
        dto.setRestaurantId(order.getRestaurant().getId());
        return dto;
    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }

    public String getCourier() { return courier; }
    public void setCourier(String courier) { this.courier = courier; }

    public Long getRestaurantId() { return restaurantId; }
    public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }
}
