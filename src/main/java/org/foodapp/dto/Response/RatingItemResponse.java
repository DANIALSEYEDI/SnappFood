package org.foodapp.dto.Response;
import java.util.List;

public class RatingItemResponse {
    public double avg_rating;
    public List<RatingResponse> comments;

    public RatingItemResponse(double avg, List<RatingResponse> list) {
        this.avg_rating = avg;
        this.comments = list;
    }
}