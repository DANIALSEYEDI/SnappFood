package org.foodapp.dto;

import org.foodapp.model.Transaction;
public class TransactionsResponse {
    public Long id;
    public Long order_id;
    public Long user_id;
    public String method;
    public String status;

    public static TransactionsResponse from(Transaction tx) {
        TransactionsResponse dto = new TransactionsResponse();
        dto.id = tx.getId();
        dto.order_id = tx.getOrder() != null ? tx.getOrder().getId() : null;
        dto.user_id = tx.getUser() != null ? tx.getUser().getId() : null;
        dto.method = tx.getMethod().name().toLowerCase();
        dto.status = tx.getStatus().name().toLowerCase();
        return dto;
    }

}
