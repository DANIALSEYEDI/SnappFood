package org.foodapp.dto;
import org.foodapp.model.Rating;
import java.time.LocalDateTime;
import java.util.List;

public class RatingDto {
    public Long user_id;
    public int rating;
    public Long food_item_id;
    public String comment;
    public List<String> imageBase64;
    public LocalDateTime createdAt;

    public static RatingDto fromEntity(Rating r) {
        RatingDto dto = new RatingDto();
        dto.rating = r.getRating();
        dto.comment = r.getComment();
        dto.user_id = r.getUser() != null ? r.getUser().getId() : null;
        dto.imageBase64 = r.getImageBase64();
        dto.createdAt = r.getCreatedAt();
        dto.food_item_id = r.getItem() != null ? r.getItem().getId() : null;
        return dto;
    }

}
