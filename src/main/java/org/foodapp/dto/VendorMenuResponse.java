package org.foodapp.dto;

import org.foodapp.model.FoodItem;
import org.foodapp.model.Restaurant;

import java.util.List;
import java.util.Map;

public class VendorMenuResponse {
    public Restaurant vendor;
    public List<String> menu_titles;
    public Map<String, List<FoodItem>> menu_items_by_title;
}
