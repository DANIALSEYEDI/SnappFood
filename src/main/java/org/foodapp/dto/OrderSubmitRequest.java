package org.foodapp.dto;

import java.util.List;

public class OrderSubmitRequest {
    public String delivery_address;
    public Long vendor_id;
    public Long coupon_id; // Optional
    public List<OrderItemRequest> items;
}

