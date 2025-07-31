package org.foodapp.dto.Response;
import org.foodapp.model.Rating;
import java.time.LocalDateTime;
import java.util.List;

public class RatingResponse {
    public Long id;
    public Long item_id;
    public int rating;
    public String comment;
    public List<String> imageBase64;
    public Long user_id;
    public LocalDateTime createdAt;

    public static RatingResponse fromEntity(Rating r) {
        RatingResponse dto = new RatingResponse();
        dto.id = r.getId();
        dto.item_id = r.getItem() != null ? r.getItem().getId() : null;
        dto.rating = r.getRating();
        dto.comment = r.getComment();
        dto.user_id = r.getUser() != null ? r.getUser().getId() : null;
        dto.imageBase64 = r.getImageBase64();
        dto.createdAt = r.getCreatedAt();
        return dto;
    }

}