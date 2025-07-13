package org.foodapp.dto;

import org.foodapp.model.Rating;

import java.time.format.DateTimeFormatter;

public class RatingResponse {
    public Long id;
    public Long order_id;
    public Integer rating;
    public String comment;
    public Long user_id;
    public String created_at;

    public static RatingResponse fromEntity(Rating rating) {
        RatingResponse dto = new RatingResponse();
        dto.id = rating.getId();
        dto.order_id = rating.getOrder() != null ? rating.getOrder().getId() : null;
        dto.rating = rating.getRating();
        dto.comment = rating.getComment();
        dto.user_id = rating.getUser() != null ? rating.getUser().getId() : null;

        if (rating.getCreatedAt() != null) {
            dto.created_at = rating.getCreatedAt()
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME); // یا فرمت دلخواه
        }

        return dto;
    }
}
