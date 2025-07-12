package org.foodapp.dto;

import org.foodapp.model.Order;
//import org.foodapp.model.OrderItem;

import java.util.List;
import java.util.stream.Collectors;

public class OrderAdminResponse {
    private Long id;
    private String status;
    private String user;
    private String courier;
    private Long restaurantId;
    private String deliveryAddress;
    private List<OrderItemResponse> items;

    public static OrderAdminResponse fromEntity(Order order) {
        OrderAdminResponse dto = new OrderAdminResponse();
        dto.setId(order.getId());
        dto.setStatus(order.getStatus().name());
        dto.setUser(order.getUser() != null ? order.getUser().getPhoneNumber() : null);
        dto.setCourier(order.getCourier() != null ? order.getCourier().getPhoneNumber() : null);
        dto.setRestaurantId(order.getRestaurant() != null ? order.getRestaurant().getId() : null);
        dto.setDeliveryAddress(order.getDeliveryAddress());
        dto.setItems(order.getItemsOfOrder().stream()
                .map(OrderItemResponse::fromEntity)
                .collect(Collectors.toList()));

        return dto;
    }

    // Getters and Setters
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

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public List<OrderItemResponse> getItems() { return items; }
    public void setItems(List<OrderItemResponse> items) { this.items = items; }
}
