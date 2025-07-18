package org.foodapp.dto;
import org.foodapp.model.FoodItem;
import java.util.List;

public class FoodItemResponse {
    public Long id;
    public String name;
    public String description;
    public Integer price;
    public Integer supply;
    public List<String> keywords;
    public Long restaurantId;

    public FoodItemResponse(FoodItem item) {
        this.id = item.getId();
        this.name = item.getName();
        this.description = item.getDescription();
        this.price = item.getPrice();
        this.supply = item.getSupply();
        this.keywords = item.getKeywords();
        this.restaurantId = item.getRestaurant().getId();
    }
}
