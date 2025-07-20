package org.foodapp.dto;

import org.foodapp.model.FoodItem;
import org.foodapp.model.OrderItem;

public class OrderItemResponse {
    private Long item_id;
    private String name;
    private Integer quantity;
    private Integer price;

    public static OrderItemResponse fromEntity(OrderItem orderItem) {
        FoodItem item = orderItem.getItem();
        OrderItemResponse dto = new OrderItemResponse();
        dto.setItem_id(item.getId());
        dto.setName(item.getName());
        dto.setQuantity(orderItem.getQuantity());
        dto.setPrice(item.getPrice());
        return dto;
    }
    // Getters and Setters
    public Long getItem_id() {
        return item_id;
    }

    public void setItem_id(Long item_id) {
        this.item_id = item_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }


}
