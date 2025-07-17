package org.foodapp.dto;

import org.foodapp.model.Order;
import org.foodapp.model.OrderItem;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class OrderResponse {
    public Long id;
    public String delivery_address;
    public Long customer_id;
    public Long vendor_id;
    public Long coupon_id;
    public List<Long> item_ids;
    public Integer raw_price;
    public Integer tax_fee;
    public Integer additional_fee;
    public Integer courier_fee;
    public Integer pay_price;
    public Long courier_id;
    public String status;
    public String created_at;
    public String updated_at;

    public static OrderResponse fromEntity(Order order) {
        OrderResponse dto = new OrderResponse();
        dto.id = order.getId();
        dto.delivery_address = order.getDeliveryAddress();
        dto.customer_id = order.getUser() != null ? order.getUser().getId() : null;
        dto.vendor_id = order.getRestaurant() != null ? order.getRestaurant().getId() : null;
        dto.coupon_id = order.getCoupon() != null ? order.getCoupon().getId() : null;
        dto.item_ids = order.getItemsOfOrder() != null
                ? order.getItemsOfOrder().stream()
                .map(item -> item.getItem().getId())
                .collect(Collectors.toList())
                : List.of();
        dto.raw_price = order.getRawPrice();
        dto.tax_fee = order.getTaxFee();
        dto.additional_fee = order.getAdditionalFee();
        dto.courier_fee = order.getCourierFee();
        dto.pay_price = order.getPayPrice();
        dto.courier_id = order.getCourier() != null ? order.getCourier().getId() : null;
        dto.status = order.getStatus().name().toLowerCase().replace("_", " ");
        dto.created_at = order.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        dto.updated_at = order.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return dto;
    }
}

