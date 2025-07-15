package org.foodapp.dto;

import org.foodapp.model.Order;
import java.util.stream.Collectors;
import java.util.List;

public class DeliveryResponse {
    public Long id;
    public String status;
    public String delivery_status;
    public String user;
    public String courier;
    public Long restaurant_id;
    public String delivery_address;
    public Integer total_price;
    public List<OrderItemResponse> items;

    public static DeliveryResponse fromEntity(Order order) {
        DeliveryResponse dto = new DeliveryResponse();
        dto.id = order.getId();
        dto.status = order.getStatus().name();
        dto.delivery_status = order.getDeliveryStatus() != null ? order.getDeliveryStatus().name() : null;
        dto.user = order.getUser() != null ? order.getUser().getPhoneNumber() : null;
        dto.courier = order.getCourier() != null ? order.getCourier().getPhoneNumber() : null;
        dto.restaurant_id = order.getRestaurant().getId();
        dto.delivery_address = order.getDeliveryAddress();
        dto.total_price = order.getTotalPrice();
        dto.items = order.getItemsOfOrder().stream()
                .map(OrderItemResponse::fromEntity)
                .collect(Collectors.toList());
        return dto;
    }
}
