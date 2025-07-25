package org.foodapp.dto.Response;
import org.foodapp.model.Transaction;

public class AdminTransactionResponse {
    public Long id;
    public Long order_id;
    public Long user_id;
    public String method;
    public String status;

    public static AdminTransactionResponse fromEntity(Transaction tx) {
        AdminTransactionResponse dto = new AdminTransactionResponse();
        dto.id = tx.getId();
        dto.order_id = tx.getOrder() != null ? tx.getOrder().getId() : null;
        dto.user_id = tx.getUser().getId();
        dto.method = tx.getMethod().name();
        dto.status = tx.getStatus().name();
        return dto;
    }
}
