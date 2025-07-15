package org.foodapp.dto;

import org.foodapp.model.Transaction;
import java.time.format.DateTimeFormatter;

public class TransactionsResponse {
    public Long id;
    public Integer amount;
    public String description;
    public String createdAt;

    public static TransactionsResponse from(Transaction tx) {
        TransactionsResponse dto = new TransactionsResponse();
        dto.id = tx.getId();
        dto.amount = tx.getAmount();
        dto.description = tx.getDescription();
        dto.createdAt = tx.getCreatedAt().toString();
        return dto;
    }
}
