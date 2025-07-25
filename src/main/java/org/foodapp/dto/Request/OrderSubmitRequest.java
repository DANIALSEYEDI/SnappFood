package org.foodapp.dto.Request;

import java.util.List;

public class OrderSubmitRequest {
    public String delivery_address;
    public Long vendor_id;
    public Long coupon_id;
    public List<OrderItemRequest> items;
}

