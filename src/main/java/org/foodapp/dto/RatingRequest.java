package org.foodapp.dto;

import java.util.List;

public class RatingRequest {
    public Long order_id;
    public Integer rating;
    public String comment;
    public List<String> imageBase64;
}
