package org.foodapp.dto;

import org.foodapp.model.Payment;
import org.foodapp.model.PaymentMethod;

import java.time.LocalDateTime;

public class PaymentResponse {
    public Long id;
    public PaymentMethod method;
    public Long order_id;
    public Integer amount;
    public String paid_at;

    public static PaymentResponse fromEntity(Payment payment) {
        PaymentResponse dto = new PaymentResponse();
        dto.id = payment.getId();
        dto.method = payment.getMethod();
        dto.order_id = payment.getOrder().getId();
        dto.amount = payment.getAmount();
        dto.paid_at = payment.getPaidAt().toString();
        return dto;
    }
}
