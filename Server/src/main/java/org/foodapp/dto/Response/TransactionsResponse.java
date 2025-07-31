package org.foodapp.dto.Response;
import org.foodapp.model.Transaction;
import java.math.BigDecimal;

public class TransactionsResponse {
    public Long id;
    public Long order_id;
    public Long user_id;
    public String method;
    public String status;
    public BigDecimal amount;

    public static TransactionsResponse from(Transaction tx) {
        TransactionsResponse dto = new TransactionsResponse();
        dto.id = tx.getId();
        dto.order_id = tx.getOrder() != null ? tx.getOrder().getId() : 0;
        dto.user_id = tx.getUser() != null ? tx.getUser().getId() : null;
        dto.method = tx.getMethod().name().toLowerCase();
        dto.status = tx.getStatus().name().toLowerCase();
        dto.amount=tx.getAmount();
        return dto;
    }
}