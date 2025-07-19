package org.foodapp.dto;
import org.foodapp.model.Rating;
import java.util.List;
import java.util.stream.Collectors;

public class RatingResponse {
    public double avg_rating;
    public List<RatingDto> comments;

    public RatingResponse(double avg, List<Rating> rawRatings) {
        this.avg_rating = avg;
        this.comments = rawRatings.stream()
                .map(RatingDto::fromEntity)
                .collect(Collectors.toList());
    }
}

