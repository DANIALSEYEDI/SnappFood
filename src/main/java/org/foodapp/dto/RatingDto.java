package org.foodapp.dto;

import org.foodapp.model.Rating;

import java.time.LocalDateTime;
import java.util.List;

public class RatingDto {
    public int rating;
    public String comment;
    public String user;
    public List<String> imageBase64;
    public LocalDateTime createdAt;

    public static RatingDto fromEntity(Rating r) {
        RatingDto dto = new RatingDto();
        dto.rating = r.getRating();
        dto.comment = r.getComment();
        dto.user = r.getUser() != null ? r.getUser().getPhoneNumber() : null;
        dto.imageBase64 = r.getImageBase64();
        dto.createdAt = r.getCreatedAt();
        return dto;
    }
}
