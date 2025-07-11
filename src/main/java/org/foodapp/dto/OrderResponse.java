package org.foodapp.dto;

import org.foodapp.model.OrderStatus;

import java.util.List;

public class OrderResponse {
    public Long id;
    public Long userId;
    public Long restaurantId;
    public Long courierId; // ممکن است null باشد
    public List<Long> itemIds;
    public String note;
    public Integer totalPrice;
    public OrderStatus status;
}

