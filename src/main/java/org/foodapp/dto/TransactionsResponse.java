package org.foodapp.dto;

import org.foodapp.model.Transaction;
import java.time.format.DateTimeFormatter;

public class TransactionsResponse {
    public Long id;
    public Integer amount;
    public String type;
    public String created_at;

    public static TransactionsResponse fromEntity(Transaction t) {
        TransactionsResponse dto = new TransactionsResponse();
        dto.id = t.getId();
        dto.amount = t.getAmount();
        dto.type = t.getType().name();
        dto.created_at = t.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return dto;
    }
}
