package org.foodapp.dto;
import java.util.List;

public class RestaurantFoodItemRequest {
    public String name;
    public String imageBase64;
    public String description;
    public Integer price;
    public Integer supply;
    public List<String> keywords;
}
