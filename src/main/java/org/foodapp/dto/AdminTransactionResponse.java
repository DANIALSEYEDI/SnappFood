package org.foodapp.dto;

import org.foodapp.model.Transaction;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;


public class AdminTransactionResponse {
    public Long id;
    public Long user_id;
    public Long order_id;
    public String method;
    public String status;
    public BigDecimal amount;
    public String createdAt;

    public static AdminTransactionResponse fromEntity(Transaction tx) {
        AdminTransactionResponse dto = new AdminTransactionResponse();
        dto.id = tx.getId();
        dto.user_id = tx.getUser().getId();
        dto.order_id = tx.getOrder() != null ? tx.getOrder().getId() : null;
        dto.method = tx.getMethod().name();
        dto.status = tx.getStatus().name();
        dto.amount = tx.getAmount();
        dto.createdAt = tx.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return dto;
    }
}
